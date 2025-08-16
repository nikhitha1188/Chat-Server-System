import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ChatClient extends Application {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private VBox messagesBox;
    private TextField inputField;
    private VBox userList;
    private String name;
    private String serverIP = "127.0.0.1";
    private int port = 12345;

    private ContextMenu emojiMenu;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        showLoginScreen(stage);
    }

    private void showLoginScreen(Stage stage) {
        Label title = new Label("🕵️‍♂️ Secret Spy Chat");
        title.setId("titleLabel");

        Label nameLabel = new Label("Username:");
        TextField nameField = new TextField();

        Label codeLabel = new Label("Secret Code:");
        PasswordField codeField = new PasswordField();

        Button joinButton = new Button("Enter Secure Channel");
        joinButton.setDefaultButton(true);

        VBox loginBox = new VBox(10, title, nameLabel, nameField, codeLabel, codeField, joinButton);
        loginBox.setPadding(new Insets(30));
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setMaxWidth(400);
        loginBox.setMaxHeight(350);

        BackgroundImage bg = new BackgroundImage(
                new Image("file:resources/spy_bg.gif", 800, 500, false, true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
        );
        loginBox.setBackground(new Background(bg));

        Scene loginScene = new Scene(loginBox, 800, 500);
        loginScene.getStylesheets().add("file:resources/spy-theme.css");

        joinButton.setOnAction(e -> {
            name = nameField.getText().trim();
            String code = codeField.getText().trim();
            if (!name.isEmpty() && !code.isEmpty()) {
                connectToServer(code, stage);
            }
        });

        stage.setScene(loginScene);
        stage.setTitle("Spy Network Login");
        stage.show();
    }

    private void connectToServer(String code, Stage stage) {
        try {
            socket = new Socket(serverIP, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    if ("ENTER_SECRET_CODE".equals(in.readLine())) {
                        out.println(code);
                        String response = in.readLine();

                        if ("ACCESS_GRANTED".equals(response)) {
                            out.println(name);
                            Platform.runLater(() -> launchChatUI(stage));
                            listenForMessages();
                        } else {
                            socket.close();
                            Platform.runLater(() -> showAlert("Access Denied", "Incorrect secret code."));
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert("Error", "Failed to connect."));
                }
            }).start();

        } catch (IOException e) {
            showAlert("Connection Error", "Unable to connect to server.");
        }
    }

    private void launchChatUI(Stage stage) {
        messagesBox = new VBox(5);
        messagesBox.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1e1e1e;");

        inputField = new TextField();
        inputField.setPromptText("Type your secret message...");
        inputField.setOnAction(e -> sendMessage());

        // Emoji Button and Styled Picker
        Button emojiBtn = new Button("😊");
        emojiBtn.setStyle("-fx-font-size: 16px;");

        emojiMenu = new ContextMenu();
        emojiMenu.setStyle("-fx-background-color: black;");

        String[] emojis = {"😀", "😂", "😍", "😎", "😭", "😡", "👍", "👀", "🔥", "🎉"};
        for (String emoji : emojis) {
            MenuItem item = new MenuItem(emoji);
            item.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 18px;");
            item.setOnAction(e -> inputField.appendText(emoji));
            emojiMenu.getItems().add(item);
        }

        emojiBtn.setOnAction(e -> emojiMenu.show(emojiBtn, Side.TOP, 0, 0));

        Button sendBtn = new Button("Send");
        sendBtn.setOnAction(e -> sendMessage());

        HBox inputArea = new HBox(10, inputField, emojiBtn, sendBtn);
        inputArea.setPadding(new Insets(10));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color: #2a2a2a;");

        userList = new VBox(10);
        userList.setPadding(new Insets(10));
        userList.setStyle("-fx-background-color: #1f1f1f;");
        Label usersLabel = new Label("🟢 Agents Online");
        usersLabel.setStyle("-fx-text-fill: lime;");
        userList.getChildren().add(usersLabel);

        Button leaveBtn = new Button("Leave Chat");
        leaveBtn.setOnAction(e -> {
            closeConnection();
            Platform.exit();
        });

        ImageView torch = new ImageView(new Image("file:resources/torches.gif"));
        torch.setFitHeight(120);
        torch.setFitWidth(60);

        VBox leftPane = new VBox(10, torch, userList);
        leftPane.setAlignment(Pos.TOP_CENTER);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(scrollPane);
        mainLayout.setBottom(new VBox(inputArea, leaveBtn));
        mainLayout.setLeft(leftPane);

        BackgroundImage bg = new BackgroundImage(
                new Image("file:resources/spy_bg.gif", 800, 500, false, true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
        );
        mainLayout.setBackground(new Background(bg));

        Scene scene = new Scene(mainLayout, 800, 500);
        scene.getStylesheets().add("file:resources/spy-theme.css");

        stage.setScene(scene);
        stage.setTitle("Secret Spy Chat");
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            out.println(msg);
            inputField.clear();
        }
    }

    private void listenForMessages() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                String finalMsg = msg;

                if (finalMsg.equals("ENTER_SECRET_CODE") ||
                    finalMsg.equals("ACCESS_GRANTED") ||
                    finalMsg.equals("ACCESS_DENIED") ||
                    finalMsg.equals("ENTER_NAME")) {
                    continue;
                }

                Platform.runLater(() -> displayMessage(finalMsg));
            }
        } catch (IOException e) {
            Platform.runLater(() -> displayMessage("🔌 Connection lost..."));
        }
    }

    private void displayMessage(String message) {
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.getStyleClass().add("chat-message");
        messagesBox.getChildren().add(msgLabel);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
