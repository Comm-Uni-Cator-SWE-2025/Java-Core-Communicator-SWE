package com.swe.chat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Model for File Messages supporting two modes:
 * 1. Path-Mode — used for sending file paths from frontend
 * 2. Content-Mode — used internally after reading/compressing file
 */
public class FileMessage {
    private final String messageId;
    private final String userId;
    private final String senderDisplayName;
    private final LocalDateTime timestamp;
    private final String replyToMessageId;

    // Content fields
    private final String caption;       // Caption or message with file
    private final String fileName;

    // State-specific fields
    private final String filePath;      // Path from frontend
    private final byte[] fileContent;   // Compressed content for network

    // Constructor for Path-Mode (Frontend → Core)
    public FileMessage(String messageId, String userId, String senderDisplayName,
                       String caption, String fileName, String filePath, String replyToId) {
        this.messageId = messageId;
        this.userId = userId;
        this.senderDisplayName = senderDisplayName;
        this.caption = caption;
        this.fileName = fileName;
        this.filePath = filePath;
        this.replyToMessageId = replyToId;
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
        this.fileContent = null; // Not used in Path Mode
    }

    // Constructor for Content-Mode (Core → Network)
    public FileMessage(String messageId, String userId, String senderDisplayName,
                       String caption, String fileName, byte[] fileContent,
                       long timestampEpoch, String replyToId) {
        this.messageId = messageId;
        this.userId = userId;
        this.senderDisplayName = senderDisplayName;
        this.caption = caption;
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.replyToMessageId = replyToId;
        this.timestamp = LocalDateTime.ofEpochSecond(timestampEpoch, 0, ZoneOffset.UTC);
        this.filePath = null; // Not used in Content Mode
    }

    // Getters
    public String getMessageId() { return messageId; }
    public String getUserId() { return userId; }
    public String getSenderDisplayName() { return senderDisplayName; }
    public String getCaption() { return caption; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public byte[] getFileContent() { return fileContent; }
    public String getReplyToMessageId() { return replyToMessageId; }
    public LocalDateTime getTimestamp() { return timestamp; }
}