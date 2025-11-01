package com.swe.chat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The ONE ViewModel for the ChatView.
 * It holds all UI state and logic, independent of the View.
 * It manages a list of MessageVM (helper) objects.
 */
public class ChatViewModel {

    // --- Model ---
    private final ChatManager chatManager;
    private final MockNetworking mockNetwork; // For the "Test Recv" button
    private final String currentUserId = "Aditya-Chauhan";
    private final Map<String, ChatMessage> messageHistory = new ConcurrentHashMap<>();

    // --- State for View ---
    private Consumer<ChatViewModel.MessageVM> onMessageAdded;
    private Consumer<String> onReplyStateChange; // (String quoteText) ->
    private Runnable onClearInput;             // () ->

    // Internal state for managing replies
    private String currentReplyId = null;

    /**
     * Helper data-transfer-object that holds all pre-formatted data
     * for a single message. The View will use this to draw itself.
     */
    public static class MessageVM {
        private final String messageId;
        private final String username;
        private final String content;
        private final String timestamp;
        private final boolean isSentByMe;
        private final String quotedContent;

        private MessageVM(String messageId, String username, String content, String timestamp, boolean isSentByMe, String quotedContent) {
            this.messageId = messageId;
            this.username = username;
            this.content = content;
            this.timestamp = timestamp;
            this.isSentByMe = isSentByMe;
            this.quotedContent = quotedContent;
        }

        public String getMessageId() { return messageId; }
        public String getUsername() { return username; }
        public String getContent() { return content; }
        public String getTimestamp() { return timestamp; }
        public boolean isSentByMe() { return isSentByMe; }
        public String getQuotedContent() { return quotedContent; }
        public boolean hasQuote() { return quotedContent != null; }
    }

    // --- Constructor ---
    public ChatViewModel() {
        this.mockNetwork = new MockNetworking();
        this.chatManager = new ChatManager(this.mockNetwork);
        this.chatManager.setOnMessageReceived(this::handleIncomingMessage);
    }

    // --- Actions (Called by the View) ---

    public void sendMessage(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        final String messageId = UUID.randomUUID().toString();
        final ChatMessage messageToSend = new ChatMessage(messageId, this.currentUserId, messageText, this.currentReplyId);

        chatManager.sendMessage(messageToSend);

        if (onClearInput != null) {
            onClearInput.run();
        }
        cancelReply();
    }

    public void simulateIncomingMessage() {
        final String messageId = UUID.randomUUID().toString();
        final ChatMessage fakeMessage =
                new ChatMessage(messageId, "akshay_backend",
                        "Hey, this is a test from the 'network'!", null);

        final String json = MessageParser.serialize(fakeMessage);
        final byte[] data = json.getBytes(StandardCharsets.UTF_8);

        mockNetwork.simulateMessageFromServer(data);
    }

    public void startReply(MessageVM messageToReply) {
        if (messageToReply == null) return;
        this.currentReplyId = messageToReply.getMessageId();

        if (onReplyStateChange != null) {
            String quote = "Replying to: " + messageToReply.getContent().substring(0, Math.min(messageToReply.getContent().length(), 30)) + "...";
            onReplyStateChange.accept(quote);
        }
    }

    public void cancelReply() {
        this.currentReplyId = null;
        if (onReplyStateChange != null) {
            onReplyStateChange.accept(null);
        }
    }

    private void handleIncomingMessage(final ChatMessage message) {
        messageHistory.put(message.getMessageId(), message);

        final boolean isSentByMe = message.getUserId().equals(this.currentUserId);
        final String username = isSentByMe ? "You" : message.getUserId();
        final String formattedTime = message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm"));

        String quotedContent = null;
        final String replyToId = message.getReplyToMessageId();
        if (replyToId != null) {
            final ChatMessage repliedTo = messageHistory.get(replyToId);

            // This was the line with the typo
            if (repliedTo != null) {
                final String originalSender = repliedTo.getUserId().equals(this.currentUserId)
                        ? "You" : repliedTo.getUserId();
                quotedContent = String.format("REPLY to %s: %s",
                        originalSender,
                        repliedTo.getContent().substring(0, Math.min(repliedTo.getContent().length(), 20)) + "...");
            } else {
                quotedContent = "REPLY: Message not found";
            }
        }

        MessageVM messageVM = new MessageVM(
                message.getMessageId(),
                username,
                message.getContent(),
                formattedTime,
                isSentByMe,
                quotedContent
        );

        if (onMessageAdded != null) {
            onMessageAdded.accept(messageVM);
        }
    }

    // --- Setters for Callbacks (Used by View) ---

    public void setOnMessageAdded(Consumer<ChatViewModel.MessageVM> onMessageAdded) {
        this.onMessageAdded = onMessageAdded;
    }

    public void setOnReplyStateChange(Consumer<String> onReplyStateChange) {
        this.onReplyStateChange = onReplyStateChange;
    }

    public void setOnClearInput(Runnable onClearInput) {
        this.onClearInput = onClearInput;
    }
}