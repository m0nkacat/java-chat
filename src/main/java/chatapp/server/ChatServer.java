package main.java.chatapp.server;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 12345;
    private static ConcurrentHashMap<String, PrintWriter> onlineUsers = new ConcurrentHashMap<>();
    private static UserManager userManager;
    private static ChannelManager channelManager;
    private static HistoryManager historyManager;

    public static void main(String[] args) {
        System.out.println("Чат-сервер запущен...");
        userManager = new UserManager();
        channelManager = new ChannelManager();
        historyManager = new HistoryManager();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, userManager, channelManager, historyManager, onlineUsers).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}