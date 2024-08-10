package zverzhik.consol.chat;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthenticationProvider authenticationProvider;

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.authenticationProvider = new InMemoryAuthenticationProvider(this);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            authenticationProvider.initialize();
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this,socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe( ClientHandler clientHandler) {
        broadcastMessage("В чат зашел: " + clientHandler.getUsername());
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe( ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Из чата вышел: " + clientHandler.getUsername());
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void sendPrivateMessage( ClientHandler sender, String message) {
        String[] messageArray = message.split(" ", 3);
        if (messageArray.length != 3) {
            sender.sendMessage("Неверный формат! Введите: /w Имя получателя Сообщение");
            return;
        }
        String recipient = messageArray[1].trim();
        String privateMessage = messageArray[2];
        if (!this.isUsernameBusy(recipient)) {
            sender.sendMessage("Пользователя " + recipient + " не существует!");
            return;
        }
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(recipient)) {
                c.sendMessage("Вам пришло личное сообщение от " + sender.getUsername() + ": " + privateMessage);
                sender.sendMessage("Личное сообщение для " + recipient + " отправлено.");
                return;
            }
        }
    }

    public synchronized void kickUser(ClientHandler sender, String message) {
        String[] messageArray = message.split(" ", 2);
        if (messageArray.length != 2) {
            sender.sendMessage("Неверный формат! Используйте: /kick Имя пользователя");
            return;
        }
        String kickedUser = messageArray[1].trim();
        if (!this.isUsernameBusy(kickedUser)) {
            sender.sendMessage("Пользователя '" + kickedUser + "' не существует!");
            return;
        }
        if (!this.authenticationProvider.isUserAdmin(sender.getUsername())) {
            sender.sendMessage("Отключить пользователя " + kickedUser + " может только админ");
            return;
        }
        if (sender.getUsername().equals(kickedUser)) {
            sender.sendMessage("Невозможно отключить самого себя!");
            return;
        }
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(kickedUser)) {
                c.sendMessage("Администратор " + sender.getUsername() + " отключил вас!");
                clients.remove(c);
                this.broadcastMessage("Пользователь " + kickedUser + " был отключен администратором " +
                        sender.getUsername());
                return;
            }
        }
    }

    public synchronized boolean isUsernameBusy(String username) {
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
