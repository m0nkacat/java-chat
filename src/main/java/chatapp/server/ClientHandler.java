package main.java.chatapp.server;

import main.java.chatapp.common.Protocol;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UserManager userManager;
    private ChannelManager channelManager;
    private HistoryManager historyManager;
    private ConcurrentHashMap<String, PrintWriter> onlineUsers;
    private String currentUserNick;
    private String currentChannel;
    private String currentPrivateWith;

    public ClientHandler(Socket socket, UserManager userManager, ChannelManager channelManager,
                         HistoryManager historyManager, ConcurrentHashMap<String, PrintWriter> onlineUsers) {
        this.socket = socket;
        this.userManager = userManager;
        this.channelManager = channelManager;
        this.historyManager = historyManager;
        this.onlineUsers = onlineUsers;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String input;
            while ((input = in.readLine()) != null) {
                System.out.println("[DEBUG] Получено: " + input);
                String[] parts = input.split(" ");
                if (parts.length == 0) continue;
                String cmd = parts[0];

                switch (cmd) {
                    case Protocol.CMD_REG:
                        if (parts.length >= 4) {
                            String login = parts[1];
                            String password = parts[2];
                            String nickname = input.substring(cmd.length() + login.length() + password.length() + 3);
                            handleReg(login, password, nickname);
                        } else {
                            out.println(Protocol.RES_ERR + " Неверный формат REG");
                        }
                        break;
                    case Protocol.CMD_AUTH:
                        if (parts.length >= 3) {
                            handleAuth(parts[1], parts[2]);
                        } else {
                            out.println(Protocol.RES_ERR + " Неверный формат AUTH");
                        }
                        break;
                    case Protocol.CMD_JOIN:
                        if (parts.length >= 2) {
                            handleJoin(parts[1]);
                        } else {
                            out.println(Protocol.RES_ERR + " Укажите канал");
                        }
                        break;
                    case Protocol.CMD_MSG:
                        if (parts.length >= 2) {
                            String text = input.substring(cmd.length() + 1);
                            handleMessage(text);
                        }
                        break;
                    case Protocol.CMD_SWITCH:
                        if (parts.length >= 2) handleSwitch(parts[1]);
                        break;
                    case Protocol.CMD_OPEN_PRIVATE:
                        if (parts.length >= 2) handleOpenPrivate(parts[1]);
                        break;
                    case Protocol.CMD_PRIVATE:
                        if (parts.length >= 3) {
                            String target = parts[1];
                            String text = input.substring(cmd.length() + target.length() + 2);
                            handlePrivate(target, text);
                        }
                        break;
                    case Protocol.CMD_HISTORY:
                        if (parts.length >= 2) handleHistory(parts[1]);
                        break;
                    case Protocol.CMD_LOGOUT:
                        currentChannel = null;
                        currentPrivateWith = null;
                        out.println(Protocol.RES_OK + " Вы вышли из чата");
                        break;
                    default:
                        out.println(Protocol.RES_ERR + " Неизвестная команда");
                }
            }
        } catch (IOException e) {
            System.out.println("Клиент отключился: " + currentUserNick);
        } finally {
            disconnect();
        }
    }

    private void handleReg(String login, String password, String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            out.println(Protocol.RES_ERR + " Требуется отображаемое имя");
            return;
        }
        if (userManager.register(login, password, nickname)) {
            out.println(Protocol.RES_OK + " Регистрация успешна");
        } else {
            out.println(Protocol.RES_ERR + " Логин уже существует");
        }
    }

    private void handleAuth(String login, String password) {
        String nick = userManager.authenticate(login, password);
        if (nick != null) {
            currentUserNick = nick;
            onlineUsers.put(nick, out);
            out.println(Protocol.RES_OK + " AUTH " + nick);
            List<String> offlineMsgs = historyManager.getOfflinePrivateMessages(nick);
            for (String msg : offlineMsgs) {
                out.println(Protocol.RES_PRIVATE_MSG + " " + msg);
            }
            out.println(Protocol.RES_USER_LIST + " " + String.join(",", onlineUsers.keySet()));
        } else {
            out.println(Protocol.RES_ERR + " Неверный логин или пароль");
        }
    }

    private void handleJoin(String channelName) {
        if (currentUserNick == null) {
            out.println(Protocol.RES_ERR + " Вы не авторизованы");
            return;
        }
        currentChannel = channelName;
        currentPrivateWith = null;
        out.println(Protocol.RES_OK + " Вы присоединились к " + channelName);
        List<String> history = historyManager.getChannelHistory(channelName, 3);
        out.println(Protocol.RES_CHANNEL_HISTORY + " " + channelName);
        for (String line : history) out.println(line);
        out.println(Protocol.RES_END_HISTORY);
    }

    private void handleMessage(String text) {
        if (currentUserNick == null) {
            out.println(Protocol.RES_ERR + " Не авторизован");
            return;
        }
        String timestamp = getTimestamp();
        if (currentPrivateWith != null) {
            String fullMsg = currentUserNick + Protocol.SEP + timestamp + Protocol.SEP + text;
            PrintWriter targetOut = onlineUsers.get(currentPrivateWith);
            if (targetOut != null) {
                targetOut.println(Protocol.RES_PRIVATE_MSG + " " + fullMsg);
                historyManager.logPrivateMessage(currentUserNick, currentPrivateWith, fullMsg);
            } else {
                historyManager.saveOfflinePrivateMessage(currentPrivateWith, fullMsg);
                out.println(Protocol.RES_ERR + " Пользователь не в сети, сообщение сохранено");
            }
        } else if (currentChannel != null) {
            String fullMsg = currentUserNick + Protocol.SEP + timestamp + Protocol.SEP + text;
            channelManager.broadcastToChannel(currentChannel, currentUserNick, fullMsg, onlineUsers);
            historyManager.logChannelMessage(currentChannel, fullMsg);
        } else {
            out.println(Protocol.RES_ERR + " Вы не в канале и не в личном чате");
        }
    }

    private void handleSwitch(String newChannel) {
        if (currentUserNick == null) return;
        currentChannel = newChannel;
        currentPrivateWith = null;
        out.println(Protocol.RES_OK + " Переключились на " + newChannel);
    }

    private void handleOpenPrivate(String targetNick) {
        if (currentUserNick == null) {
            out.println(Protocol.RES_ERR + " Вы не авторизованы");
            return;
        }
        if (!userManager.userExists(targetNick)) {
            out.println(Protocol.RES_ERR + " Пользователь " + targetNick + " не найден");
            return;
        }
        currentPrivateWith = targetNick;
        currentChannel = null;
        out.println(Protocol.RES_OK + " Личный чат с " + targetNick);
        List<String> history = historyManager.getPrivateHistory(currentUserNick, targetNick, 3);
        out.println(Protocol.RES_CHANNEL_HISTORY + " PRIVATE " + targetNick);
        for (String line : history) out.println(line);
        out.println(Protocol.RES_END_HISTORY);
    }

    private void handlePrivate(String targetNick, String text) {
        if (currentUserNick == null) return;
        String timestamp = getTimestamp();
        String fullMsg = currentUserNick + Protocol.SEP + timestamp + Protocol.SEP + text;
        PrintWriter targetOut = onlineUsers.get(targetNick);
        if (targetOut != null) {
            targetOut.println(Protocol.RES_PRIVATE_MSG + " " + fullMsg);
            historyManager.logPrivateMessage(currentUserNick, targetNick, fullMsg);
        } else {
            historyManager.saveOfflinePrivateMessage(targetNick, fullMsg);
            out.println(Protocol.RES_ERR + " Пользователь не в сети, сообщение сохранено");
        }
    }

    private void handleHistory(String target) {
        if (target.startsWith("#")) {
            List<String> hist = historyManager.getChannelHistory(target, 3);
            out.println(Protocol.RES_CHANNEL_HISTORY + " " + target);
            for (String s : hist) out.println(s);
            out.println(Protocol.RES_END_HISTORY);
        } else {
            out.println(Protocol.RES_ERR + " Используйте OPEN_PRIVATE для просмотра личной истории");
        }
    }

    private void disconnect() {
        if (currentUserNick != null) onlineUsers.remove(currentUserNick);
        try { socket.close(); } catch (IOException e) {}
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}