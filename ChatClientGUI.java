import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClientGUI {
    private BufferedReader in;
    private PrintWriter out;

    private JFrame frame = new JFrame("🕵 Secret Spy Chat");
    private JTextArea chatArea = new JTextArea(20, 50);
    private JTextField inputField = new JTextField(30);
    private JButton sendButton = new JButton("📤 Send");
    private JButton exitButton = new JButton("🚪 Exit");
    private JButton emojiButton = new JButton("😊");

    @SuppressWarnings("resource")
    public ChatClientGUI(String serverIP, int port) {
        try {
            Socket socket = new Socket(serverIP, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            if ("ENTER_SECRET_CODE".equals(in.readLine())) {
                JPanel panel = new JPanel(new GridLayout(2, 2));
                JTextField nameField = new JTextField();
                JPasswordField codeField = new JPasswordField();

                panel.add(new JLabel("🕶 Spy Name:"));
                panel.add(nameField);
                panel.add(new JLabel("🔐 Secret Code:"));
                panel.add(codeField);

                int result = JOptionPane.showConfirmDialog(frame, panel, "Enter Credentials", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    out.println(new String(codeField.getPassword()));
                    String response = in.readLine();

                    if ("ACCESS_GRANTED".equals(response)) {
                        out.println(nameField.getText().trim());
                    } else {
                        JOptionPane.showMessageDialog(frame, "❌ Access Denied!");
                        socket.close();
                        System.exit(0);
                    }
                } else {
                    socket.close();
                    System.exit(0);
                }
            }

            setupGUI();

            // ✅ Message listener thread with final variable fix
            new Thread(() -> {
                try {
                    while (true) {
                        String line = in.readLine();
                        if (line == null) break;
                        if (!isSystemCommand(line)) {
                            String finalLine = line;
                            SwingUtilities.invokeLater(() -> chatArea.append("📡 " + finalLine + "\n"));
                        }
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> chatArea.append("🔌 Disconnected from server.\n"));
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "⚠ Unable to connect to server.");
        }
    }

    private void setupGUI() {
        // Style Chat Area
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);

        chatArea.setBackground(Color.BLACK);
        chatArea.setForeground(Color.GREEN);
        chatArea.setFont(emojiFont);
        chatArea.setEditable(false);
        chatArea.setCaretColor(Color.GREEN);

        inputField.setBackground(Color.BLACK);
        inputField.setForeground(Color.GREEN);
        inputField.setFont(emojiFont);
        inputField.setCaretColor(Color.GREEN);
        inputField.setBorder(BorderFactory.createLineBorder(Color.GREEN));

        sendButton.setBackground(Color.DARK_GRAY);
        sendButton.setForeground(Color.CYAN);
        sendButton.setFont(new Font("Consolas", Font.BOLD, 14));
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createLineBorder(Color.CYAN));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        exitButton.setBackground(Color.DARK_GRAY);
        exitButton.setForeground(Color.RED);
        exitButton.setFont(new Font("Consolas", Font.BOLD, 14));
        exitButton.setFocusPainted(false);
        exitButton.setBorder(BorderFactory.createLineBorder(Color.RED));
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        emojiButton.setBackground(Color.DARK_GRAY);
        emojiButton.setForeground(Color.ORANGE);
        emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        emojiButton.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        emojiButton.setFocusPainted(false);
        emojiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GREEN));

        frame.getContentPane().setBackground(Color.DARK_GRAY);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.DARK_GRAY);
        bottomPanel.add(emojiButton);
        bottomPanel.add(inputField);
        bottomPanel.add(sendButton);
        bottomPanel.add(exitButton);

        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        inputField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());
        exitButton.addActionListener(e -> {
            closeConnection();
            System.exit(0);
        });
        emojiButton.addActionListener(e -> showEmojiPopup(emojiButton));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.trim().isEmpty()) {
            out.println(text);
            inputField.setText("");
        }
    }

    private boolean isSystemCommand(String msg) {
        return msg.equals("ENTER_SECRET_CODE") || msg.equals("ACCESS_GRANTED") || msg.equals("ACCESS_DENIED") || msg.equals("ENTER_NAME");
    }

    private void closeConnection() {
        try {
            out.close();
            in.close();
        } catch (Exception ignored) {}
    }

    private void showEmojiPopup(Component parent) {
        JPopupMenu emojiMenu = new JPopupMenu();
        String[] emojis = {"😀", "😂", "😍", "😎", "😭", "😡", "👍", "👀", "🔥", "🎉"};

        JPanel emojiPanel = new JPanel(new GridLayout(3, 4));
        emojiPanel.setBackground(Color.DARK_GRAY);

        for (String emoji : emojis) {
            JButton emojiBtn = new JButton(emoji);
            emojiBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            emojiBtn.setBackground(Color.BLACK);
            emojiBtn.setForeground(Color.WHITE);
            emojiBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            emojiBtn.setFocusPainted(false);
            emojiBtn.addActionListener(e -> {
                inputField.setText(inputField.getText() + emoji);
                emojiMenu.setVisible(false);
            });
            emojiPanel.add(emojiBtn);
        }

        emojiMenu.add(emojiPanel);
        emojiMenu.show(parent, 0, parent.getHeight());
    }

    public static void main(String[] args) {
        String serverIP = JOptionPane.showInputDialog("Enter Server IP:");
        if (serverIP != null && !serverIP.trim().isEmpty()) {
            new ChatClientGUI(serverIP.trim(), 12345);
        }
    }
}