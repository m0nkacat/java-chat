package main.java.chatapp.client;

import main.java.chatapp.common.Protocol;
import java.util.Scanner;

public class ClientMenu {
    private ChatClientInterface client;
    private Scanner scanner;

    public ClientMenu(ChatClientInterface client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        showAuthMenu();
    }

    private void showAuthMenu() {
        System.out.println("1. Вход");
        System.out.println("2. Регистрация");
        System.out.println("3. Выход");
        System.out.print("Выбор: ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1": doLogin(); break;
            case "2": doRegister(); break;
            default: System.exit(0);
        }
    }

    private void doLogin() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String pass = scanner.nextLine();
        client.sendCommand(Protocol.CMD_AUTH + " " + login + " " + pass);
        System.out.println("Ожидание ответа...");
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        showChannelMenu();
    }

    private void doRegister() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String pass = scanner.nextLine();
        System.out.print("Отображаемое имя: ");
        String nick = scanner.nextLine();
        client.sendCommand(Protocol.CMD_REG + " " + login + " " + pass + " " + nick);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println("Теперь войдите.");
        showAuthMenu();
    }

    private void showChannelMenu() {
        while (true) {
            System.out.println("1. #general");
            System.out.println("2. #random");
            System.out.println("3. #help");
            System.out.println("4. Личный чат с пользователем");
            System.out.println("5. Выход в главное меню");
            System.out.print("Выбор: ");
            String ch = scanner.nextLine();
            switch (ch) {
                case "1": client.sendCommand(Protocol.CMD_JOIN + " #general"); startChat(); return;
                case "2": client.sendCommand(Protocol.CMD_JOIN + " #random"); startChat(); return;
                case "3": client.sendCommand(Protocol.CMD_JOIN + " #help"); startChat(); return;
                case "4": startPrivateChat(); break;
                case "5": showAuthMenu(); return;
                default: System.out.println("Неверный выбор");
            }
        }
    }

    private void startPrivateChat() {
        System.out.print("Введите никнейм пользователя: ");
        String target = scanner.nextLine();
        client.sendCommand(Protocol.CMD_OPEN_PRIVATE + " " + target);
        System.out.println("Ожидание подтверждения...");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        startChat();
    }

    private void startChat() {
        client.setInChat(true);
        System.out.println("Введите /exit для выхода в меню");
        while (client.isInChat()) {
            String input = scanner.nextLine();
            if (input.equals("/exit")) {
                client.sendCommand(Protocol.CMD_LOGOUT);
                client.setInChat(false);
            } else {
                client.sendCommand(Protocol.CMD_MSG + " " + input);
            }
        }
        showChannelMenu();
    }
}