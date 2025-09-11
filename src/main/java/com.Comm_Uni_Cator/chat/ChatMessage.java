package com.Comm_Uni_Cator.chat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents a single chat message with ID, user, content, and timestamp.
 */
public class ChatMessage {

    /** Unique identifier for this message. */
    private final String messageId;   // Unique per message

    /** ID of the user who sent the message. */
    private final String userId;      // Who sent the messag

    /** Text content of the message. */
    private String content;

    /** Timestamp when the message was created, in epoch seconds (UTC). */
    private final long timestamp;

    /**
     * Creates a new chat message.
     *
     * @param messageIdd unique ID for the message
     * @param userIdd ID of the user who sent the message
     * @param contentt the message content
     */
    public ChatMessage(final String messageIdd, final String userIdd,
                       final String contentt) {
        this.messageId = messageIdd;
        this.userId = userIdd;
        this.content = contentt;
        final LocalDateTime timestp = LocalDateTime.now();
        this.timestamp = timestp.toEpochSecond(ZoneOffset.UTC);
    }

    /**
     * Returns the unique message ID.
     *
     * @return the unique message ID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Returns the ID of the user who sent this message.
     *
     * @return the ID of the user
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the text content of the message.
     *
     * @return the message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the timestamp of the message as a LocalDateTime (UTC).
     *
     * @return the timestamp in UTC
     */
    public LocalDateTime getTimestamp() {
        return LocalDateTime.ofEpochSecond(this.timestamp, 0, ZoneOffset.UTC);
    }

    /**
     * Updates the content of this message.
     *
     * @param newcontent new content to set
     */
    public void setContent(final String newcontent) {
        this.content = newcontent;
    }
}
