import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("ENTER_SECRET_CODE");
            String code = in.readLine();

            if (!"SPY2025".equals(code)) {
                out.println("ACCESS_DENIED");
                socket.close();
                return;
            }

            out.println("ACCESS_GRANTED");
            out.println("ENTER_NAME");

            username = in.readLine();
            ChatServer.register(this);

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("[" + username + "]: " + line);
                ChatServer.broadcast(username + ": " + line, this);
            }
        } catch (IOException e) {
            System.out.println("⚠️ Connection lost with " + username);
        } finally {
            ChatServer.unregister(this);
            try { socket.close(); } catch (IOException e) { }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
