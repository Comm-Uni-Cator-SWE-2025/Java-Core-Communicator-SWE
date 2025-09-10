package com.Comm_Uni_Cator.chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.UUID; // Make sure you have this import at the top
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID; // Import for generating unique message IDs

public class ChatController {

    @FXML private BorderPane borderPane;
    @FXML private VBox messageContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInputField;

    private ChatManager chatManager;
    private MockNetworking mockNetwork; // Keep a reference to the mock network
    private final String currentUserId = "Aditya-Chauhan";

    @FXML
    public void initialize() {
        // 1. Use the real MockNetworking your team provided
        this.mockNetwork = new MockNetworking(); // Use the real MockNetworking

        // 2. Initialize the real ChatManager with the mock network
        this.chatManager = new ChatManager(this.mockNetwork);

        // 3. Register a listener to receive messages from the ChatManager
        this.chatManager.setOnMessageReceived(this::handleIncomingMessage);

        // Auto-scroll to the bottom when new messages are added
        messageContainer.heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setVvalue(1.0));
    }


    /**
     * This method is triggered by the "Test Recv" button.
     * It now simulates a message coming all the way from the network layer.
     */
    @FXML
    private void simulateIncomingMessage() {
        // 1. Create a fake message from another user
        String messageId = UUID.randomUUID().toString();
        ChatMessage fakeMessage = new ChatMessage(messageId, "akshay_backend", "Hey, this is a test from the 'network'!");

        // 2. Serialize it, just like the real network would
        String json = MessageParser.serialize(fakeMessage);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        // 3. Use our new simulation method to push the data into the network layer
        mockNetwork.simulateMessageFromServer(data);
    }

    /**
     * Called when the user clicks the "Send" button.
     */
    @FXML
    private void handleSendButtonAction() {
        String messageText = messageInputField.getText();
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        // 1. Create a proper ChatMessage object with a unique ID
        String messageId = UUID.randomUUID().toString();
        ChatMessage messageToSend = new ChatMessage(messageId, this.currentUserId, messageText);

        // 2. Send the message object to the ChatManager
        chatManager.sendMessage(messageToSend);

        // 3. Clear the input field
        messageInputField.clear();

        // NOTE: We no longer need to call displayMessage here. The MockNetwork will
        // loop the message back, and it will be displayed by handleIncomingMessage,
        // just like a real message from another user would.
    }

    /**
     * This method is the listener for the ChatManager. It's called when any new message arrives.
     */
    private void handleIncomingMessage(ChatMessage message) {
        // Determine if the message was sent by us or someone else
        boolean isSentByMe = message.getUserId().equals(this.currentUserId);

        String username = isSentByMe ? "You" : message.getUserId();

        // Convert timestamp to a readable format
        LocalDateTime timestamp = message.getTimestamp();
        String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));

        // Display the message on the UI
        displayMessage(username, message.getContent(), formattedTime, isSentByMe);
    }

    /**
     * This private helper method builds the visual representation of a message.
     */
    private void displayMessage(String username, String message, String timestamp, boolean isSent) {
        Platform.runLater(() -> {
            VBox messageBubble = new VBox();
            messageBubble.getStyleClass().add("message-bubble");

            Label usernameLabel = new Label(username);
            Label contentLabel = new Label(message);
            Label timestampLabel = new Label(timestamp);

            usernameLabel.getStyleClass().add("username-label");
            contentLabel.getStyleClass().add("message-content-label");
            timestampLabel.getStyleClass().add("timestamp-label");

            messageBubble.getChildren().addAll(usernameLabel, contentLabel, timestampLabel);
            messageBubble.setAlignment(Pos.CENTER_LEFT);

            HBox wrapper = new HBox();
            if (isSent) {
                messageBubble.getStyleClass().add("sent-bubble");
                wrapper.setAlignment(Pos.CENTER_RIGHT);
            } else {
                messageBubble.getStyleClass().add("received-bubble");
                wrapper.setAlignment(Pos.CENTER_LEFT);
            }

            wrapper.getChildren().add(messageBubble);
            messageContainer.getChildren().add(wrapper);
        });
    }
}