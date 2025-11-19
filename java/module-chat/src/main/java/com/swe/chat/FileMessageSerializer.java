package com.swe.chat;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;

public final class FileMessageSerializer {

    private FileMessageSerializer() {}

    /**
     * Helper to write a length-prefixed byte array to the buffer.
     * Writes 0 for null or empty arrays.
     */
    private static void writeBytes(ByteBuffer buffer, byte[] data) {
        if (data == null) {
            buffer.putInt(0); // Write length 0 for null
        } else {
            buffer.putInt(data.length);
            buffer.put(data);
        }
    }

    /**
     * Helper to read a length-prefixed byte array from the buffer.
     * Returns null if length is 0.
     */
    private static byte[] readBytes(ByteBuffer buffer) {
        int len = buffer.getInt();
        if (len <= 0) {
            return null;
        }
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Helper to read a length-prefixed string from the buffer.
     */
    private static String readString(ByteBuffer buffer) {
        byte[] bytes = readBytes(buffer);
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }


    public static byte[] serialize(final FileMessage message) {
        // --- 1. Convert all strings to byte[] *once* ---
        // (Handle nulls to avoid crashes)
        byte[] messageIdBytes = (message.getMessageId() == null) ? null : message.getMessageId().getBytes(StandardCharsets.UTF_8);
        byte[] userIdBytes = (message.getUserId() == null) ? null : message.getUserId().getBytes(StandardCharsets.UTF_8);
        byte[] senderNameBytes = (message.getSenderDisplayName() == null) ? null : message.getSenderDisplayName().getBytes(StandardCharsets.UTF_8);
        byte[] captionBytes = (message.getCaption() == null) ? null : message.getCaption().getBytes(StandardCharsets.UTF_8);
        byte[] fileNameBytes = (message.getFileName() == null) ? null : message.getFileName().getBytes(StandardCharsets.UTF_8);
        byte[] filePathBytes = (message.getFilePath() == null) ? null : message.getFilePath().getBytes(StandardCharsets.UTF_8);
        byte[] fileContentBytes = message.getFileContent(); // This is already a byte[]
        byte[] replyIdBytes = (message.getReplyToMessageId() == null) ? null : message.getReplyToMessageId().getBytes(StandardCharsets.UTF_8);

        long timestampEpoch = message.getTimestamp().toEpochSecond(ZoneOffset.UTC);

        // --- 2. Calculate the exact total size ---
        // (4 bytes for the length-prefix of *each* field)
        int totalSize = 0;
        totalSize += 4 + (messageIdBytes == null ? 0 : messageIdBytes.length);
        totalSize += 4 + (userIdBytes == null ? 0 : userIdBytes.length);
        totalSize += 4 + (senderNameBytes == null ? 0 : senderNameBytes.length);
        totalSize += 4 + (captionBytes == null ? 0 : captionBytes.length);
        totalSize += 4 + (fileNameBytes == null ? 0 : fileNameBytes.length);
        totalSize += 4 + (filePathBytes == null ? 0 : filePathBytes.length);
        totalSize += 4 + (fileContentBytes == null ? 0 : fileContentBytes.length);
        totalSize += 8; // timestamp (long)
        totalSize += 4 + (replyIdBytes == null ? 0 : replyIdBytes.length);

        // --- 3. Allocate buffer and write data sequentially ---
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        writeBytes(buffer, messageIdBytes);
        writeBytes(buffer, userIdBytes);
        writeBytes(buffer, senderNameBytes);
        writeBytes(buffer, captionBytes);
        writeBytes(buffer, fileNameBytes);
        writeBytes(buffer, filePathBytes);
        writeBytes(buffer, fileContentBytes);
        buffer.putLong(timestampEpoch);
        writeBytes(buffer, replyIdBytes);

        return buffer.array();
    }

    public static FileMessage deserialize(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);

        String messageId = readString(buffer);
        String userId = readString(buffer);
        String senderName = readString(buffer);
        String caption = readString(buffer);
        String fileName = readString(buffer);
        String filePath = readString(buffer);
        byte[] fileContent = readBytes(buffer);
        long timestampEpoch = buffer.getLong();
        String replyToId = readString(buffer);

        // Defensive cleaning
        if (filePath != null) {
            filePath = filePath.trim();
            if (filePath.startsWith("*")) {
                filePath = filePath.substring(1).trim();
            }
            if (filePath.isEmpty()) {
                filePath = null;
            }
        }

        // Prioritize filePath detection (Path-Mode) over fileContent (Content-Mode)
        if (filePath != null) {
            // Path-Mode: filePath is present
            return new FileMessage(messageId, userId, senderName, caption, fileName, filePath, replyToId);
        } else {
            // Content-Mode: fileContent is present (or null if it was an empty file)
            return new FileMessage(messageId, userId, senderName, caption, fileName, fileContent, timestampEpoch, replyToId);
        }
    }
}