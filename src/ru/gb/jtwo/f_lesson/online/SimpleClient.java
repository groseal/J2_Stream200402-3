package ru.gb.jtwo.f_lesson.online;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SimpleClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", 8189);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("connected to server");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String msg = scanner.nextLine();
                out.writeUTF(msg);
                String b = in.readUTF();
                System.out.println(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
