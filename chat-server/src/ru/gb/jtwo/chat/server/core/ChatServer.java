package ru.gb.jtwo.chat.server.core;

import ru.gb.jtwo.chat.common.Library;
import ru.gb.jtwo.network.ServerSocketThread;
import ru.gb.jtwo.network.ServerSocketThreadListener;
import ru.gb.jtwo.network.SocketThread;
import ru.gb.jtwo.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    ServerSocketThread server;
    ChatServerListener listener;
    Vector<SocketThread> clients = new Vector<>();

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (server != null && server.isAlive())
            putLog("Already running");
        else
            server = new ServerSocketThread(this, "Server", port, 2000);
    }

    public void stop() {
        if (server == null || !server.isAlive()) {
            putLog("Nothing to stop");
        } else {
            server.interrupt();
        }
    }

    private void putLog(String msg) {
        listener.onChatServerMessage(msg);
    }

    /**
     * Server Socket Thread Listener methods
     * */

    @Override
    public void onServerStarted(ServerSocketThread thread) {
        putLog("Server thread started");
        SqlClient.connect();
    }

    @Override
    public void onServerCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server socket started");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
        //putLog("Server timeout");
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        putLog("Client connected");
        String name = "SocketThread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, name, socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable throwable) {
        putLog("Server exception");
        throwable.printStackTrace();
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server thread stopped");
        dropAllClients();//отключаем всех клиентов
        SqlClient.disconnect();//отключаем БД
    }

    /**
     * Socket Thread Listener methods
     * */

    @Override
    public synchronized void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Socket started");
    }

    @Override
    public synchronized void onSocketStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
        if (client.isAuthorized() && !client.isReconnecting()) {//если клиент авторизован и не переподключается
            sendToAllAuthorizedClients(Library.getTypeBroadcast("Server",
                    client.getNickname() + " disconnected"));//Сообщаем всем в чате что клиент отключился если он был авторизован
        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));//Синхронизирует список пользователей
    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Socket ready");
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {
            handleAuthMessage(client, msg);//пришло типизированное сообщение от авторизованного пользователя
        } else
            handleNonAuthMessage(client, msg);
    }

    @Override
    public synchronized void onSocketException(SocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
    }

    //сообщения от авторизованных пользователей
    void handleAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.TYPE_BCAST_CLIENT://клиент попросил сервер отправить широковещательное сообщение
                sendToAllAuthorizedClients(Library.getTypeBroadcast(
                        client.getNickname(), arr[1]));//посылает всем клиентам широковещательное сообщение
                break;
            default:
                client.sendMessage(Library.getMsgFormatError(msg));//сообщение сервера клиенту что сообщение не понято
        }

    }

    //сообщения от неавторизованных пользователей
    void handleNonAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            client.msgFormatError(msg);//происходит если присланны неверные данные в сообщении для авторизации
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNickname(login, password);
        if (nickname == null) {
            putLog("Invalid login attempt: " + login);
            client.authFail();
            return;
        } else {
            ClientThread oldClient = findClientByNickname(nickname);
            client.authAccept(nickname);//клиент успешно прошел авторизацию
            if (oldClient == null) {
                //сообщение для других клиентов в чате что в нем теперь еще один пользователь
                sendToAllAuthorizedClients(Library.getTypeBroadcast("Server", nickname + " connected"));//клиетн переподключился
            } else {
                oldClient.reconnect();//отключаем старого клиента
                clients.remove(oldClient);//удаляем старого клиента из списка клиентов
            }
        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));//Синхронизирует списки пользователей
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            client.sendMessage(msg);
        }
    }

    //отключает всех клиентов
    public void dropAllClients() {
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
    }

    //Добавляет пользователя если он авторизован в список пользователей
    private synchronized String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);//
            if (!client.isAuthorized()) continue;//проверяет авторизован или нет пользователь
            sb.append(client.getNickname()).append(Library.DELIMITER);//добавляет пользователя в список пользователей если он авторизован
        }
        return sb.toString();
    }


    private synchronized ClientThread findClientByNickname(String nickname) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }
}
