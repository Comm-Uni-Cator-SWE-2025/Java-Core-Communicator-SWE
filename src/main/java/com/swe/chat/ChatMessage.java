package com.swe.chat;

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

    /** The ID of the message this message is replying to*/
    private final String replyToMessageId;

    /**
     * Creates a new chat message.
     *
     * @param messageIdd unique ID for the message
     * @param userIdd ID of the user who sent the message
     * @param contentt the message content
     * @param replyToId ID of the message being replied to, or null
     */
    public ChatMessage(final String messageIdd, final String userIdd,
                       final String contentt,final String replyToId) {
        this.messageId = messageIdd;
        this.userId = userIdd;
        this.content = contentt;
        final LocalDateTime timestp = LocalDateTime.now();
        this.timestamp = timestp.toEpochSecond(ZoneOffset.UTC);
        this.replyToMessageId=replyToId;
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
     * Returns the message id to whose message is replied
     *
     * @return messageId
     */
    public String getReplyToMessageId(){
        return replyToMessageId;
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
