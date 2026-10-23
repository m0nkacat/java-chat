package main.java.chatapp.client;

import main.java.chatapp.common.Protocol;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient2 implements ChatClientInterface {
    private static final String SERVER = "localhost";
    private static final int PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String myNick;
    private ClientMenu menu;
    private volatile boolean inChat = false;

    public static void main(String[] args) {
        new ChatClient2().start();
    }

    public void start() {
        try {
            socket = new Socket(SERVER, PORT);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            new Thread(new IncomingHandler(in, this)).start();
            menu = new ClientMenu(this);
            menu.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String cmd) { out.println(cmd); }
    public void setMyNick(String nick) { this.myNick = nick; }
    public void setInChat(boolean inChat) { this.inChat = inChat; }
    public boolean isInChat() { return inChat; }
    public void stop() { try { socket.close(); } catch (IOException e) {} System.exit(0); }
}