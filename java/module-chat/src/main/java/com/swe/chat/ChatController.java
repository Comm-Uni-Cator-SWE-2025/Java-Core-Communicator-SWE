package com.swe.chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
//import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button; // For the reply button (optional, but good)
import javafx.scene.input.MouseButton; // For reply via right-click or other input
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.UUID; // Make sure you have this import at the top
//import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
//import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
//import java.util.UUID; // Import for generating unique message IDs
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * Controller for handling chat UI interactions.
 */

public class ChatController {

    /** Main layout container. */
    @FXML private BorderPane borderPane;

    /** Container for all chat messages. */
    @FXML private VBox messageContainer;

    /** Scrollable container for chat. */
    @FXML private ScrollPane scrollPane;

    /** Input field for typing messages. */
    @FXML private TextField messageInputField;

    /** Handles sending and receiving messages. */
    private ChatManager chatManager;

    /** Simulates networking for chat messages. */
    private MockNetworking mockNetwork; // Keep a reference to the mock network

    /** The ID of the current user. */
    private final String currentUserId = "Aditya-Chauhan";

    private final Map<String, ChatMessage> messageHistory = new ConcurrentHashMap<>();

    /** The message ID currently selected for reply. Null if not replying. */
    private String currentReplyId=null;

    /** The message content of the message being replied to. */
    private String currentReplyContent=null;

    /** New UI element to show the current reply quote above the input field. */
    @FXML private HBox replyQuoteBox;
    @FXML private Label replyQuoteLabel;
    @FXML private Button cancelReplyButton;

