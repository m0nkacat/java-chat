package main.java.chatapp.server;

import main.java.chatapp.common.Protocol;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {
    private static final String[] CHANNELS = {"#general", "#random", "#help"};

    public void broadcastToChannel(String channel, String senderNick, String fullMessage,
                                   ConcurrentHashMap<String, PrintWriter> onlineUsers) {
        String formatted = Protocol.RES_CHANNEL_MSG + " " + channel + " " + fullMessage;
        for (PrintWriter pw : onlineUsers.values()) {
            pw.println(formatted);
        }
    }
}