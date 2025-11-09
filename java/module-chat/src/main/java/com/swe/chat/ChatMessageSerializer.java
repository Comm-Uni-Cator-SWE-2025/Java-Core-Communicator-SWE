package com.swe.chat;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Handles custom binary serialization and deserialization for ChatMessage objects.
 * This ensures cross-platform compatibility between Java and .NET.
 */
public final class ChatMessageSerializer {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ChatMessageSerializer() {
    }

    /**
     * Serializes a ChatMessage object into a byte array.
     *
     * @param message The ChatMessage object to serialize.
     * @return A byte array representing the message.
     */
    public static byte[] serialize(final ChatMessage message) {
        // --- 1. Convert all fields to bytes ---
        final byte[] messageIdBytes = message.getMessageId().getBytes(StandardCharsets.UTF_8);
        final byte[] userIdBytes = message.getUserId().getBytes(StandardCharsets.UTF_8);
        final byte[] senderNameBytes = message.getSenderDisplayName().getBytes(StandardCharsets.UTF_8);
        final byte[] contentBytes = message.getContent().getBytes(StandardCharsets.UTF_8);

        // Convert LocalDateTime to a long (epoch seconds)
        final long timestampEpoch = message.getTimestamp().toEpochSecond(ZoneOffset.UTC);

        // Handle the nullable replyToMessageId
        final byte[] replyIdBytes;
        if (message.getReplyToMessageId() != null) {
            replyIdBytes = message.getReplyToMessageId().getBytes(StandardCharsets.UTF_8);
        } else {
            replyIdBytes = new byte[0]; // Empty array if null
        }

        // --- 2. Calculate total size ---
        // (4 bytes for each "length" integer) + (bytes of each string) + (8 bytes for the long)
        final int totalSize = (4 + messageIdBytes.length)
                + (4 + userIdBytes.length)
                + (4 + senderNameBytes.length)
                + (4 + contentBytes.length)
                + 8 // for the long timestamp
                + (4 + replyIdBytes.length);

        // --- 3. Write to ByteBuffer ---
        final ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        // Write strings (Length-Prefixed)
        writeString(buffer, message.getMessageId());
        writeString(buffer, message.getUserId());
        writeString(buffer, message.getSenderDisplayName());
        writeString(buffer, message.getContent());

        // Write the timestamp
        buffer.putLong(timestampEpoch);

        // Write the reply ID (handles null)
        writeString(buffer, message.getReplyToMessageId());

        return buffer.array();
    }

    /**
     * Deserializes a byte array back into a ChatMessage object.
     *
     * @param data The byte array to deserialize.
     * @return The re-constructed ChatMessage object.
     */
    public static ChatMessage deserialize(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);

        // Read in the exact same order we wrote
        final String messageId = readString(buffer);
        final String userId = readString(buffer);
        final String senderName = readString(buffer);
        final String content = readString(buffer);

        // Read the timestamp
        final long timestampEpoch = buffer.getLong();

        // Read the reply ID (handles null)
        final String replyId = readString(buffer);

        // Use the special constructor to re-create the ChatMessage
        return new ChatMessage(
                messageId,
                userId,
                senderName,
                content,
                timestampEpoch,
                replyId
        );
    }

    // --- Helper Methods ---

    /**
     * A helper to write a string to the buffer in [length][data] format.
     * Handles null strings by writing a length of 0.
     */
    private static void writeString(final ByteBuffer buffer, final String s) {
        if (s == null) {
            buffer.putInt(0);
        } else {
            final byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }
    }

    /**
     * A helper to read a string from the buffer in [length][data] format.
     * Handles null strings by checking for a length of 0.
     */
    private static String readString(final ByteBuffer buffer) {
        final int length = buffer.getInt();
        if (length == 0) {
            return null; // Was a null string
        } else {
            final byte[] bytes = new byte[length];
            buffer.get(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}