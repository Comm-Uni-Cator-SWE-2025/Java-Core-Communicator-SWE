/**
 * Contributed by : Sachin(112101052)
 */

package com.swe.chat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents a single chat message.
 * This is the "Data Contract" for all frontends and the core.
 * It is immutable once created.
 */
public class ChatMessage {

    private final String messageId;
    private final String userId;
    private final String senderDisplayName;
    private final String content;
    private final LocalDateTime timestamp;
    private final String replyToMessageId;

    /**
     * Constructor for creating a NEW chat message.
     * Automatically sets the timestamp to 'now' (UTC).
     *
     * @param messageId         Unique ID for the message.
     * @param userId            Stable, unique ID of the sender.
     * @param senderDisplayName The name to show in the UI.
     * @param content           The message text.
     * @param replyToId         The ID of the message being replied to (or null).
     */
    public ChatMessage(final String messageId,
                       final String userId,
                       final String senderDisplayName,
                       final String content,
                       final String replyToId) {

        this.messageId = messageId;
        this.userId = userId;
        this.senderDisplayName = senderDisplayName;
        this.content = content;
        this.replyToMessageId = replyToId;
        // Auto-set timestamp to now in UTC
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Constructor for DESERIALIZATION only.
     * Allows re-creating a message with an existing timestamp.
     *
     * @param messageId         Unique ID for the message.
     * @param userId            Stable, unique ID of the sender.
    //     * @param senderDisplayName The name to show in the UI.
     * @param content           The message text.
     * @param timestampEpoch    The raw timestamp (epoch seconds).
     * @param replyToId         The ID of the message being replied to (or null).
     */
    public ChatMessage(final String messageId,
                       final String userId,
                       final String senderDisplayName,
                       final String content,
                       final long timestampEpoch,
                       final String replyToId) {

        this.messageId = messageId;
        this.userId = userId;
        this.senderDisplayName = senderDisplayName;
        this.content = content;
        this.replyToMessageId = replyToId;
        // Convert the raw long back to a rich LocalDateTime object
        this.timestamp = LocalDateTime.ofEpochSecond(timestampEpoch, 0, ZoneOffset.UTC);
    }

    // --- Getters ---

    public String getMessageId() {
        return messageId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getReplyToMessageId() {
        return replyToMessageId;
    }
}