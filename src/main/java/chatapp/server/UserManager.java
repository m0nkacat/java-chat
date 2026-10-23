package main.java.chatapp.server;

import main.java.chatapp.common.Protocol;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserManager {
    private Map<String, User> users = new HashMap<>();
    private final String USER_FILE = "users.txt";
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public UserManager() { loadUsers(); }

    private void loadUsers() {
        lock.writeLock().lock();
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\" + Protocol.SEP);
                if (parts.length == 3) users.put(parts[0], new User(parts[0], parts[1], parts[2]));
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) { e.printStackTrace(); }
        finally { lock.writeLock().unlock(); }
    }

    private void saveUsers() {
        lock.writeLock().lock();
        try (PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE))) {
            for (User u : users.values())
                pw.println(u.login + Protocol.SEP + u.password + Protocol.SEP + u.nickname);
        } catch (IOException e) { e.printStackTrace(); }
        finally { lock.writeLock().unlock(); }
    }

    public boolean register(String login, String password, String nickname) {
        lock.writeLock().lock();
        try {
            if (users.containsKey(login)) return false;
            for (User u : users.values()) {
                if (u.nickname.equals(nickname)) return false;
            }
            users.put(login, new User(login, password, nickname));
            saveUsers();
            return true;
        } finally { lock.writeLock().unlock(); }
    }

    public String authenticate(String login, String password) {
        lock.readLock().lock();
        try {
            User u = users.get(login);
            if (u != null && u.password.equals(password)) return u.nickname;
            return null;
        } finally { lock.readLock().unlock(); }
    }

    public boolean userExists(String nickname) {
        lock.readLock().lock();
        try {
            for (User u : users.values()) {
                if (u.nickname.equals(nickname)) return true;
            }
            return false;
        } finally { lock.readLock().unlock(); }
    }

    private static class User {
        String login, password, nickname;
        User(String l, String p, String n) { login=l; password=p; nickname=n; }
    }
}