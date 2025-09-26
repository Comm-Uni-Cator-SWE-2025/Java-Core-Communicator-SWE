package chatsummary;

import java.time.LocalDateTime;

/**
 * Represents one chat message with sender, text, and timestamp.
 */
public class MeetingMessage {
    /** The sender of the message. */
    private final String sender;
    /** The text content of the message. */
    private final String text;
    /** The timestamp when the message was sent. */
    private final LocalDateTime timestamp;

    /**
     * Constructor for MeetingMessage.
     *
     * @param senderName the sender of the message
     * @param messageText the text content of the message
     * @param messageTimestamp the timestamp when the message was sent
     */
    public MeetingMessage(final String senderName, final String messageText, final LocalDateTime messageTimestamp) {
        this.sender = senderName;
        this.text = messageText;
        this.timestamp = messageTimestamp;
    }

    /**
     * Gets the sender of the message.
     *
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the text content of the message.
     *
     * @return the text content
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the timestamp of the message.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return sender + " (" + timestamp + "): " + text;
    }
}