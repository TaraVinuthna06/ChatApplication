package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 5000;

    // Thread-safe map of username -> handler
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    // Thread pool for handling clients
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("✅ Server started on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("🔌 New connection from " + socket.getInetAddress());
            ClientHandler handler = new ClientHandler(socket);
            pool.execute(handler);
        }
    }

    // Broadcast to all clients
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients.values()) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Send to a specific user
    public static boolean sendToUser(String username, String message) {
        ClientHandler target = clients.get(username);
        if (target != null) {
            target.sendMessage(message);
            return true;
        }
        return false;
    }

    public static void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
    }

    public static void removeClient(String username) {
        clients.remove(username);
    }

    public static Set<String> getUsernames() {
        return clients.keySet();
    }
}