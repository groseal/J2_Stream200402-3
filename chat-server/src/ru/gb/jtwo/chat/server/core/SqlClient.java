package ru.gb.jtwo.chat.server.core;

import java.sql.*;

//Работает с базой данных
public class SqlClient {

    private static Connection connection;//соединение с БД
    private static Statement statement;//отправка в БД запровсов
    private static PreparedStatement preparedStatement; //подготовленное выражение ждя смены ника

    //подключение к БД
    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");//регистрация драйвера для работы с БД
            connection = DriverManager.getConnection("jdbc:sqlite:chat-server/chat-db.sqlite");//соединение с БД
            statement = connection.createStatement();//делаем запрос в БД
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

    }

    //проверка в БД логина и пароля
    synchronized static String getNickname(String login, String password) {
        String query = String.format("select nickname from users where login='%s' and password='%s'", login, password);//создаем запрос (SQL) в БД
        try (ResultSet set = statement.executeQuery(query)) {//выполнение запроса
            if (set.next())//ответом на запрос является set коллекция, если в ответе на запрос что то есть
                return set.getString(1);//просим вернуть первую колонку (nickname) в виде строки
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;//если на запрос ничего не нешлост возвращаем null
    }

    //Смена ника
    synchronized static void nicknameChange(String login, String newNickname) {
        String query = String.format("select nickname from users where login='%s'", login);//создаем запрос (SQL) в БД
        try (ResultSet set = statement.executeQuery(query)) {//выполнение запроса
            if (set.next())//ответом на запрос является set коллекция, если в ответе на запрос что то есть
            {
                statement.executeUpdate("UPDATE users SET nickname='" + newNickname + "'");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //отключение от БД
    synchronized static void disconnect() {
        try {
            connection.close();//закрываем соединение с БД
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
