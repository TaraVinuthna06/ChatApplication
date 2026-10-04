package com.chatapp.client;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
        System.out.println("✅ Connected to server");

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        // Thread 1: read from server and print
        Thread readerThread = new Thread(() -> {
            try {
                String serverMsg;
                while ((serverMsg = in.readLine()) != null) {
                    System.out.println(serverMsg);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server.");
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();

        // Main thread: read from console and send
        String userInput;
        while ((userInput = console.readLine()) != null) {
            out.println(userInput);
            if (userInput.equalsIgnoreCase("/quit")) break;
        }

        socket.close();
    }
}