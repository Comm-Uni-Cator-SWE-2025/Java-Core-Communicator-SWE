package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ChatMessageSerializerTest {

    @Test
    void serializeThenDeserializeRoundTripsAllFields() {
        ChatMessage original =
                new ChatMessage("id-1", "user-9", "Alice", "Hello world", "root-1");

        byte[] serialized = ChatMessageSerializer.serialize(original);
        ChatMessage restored = ChatMessageSerializer.deserialize(serialized);

        assertEquals(original.getMessageId(), restored.getMessageId());
        assertEquals(original.getUserId(), restored.getUserId());
        assertEquals(original.getSenderDisplayName(), restored.getSenderDisplayName());
        assertEquals(original.getContent(), restored.getContent());
        assertEquals(original.getReplyToMessageId(), restored.getReplyToMessageId());
        assertEquals(original.getTimestamp().toEpochSecond(ZoneOffset.UTC),
                restored.getTimestamp().toEpochSecond(ZoneOffset.UTC));
    }

    @Test
    void serializeHandlesNullFieldsAndEmptyReply() {
        ChatMessage message = new ChatMessage("null-id", "null-user", null, null, null);

        byte[] serialized = ChatMessageSerializer.serialize(message);
        ChatMessage restored = ChatMessageSerializer.deserialize(serialized);

        assertEquals("null-id", restored.getMessageId());
        assertEquals("null-user", restored.getUserId());
        assertNull(restored.getSenderDisplayName());
        assertNull(restored.getContent());
        assertNull(restored.getReplyToMessageId());
    }

    @Test
    void serializeProducesExpectedLayout() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 1, 12, 0);
        long epoch = timestamp.toEpochSecond(ZoneOffset.UTC);
        ChatMessage message =
                new ChatMessage("layout", "user", "Name", "Body", epoch, "reply");

        byte[] bytes = ChatMessageSerializer.serialize(message);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        assertEquals("layout", readString(buffer));
        assertEquals("user", readString(buffer));
        assertEquals("Name", readString(buffer));
        assertEquals("Body", readString(buffer));
        assertEquals(epoch, buffer.getLong());
        assertEquals("reply", readString(buffer));
    }

    private static String readString(ByteBuffer buffer) {
        int length = buffer.getInt();
        byte[] data = new byte[length];
        buffer.get(data);
        return new String(data, StandardCharsets.UTF_8);
    }
}

