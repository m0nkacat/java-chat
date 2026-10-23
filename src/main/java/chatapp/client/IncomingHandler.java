package main.java.chatapp.client;

import main.java.chatapp.common.Protocol;
import java.io.BufferedReader;
import java.io.IOException;

public class IncomingHandler implements Runnable {
    private BufferedReader in;
    private ChatClientInterface client;

    public IncomingHandler(BufferedReader in, ChatClientInterface client) {
        this.in = in;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith(Protocol.RES_OK)) {
                    String msg = line.substring(3);
                    if (msg.startsWith(" AUTH ")) {
                        client.setMyNick(msg.substring(6));
                        System.out.println("[ОК] Авторизован");
                    } else if (msg.startsWith(" Личный чат с ")) {
                        client.setInChat(true);
                        System.out.println("[ОК]" + msg);
                    } else {
                        System.out.println("[ОК] " + msg);
                    }
                } else if (line.startsWith(Protocol.RES_ERR)) {
                    System.err.println("[ОШИБКА] " + line.substring(4));
                } else if (line.startsWith(Protocol.RES_CHANNEL_MSG)) {
                    String rest = line.substring(Protocol.RES_CHANNEL_MSG.length() + 1);
                    String[] parts = rest.split(" ", 2);
                    System.out.println("[" + parts[0] + "] " + parts[1].replace(Protocol.SEP, " | "));
                } else if (line.startsWith(Protocol.RES_PRIVATE_MSG)) {
                    String data = line.substring(Protocol.RES_PRIVATE_MSG.length() + 1);
                    String[] parts = data.split("\\" + Protocol.SEP);
                    if (parts.length >= 3) {
                        System.out.println("[Личное от " + parts[0] + "] " + parts[2] + " (" + parts[1] + ")");
                    }
                } else if (line.equals(Protocol.RES_END_HISTORY)) {
                    System.out.println("--- Конец истории ---");
                } else if (line.startsWith(Protocol.RES_USER_LIST)) {
                    System.out.println("Онлайн: " + line.substring(Protocol.RES_USER_LIST.length() + 1));
                } else if (line.equals("BYE")) {
                    client.stop();
                    break;
                } else if (line.contains(Protocol.SEP)) {
                    String[] parts = line.split("\\" + Protocol.SEP);
                    if (parts.length >= 3) {
                        System.out.println("[ИСТОРИЯ] " + parts[0] + " | " + parts[1] + " | " + parts[2]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}