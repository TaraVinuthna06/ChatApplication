package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 1. Ask for username
            out.println("Enter your username:");
            username = in.readLine();
            if (username == null || username.isBlank()) {
                socket.close();
                return;
            }

            // Check for duplicate usernames
            if (ChatServer.getUsernames().contains(username)) {
                out.println("Username already taken. Disconnecting.");
                socket.close();
                return;
            }

            ChatServer.addClient(username, this);
            out.println("Welcome " + username + "! Type /help for commands.");
            ChatServer.broadcast("📢 " + username + " joined the chat", this);
            System.out.println(username + " joined");

            // 2. Listen for messages
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String message) {
        // Command: /help
        if (message.equals("/help")) {
            sendMessage("Commands: /help, /users, /msg <user> <message>, /quit");
            return;
        }

        // Command: /users
        if (message.equals("/users")) {
            sendMessage("Online: " + String.join(", ", ChatServer.getUsernames()));
            return;
        }

        // Command: /msg <user> <message>  (private message)
        if (message.startsWith("/msg ")) {
            String[] parts = message.split(" ", 3);
            if (parts.length < 3) {
                sendMessage("Usage: /msg <user> <message>");
                return;
            }
            String target = parts[1];
            String text = parts[2];
            boolean sent = ChatServer.sendToUser(target, "🔒 [PM from " + username + "]: " + text);
            sendMessage(sent ? "Sent to " + target : "User not found: " + target);
            return;
        }

        // Command: /quit
        if (message.equals("/quit")) {
            try { socket.close(); } catch (IOException ignored) {}
            return;
        }

        // Normal broadcast
        ChatServer.broadcast("[" + username + "]: " + message, this);
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    private void cleanup() {
        try {
            if (username != null) {
                ChatServer.removeClient(username);
                ChatServer.broadcast("❌ " + username + " left the chat", this);
                System.out.println(username + " disconnected");
            }
            if (!socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}