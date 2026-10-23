package main.java.chatapp.server;

import main.java.chatapp.common.Protocol;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoryManager {
    private static final String CHANNEL_LOG_DIR = "logs/channels/";
    private static final String PRIVATE_LOG_DIR = "logs/private/";
    private static final String OFFLINE_DIR = "logs/offline/";

    public HistoryManager() {
        new File(CHANNEL_LOG_DIR).mkdirs();
        new File(PRIVATE_LOG_DIR).mkdirs();
        new File(OFFLINE_DIR).mkdirs();
    }

    public void logChannelMessage(String channel, String formattedMsg) {
        String filename = CHANNEL_LOG_DIR + channel.substring(1) + ".log";
        appendToFile(filename, formattedMsg);
    }

    public List<String> getChannelHistory(String channel, int daysBack) {
        String filename = CHANNEL_LOG_DIR + channel.substring(1) + ".log";
        return filterLastDays(new File(filename), daysBack);
    }

    public void logPrivateMessage(String from, String to, String formattedMsg) {
        String filename = getPrivateLogFile(from, to);
        appendToFile(filename, formattedMsg);
    }

    public List<String> getPrivateHistory(String user1, String user2, int daysBack) {
        String filename = getPrivateLogFile(user1, user2);
        return filterLastDays(new File(filename), daysBack);
    }

    public void saveOfflinePrivateMessage(String targetNick, String formattedMsg) {
        String filename = OFFLINE_DIR + targetNick + ".txt";
        appendToFile(filename, formattedMsg);
    }

    public List<String> getOfflinePrivateMessages(String nick) {
        File f = new File(OFFLINE_DIR + nick + ".txt");
        List<String> msgs = readAllLines(f);
        f.delete();
        return msgs;
    }

    private String getPrivateLogFile(String userA, String userB) {
        List<String> list = Arrays.asList(userA, userB);
        Collections.sort(list);
        return PRIVATE_LOG_DIR + list.get(0) + "_" + list.get(1) + ".log";
    }

    private void appendToFile(String filename, String line) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
            pw.println(line);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private List<String> filterLastDays(File file, int daysBack) {
        List<String> result = new ArrayList<>();
        if (!file.exists()) return result;
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysBack);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\" + Protocol.SEP);
                if (parts.length >= 2) {
                    try {
                        LocalDateTime msgTime = LocalDateTime.parse(parts[1], fmt);
                        if (msgTime.isAfter(cutoff)) result.add(line);
                    } catch (Exception ignored) {}
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return result;
    }

    private List<String> readAllLines(File f) {
        List<String> lines = new ArrayList<>();
        if (!f.exists()) return lines;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        } catch (IOException e) {}
        return lines;
    }
}