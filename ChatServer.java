import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final String SECRET_CODE = "SPY2025";
    private static Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("🔐 Secret Spy Chat Server Started on port " + PORT);
        printServerLinks();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("🔗 New connection: " + socket);
                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printServerLinks() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String localIP = localHost.getHostAddress();
            System.out.println("📍 Localhost: 127.0.0.1:" + PORT);
            System.out.println("📶 Network IP: " + localIP + ":" + PORT);
        } catch (UnknownHostException e) {
            System.out.println("⚠ Unable to detect IP.");
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(message);
            }
        }
    }

    public static void register(ClientHandler handler) {
        clientHandlers.add(handler);
        updateUserCount();
    }

    public static void unregister(ClientHandler handler) {
        clientHandlers.remove(handler);
        updateUserCount();
    }

    public static void updateUserCount() {
        String msg = "[SERVER] Active agents: " + clientHandlers.size();
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(msg);
        }
    }
}