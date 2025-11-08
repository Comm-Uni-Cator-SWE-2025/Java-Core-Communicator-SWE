package com.swe.core.Analytics;

import java.nio.file.Path;
import java.util.Optional;

public class ChatModel {

    private String id;

    private String author; // email of the author

    private Long timeStamp;

    private String content;

    private Optional<Path> attachment;

    // Constructor (optional)
    public ChatModel() {
        this.attachment = Optional.empty();
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public String getContent() {
        return content;
    }

    public Optional<Path> getAttachment() {
        return attachment;
    }

    // --- Setters ---
    public void setId(String id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAttachment(Optional<Path> attachment) {
        this.attachment = attachment;
    }
}
