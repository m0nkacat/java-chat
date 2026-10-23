package main.java.chatapp.client;

public interface ChatClientInterface {
    void sendCommand(String cmd);
    void setMyNick(String nick);
    void setInChat(boolean inChat);
    boolean isInChat();
    void stop();
}