    /**
     * Initializes the chat controller.
     * Sets up networking, message manager, and auto-scroll behavior.
     */
    @FXML
    public void initialize() {
        // 1. Use the real MockNetworking your team provided
        this.mockNetwork = new MockNetworking(); // Use the real MockNetworking

        // 2. Initialize the real ChatManager with the mock network
        this.chatManager = new ChatManager(this.mockNetwork);

        // 3. Register a listener to receive messages from the ChatManager
        this.chatManager.setOnMessageReceived(this::handleIncomingMessage);

        replyQuoteBox.setVisible(false);
        replyQuoteBox.setManaged(false);

        // Auto-scroll to the bottom when new messages are added
        messageContainer.heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setVvalue(1.0));
    }

    /**
     * Sets the context for a reply. This is called when a user clicks a 'Reply' option.
     */
    private void startReply(final String messageId, final String messageContent) {
        this.currentReplyId = messageId;
        this.currentReplyContent = messageContent;

        String senderName = "User"; // Default fallback
        final ChatMessage repliedTo = messageHistory.get(messageId);
        if (repliedTo != null) {
            senderName = repliedTo.getUserId().equals(this.currentUserId) ? "Yourself" : repliedTo.getUserId();
        }

        replyQuoteLabel.setText("Replying to: " + messageContent.substring(0, Math.min(messageContent.length(), 30)) + (messageContent.length() > 30 ? "..." : ""));

        // Show the reply quote box
        replyQuoteBox.setVisible(true);
        replyQuoteBox.setManaged(true);
        messageInputField.requestFocus();
    }

    /**
     * Cancel the reply state
     */
    @FXML
    private void cancelReply() {
        this.currentReplyId = null;
        this.currentReplyContent = null;
        replyQuoteLabel.setText("");

        // Hide the reply quote box
        replyQuoteBox.setVisible(false);
        replyQuoteBox.setManaged(false);
    }




    /**
     * This method is triggered by the "Test Recv" button.
     * It now simulates a message coming all the way from the network layer.
     */
    @FXML
    private void simulateIncomingMessage() {
        // 1. Create a fake message from another user
        final String messageId = UUID.randomUUID().toString();
        final ChatMessage fakeMessage =
                new ChatMessage(messageId, "akshay_backend",
                        "Hey, this is a test from the 'network'!",null);

        // 2. Serialize it, just like the real network would
        final String json = MessageParser.serialize(fakeMessage);
        final byte[] data = json.getBytes(StandardCharsets.UTF_8);

        // 3. Use our new simulation method to push the data into the network layer
        mockNetwork.simulateMessageFromServer(data);
    }

    /**
     * Called when the user clicks the "Send" button.
     */
    @FXML
    private void handleSendButtonAction() {
        final String messageText = messageInputField.getText();
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        // 1. Create a proper ChatMessage object with a unique ID
        final String messageId = UUID.randomUUID().toString();
        final ChatMessage messageToSend = new ChatMessage(messageId, this.currentUserId, messageText,this.currentReplyId);

        // 2. Send the message object to the ChatManager
        chatManager.sendMessage(messageToSend);

        // 3. Clear the input field and clear the reply state
        messageInputField.clear();
        cancelReply();


        // NOTE: We no longer need to call displayMessage here. The MockNetwork will
        // loop the message back, and it will be displayed by handleIncomingMessage,
        // just like a real message from another user would.
    }

    /**
     * Handles a new incoming message from the chat manager.
     *
     * @param message the chat message to display
     */
    private void handleIncomingMessage(final ChatMessage message) {
        messageHistory.put(message.getMessageId(),message);

        // Determine if the message was sent by us or someone else
        final boolean isSentByMe = message.getUserId().equals(this.currentUserId);
        final String username = isSentByMe ? "You" : message.getUserId();
        final LocalDateTime timestamp = message.getTimestamp();
        final String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));

        final String replyToId = message.getReplyToMessageId();
        String quotedContent = null;
        if (replyToId != null) {
            final ChatMessage repliedTo = messageHistory.get(replyToId);
            if (repliedTo != null) {
                // Display the original sender's username and a snippet of the original content
                final String originalSender = repliedTo.getUserId().equals(this.currentUserId)
                        ? "You" : repliedTo.getUserId();
                final String originalContent = repliedTo.getContent();

                quotedContent = String.format("REPLY to %s: %s",
                        originalSender,
                        originalContent.substring(0, Math.min(originalContent.length(), 20)) + (originalContent.length() > 20 ? "..." : ""));
            } else {
                // Message was a reply, but we don't have the original (e.g., from an old session)
                quotedContent = "REPLY: Message not found";
            }
        }

        // Display the message on the UI
        displayMessage(username, message.getContent(), formattedTime, isSentByMe,message.getMessageId(),quotedContent);
    }

    /**
     * Builds and displays the message bubble on the UI.
     *
     * @param username  the sender's name
     * @param message   the content of the message
     * @param timestamp the formatted message time
     * @param isSent    whether the message was sent by the current user
     * @param messageId Message Id for reply function
     * @param quotedContent Quoted text for display
     */
    private void displayMessage(final String username,
                                final String message,
                                final String timestamp,
                                final boolean isSent,
                                final String messageId,
                                final String quotedContent
                                ) {
        Platform.runLater(() -> {
            final VBox messageBubble = new VBox();
            messageBubble.getStyleClass().add("message-bubble");

            if (quotedContent != null) {
                final Label quoteLabel = new Label(quotedContent);
                quoteLabel.getStyleClass().addAll("reply-quote-label", "small-text"); // Use a style for quoted text
                messageBubble.getChildren().add(quoteLabel);
            }

            final Label usernameLabel = new Label(username);
            final Label contentLabel = new Label(message);
            final Label timestampLabel = new Label(timestamp);

            final Button replyButton = new Button("Reply");
            replyButton.getStyleClass().add("reply-button");
            replyButton.setOnAction(e -> startReply(messageId, message));

            // Use an HBox for the timestamp and reply button
            final HBox bottomRow = new HBox(5); // 5 is spacing
            bottomRow.setAlignment(Pos.CENTER_RIGHT);
            bottomRow.getChildren().addAll(timestampLabel, replyButton);

            usernameLabel.getStyleClass().add("username-label");
            contentLabel.getStyleClass().add("message-content-label");
            timestampLabel.getStyleClass().add("timestamp-label");

            messageBubble.getChildren().addAll(usernameLabel, contentLabel, bottomRow);
            messageBubble.setAlignment(Pos.CENTER_LEFT);

            final HBox wrapper = new HBox();
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