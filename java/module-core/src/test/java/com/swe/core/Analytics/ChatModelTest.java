/**
 *  Contributed by Kishore.
 */
package com.swe.core.Analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Test;

public class ChatModelTest {

    @Test
    public void defaultConstructorCreatesEmptyModel() {
        final ChatModel model = new ChatModel();
        assertNull(model.getId());
        assertNull(model.getAuthor());
        assertNull(model.getTimeStamp());
        assertNull(model.getContent());
        assertNotNull(model.getAttachment());
        assertFalse(model.getAttachment().isPresent());
    }

    @Test
    public void settersAndGettersWork() {
        final ChatModel model = new ChatModel();
        model.setId("chat-123");
        model.setAuthor("user@example.com");
        model.setTimeStamp(1700000000000L);
        model.setContent("Hello, world!");

        assertEquals("chat-123", model.getId());
        assertEquals("user@example.com", model.getAuthor());
        assertEquals(Long.valueOf(1700000000000L), model.getTimeStamp());
        assertEquals("Hello, world!", model.getContent());
    }

    @Test
    public void setAttachmentWithPath() {
        final ChatModel model = new ChatModel();
        final Path path = Paths.get("/tmp/file.txt");
        model.setAttachment(Optional.of(path));

        assertTrue(model.getAttachment().isPresent());
        assertEquals(path, model.getAttachment().get());
    }

    @Test
    public void setAttachmentWithEmptyOptional() {
        final ChatModel model = new ChatModel();
        model.setAttachment(Optional.empty());

        assertFalse(model.getAttachment().isPresent());
    }

    @Test
    public void setAttachmentWithNull() {
        final ChatModel model = new ChatModel();
        model.setAttachment(null);

        assertNull(model.getAttachment());
    }

    @Test
    public void canSetAllFields() {
        final ChatModel model = new ChatModel();
        final Path path = Paths.get("/tmp/attachment.pdf");

        model.setId("chat-456");
        model.setAuthor("author@example.com");
        model.setTimeStamp(1700000001000L);
        model.setContent("Test message");
        model.setAttachment(Optional.of(path));

        assertEquals("chat-456", model.getId());
        assertEquals("author@example.com", model.getAuthor());
        assertEquals(Long.valueOf(1700000001000L), model.getTimeStamp());
        assertEquals("Test message", model.getContent());
        assertTrue(model.getAttachment().isPresent());
        assertEquals(path, model.getAttachment().get());
    }
}

