package com.swe.core.logging;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.logging.Level;
import org.junit.Test;

public class SweLogEntryTest {

    @Test
    public void createsEntryWithAllFields() {
        final Instant timestamp = Instant.now();
        final SweLogEntry entry = new SweLogEntry(
            timestamp,
            1700000000000L,
            Level.INFO,
            "core.test",
            "main",
            "Test message",
            "context=value",
            "error details"
        );

        assertEquals(timestamp, entry.timestamp());
        assertEquals(1700000000000L, entry.epochMillis());
        assertEquals(Level.INFO, entry.level());
        assertEquals("core.test", entry.module());
        assertEquals("main", entry.thread());
        assertEquals("Test message", entry.message());
        assertEquals("context=value", entry.context());
        assertEquals("error details", entry.error());
    }

    @Test
    public void nullContextDefaultsToEmptyString() {
        final Instant timestamp = Instant.now();
        final SweLogEntry entry = new SweLogEntry(
            timestamp,
            1700000000000L,
            Level.WARNING,
            "core.test",
            "thread-1",
            "Message",
            null,
            null
        );

        assertEquals("", entry.context());
        assertEquals("", entry.error());
    }

    @Test(expected = NullPointerException.class)
    public void nullTimestampThrowsException() {
        new SweLogEntry(
            null,
            1700000000000L,
            Level.INFO,
            "core.test",
            "main",
            "Message",
            null,
            null
        );
    }

    @Test(expected = NullPointerException.class)
    public void nullLevelThrowsException() {
        new SweLogEntry(
            Instant.now(),
            1700000000000L,
            null,
            "core.test",
            "main",
            "Message",
            null,
            null
        );
    }

    @Test(expected = NullPointerException.class)
    public void nullModuleThrowsException() {
        new SweLogEntry(
            Instant.now(),
            1700000000000L,
            Level.INFO,
            null,
            "main",
            "Message",
            null,
            null
        );
    }

    @Test(expected = NullPointerException.class)
    public void nullThreadThrowsException() {
        new SweLogEntry(
            Instant.now(),
            1700000000000L,
            Level.INFO,
            "core.test",
            null,
            "Message",
            null,
            null
        );
    }

    @Test(expected = NullPointerException.class)
    public void nullMessageThrowsException() {
        new SweLogEntry(
            Instant.now(),
            1700000000000L,
            Level.INFO,
            "core.test",
            "main",
            null,
            null,
            null
        );
    }
}

