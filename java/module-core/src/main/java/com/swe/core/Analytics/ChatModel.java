package com.swe.core.analytics;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Model representing a chat message used by analytics components.
 *
 * <p>
 * Contains metadata such as author, timestamp, content and an optional
 * attachment path.
 * </p>
 *
 * <p>
 * Contributed by Kishore.
 * </p>
 */
public class ChatModel {

    /** Unique identifier for the chat message. */
    private String id;

    /** Email of the author. */
    private String author;

    /** Epoch timestamp (milliseconds) when the message was created. */
    private Long timeStamp;

    /** Message content. */
    private String content;

    /** Optional attachment path. */
    private Optional<Path> attachment;

    /**
     * Creates an empty ChatModel with no attachment.
     */
    public ChatModel() {
        this.attachment = Optional.empty();
    }

    // --- Getters ---

    /**
     * Gets the message id.
     *
     * @return the message id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the author email of the message.
     *
     * @return the author email
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Gets the message timestamp in milliseconds since epoch.
     *
     * @return the timestamp
     */
    public Long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Gets the textual content of the message.
     *
     * @return the message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the optional attachment path for this message.
     *
     * @return optional attachment path
     */
    public Optional<Path> getAttachment() {
        return attachment;
    }

    // --- Setters ---

    /**
     * Sets the message id.
     *
     * @param id the message id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the author email for this message.
     *
     * @param author the author email
     */
    public void setAuthor(final String author) {
        this.author = author;
    }

    /**
     * Sets the message timestamp (milliseconds since epoch).
     *
     * @param timeStamp the timestamp
     */
    public void setTimeStamp(final Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Sets the textual content for this message.
     *
     * @param content the message content
     */
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Sets the optional attachment path.
     *
     * @param attachment the optional attachment
     */
    public void setAttachment(final Optional<Path> attachment) {
        this.attachment = attachment;
    }
}
