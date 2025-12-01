package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class FileMessageSerializerTest {

    @Test
    void serializeThenDeserializeRoundTripsContentMode() {
        byte[] content = {1, 2, 3};
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 1, 12, 0);
        long epoch = timestamp.toEpochSecond(ZoneOffset.UTC);
        FileMessage original =
                new FileMessage("id-1", "user-9", "Alice", "Hello world", "doc.txt", content, epoch, "root-1");

        byte[] serialized = FileMessageSerializer.serialize(original);
        FileMessage restored = FileMessageSerializer.deserialize(serialized);

        assertEquals(original.getMessageId(), restored.getMessageId());
        assertEquals(original.getUserId(), restored.getUserId());
        assertEquals(original.getSenderDisplayName(), restored.getSenderDisplayName());
        assertEquals(original.getCaption(), restored.getCaption());
        assertEquals(original.getFileName(), restored.getFileName());
        assertNull(restored.getFilePath());
        assertArrayEquals(content, restored.getFileContent());
        assertEquals("root-1", restored.getReplyToMessageId());
        assertEquals(
                original.getTimestamp().toEpochSecond(ZoneOffset.UTC),
                restored.getTimestamp().toEpochSecond(ZoneOffset.UTC));
    }

    @Test
    void serializeHandlesNullFieldsAndPathMode() {
        FileMessage message =
                new FileMessage("null-id", null, null, null, null, "/tmp/path", null);

        byte[] serialized = FileMessageSerializer.serialize(message);
        FileMessage restored = FileMessageSerializer.deserialize(serialized);

        assertEquals("null-id", restored.getMessageId());
        assertNull(restored.getUserId());
        assertNull(restored.getSenderDisplayName());
        assertNull(restored.getCaption());
        assertNull(restored.getFileName());
        assertEquals("/tmp/path", restored.getFilePath());
        assertNull(restored.getFileContent());
        assertNull(restored.getReplyToMessageId());
    }

    @Test
    void serializeProducesExpectedLayout() {
        byte[] content = {5, 6};
        FileMessage message =
                new FileMessage("layout", "user", "Name", "Body", "layout.bin", content, 42L, "reply");

        byte[] bytes = FileMessageSerializer.serialize(message);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        assertEquals("layout", readString(buffer));
        assertEquals("user", readString(buffer));
        assertEquals("Name", readString(buffer));
        assertEquals("Body", readString(buffer));
        assertEquals("layout.bin", readString(buffer));
        assertNull(readString(buffer)); // filePath
        assertArrayEquals(content, readBytes(buffer));
        assertEquals(42L, buffer.getLong());
        assertEquals("reply", readString(buffer));
    }

    private static String readString(ByteBuffer buffer) {
        byte[] bytes = readBytes(buffer);
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] readBytes(ByteBuffer buffer) {
        int len = buffer.getInt();
        if (len <= 0) {
            return null;
        }
        byte[] data = new byte[len];
        buffer.get(data);
        return data;
    }
}

