package com.swe.chat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ChatMessage {
    private String messageId;   // Unique per message
    private String userId;      // Who sent the message
    private String content;
    private long timestamp;

    public ChatMessage(String messageId, String userId,
                       String content) {
        this.messageId = messageId;
        this.userId = userId;
        this.content = content;
        LocalDateTime time_stp = LocalDateTime.now();
        this.timestamp = time_stp.toEpochSecond(ZoneOffset.UTC);
    }

    public String getMessageId() { return messageId; }
    public String getUserId() { return userId; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return LocalDateTime.ofEpochSecond(this.timestamp,0,ZoneOffset.UTC); }
    public void setContent(String new_content) { this.content = new_content; }
}
