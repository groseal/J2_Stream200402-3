package ru.gb.jtwo.e_lesson.online.network;

public class ServerSocketThread extends Thread {

    private final int port;

    public ServerSocketThread(String name, int port) {
        super(name);
        this.port = port;
        start();
    }

    @Override
    public void run() {
        System.out.println("Server started at port: " + port);
        while (!isInterrupted()) {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                interrupt();
            }
            System.out.println("SST is working");
        }
        System.out.println("Server stopped");

    }
}
