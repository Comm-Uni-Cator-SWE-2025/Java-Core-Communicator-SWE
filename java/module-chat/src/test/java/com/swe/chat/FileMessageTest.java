package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class FileMessageTest {

    @Test
    void pathModeConstructorPopulatesFields() {
        FileMessage message =
                new FileMessage("mid", "uid", "Alice", "Caption", "file.txt", "/tmp/path", "root");

        assertEquals("mid", message.getMessageId());
        assertEquals("uid", message.getUserId());
        assertEquals("Alice", message.getSenderDisplayName());
        assertEquals("Caption", message.getCaption());
        assertEquals("file.txt", message.getFileName());
        assertEquals("/tmp/path", message.getFilePath());
        assertNull(message.getFileContent());
        assertEquals("root", message.getReplyToMessageId());
    }

    @Test
    void contentModeConstructorRestoresEpochTimestamp() {
        byte[] content = {1, 2, 3};
        LocalDateTime timestamp = LocalDateTime.of(2024, 10, 10, 10, 0);
        long epoch = timestamp.toEpochSecond(ZoneOffset.UTC);

        FileMessage message =
                new FileMessage("mid", "uid", "Bob", "Caption", "data.bin", content, epoch, null);

        assertEquals("mid", message.getMessageId());
        assertEquals("uid", message.getUserId());
        assertEquals("Bob", message.getSenderDisplayName());
        assertEquals("Caption", message.getCaption());
        assertEquals("data.bin", message.getFileName());
        assertNull(message.getFilePath());
        assertArrayEquals(content, message.getFileContent());
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    void allowsNullOptionalFields() {
        FileMessage message =
                new FileMessage("mid", "uid", null, null, null, null, null);

        assertNull(message.getSenderDisplayName());
        assertNull(message.getCaption());
        assertNull(message.getFileName());
        assertNull(message.getFilePath());
        assertNull(message.getReplyToMessageId());
    }
}

