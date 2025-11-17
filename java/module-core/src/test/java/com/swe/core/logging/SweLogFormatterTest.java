package com.swe.core.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;
import org.junit.Test;

public class SweLogFormatterTest {

    private final SweLogFormatter formatter = new SweLogFormatter();

    @Test
    public void formatIncludesLevelAndModule() {
        final SweLogRecord record = new SweLogRecord(
            Level.WARNING,
            "Hello \"World\"",
            "core.analytics",
            "thread-1",
            "session=abc123"
        );
        record.setMillis(1700000000000L);
        final String formatted = formatter.format(record);
        assertTrue(formatted.contains("level=WARNING"));
        assertTrue(formatted.contains("module=core.analytics"));
        final SweLogEntry parsed = formatter.parseLine(formatted.trim());
        assertEquals("Hello \"World\"", parsed.message());
        assertEquals("core.analytics", parsed.module());
        assertEquals("session=abc123", parsed.context());
    }
}

