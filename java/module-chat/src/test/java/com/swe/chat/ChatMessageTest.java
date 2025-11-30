package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ChatMessageTest {

    @Test
    void newMessageConstructorAssignsNowTimestamp() {
        LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
        ChatMessage message = new ChatMessage("id", "user", "Alice", "Hello", null);
        LocalDateTime after = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1);

        assertEquals("id", message.getMessageId());
        assertEquals("user", message.getUserId());
        assertEquals("Alice", message.getSenderDisplayName());
        assertEquals("Hello", message.getContent());
        assertNull(message.getReplyToMessageId());

        assertTrue(
                !message.getTimestamp().isBefore(before)
                        && !message.getTimestamp().isAfter(after),
                "Timestamp should be captured at creation time");
    }

    @Test
    void deserializationConstructorRestoresEpochTimestamp() {
        LocalDateTime expected = LocalDateTime.of(2024, 12, 25, 5, 30);
        long epoch = expected.toEpochSecond(ZoneOffset.UTC);

        ChatMessage message =
                new ChatMessage("mid", "uid", "Bob", "Msg", epoch, "root");

        assertEquals("mid", message.getMessageId());
        assertEquals("uid", message.getUserId());
        assertEquals("Bob", message.getSenderDisplayName());
        assertEquals("Msg", message.getContent());
        assertEquals("root", message.getReplyToMessageId());
        assertEquals(expected, message.getTimestamp());
    }

    @Test
    void allowsNullDisplayNameAndContent() {
        ChatMessage message = new ChatMessage("id", "user", null, null, "parent");

        assertNull(message.getSenderDisplayName());
        assertNull(message.getContent());
        assertEquals("parent", message.getReplyToMessageId());
        assertNotNull(message.getTimestamp());
    }
}

