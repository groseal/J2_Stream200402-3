package ru.gb.jtwo.chat.common;

public class Library {
    /*
    * /auth_request±login±password сообщение авторизации
    * /auth_accept±nickname верная авторизация
    * /auth_denied неверная авторизация
    * /broadcast±msg обычное сообщение
    * /new_nickname±msg запрос смены ника
    *
    * /msg_format_error±msg один из участников взаимодействия не понял что ему прислали
    * */

    public static final String DELIMITER = "±";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENIED = "/auth_denied";
    public static final String MSG_FORMAT_ERROR = "/msg_format_error";// если мы вдруг не поняли, что за сообщение и не смогли разобрать
    public static final String TYPE_BROADCAST = "/bcast";//тип сообщения который говорит что сервер рассылает сообщение всем
    // то есть сообщение, которое будет посылаться всем
    public static final String TYPE_BCAST_CLIENT = "/client_msg";//тип сообщения который говорит что клиент хочет отослать сообщение всем
    public static final String USER_LIST = "/user_list";//тип сообщений для синхронизации списка позьзователей
    public static final String NEW_NICKNAME ="/new_nickname";//тип сообщений для смены ника

    //Клиентское сообщение
    public static String getTypeBcastClient(String msg) {
        return TYPE_BCAST_CLIENT + DELIMITER + msg;
    }
    //Формирование списка пользователей на сервере и передача его в
    public static String getUserList(String users) {
        return USER_LIST + DELIMITER + users;
    }


    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {
        return AUTH_DENIED;
    }

    public static String getMsgFormatError(String message) {
        return MSG_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getTypeBroadcast(String src, String message) {
        return TYPE_BROADCAST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

    public static String getNewNickname(String message){return NEW_NICKNAME+ DELIMITER +message;}

}
