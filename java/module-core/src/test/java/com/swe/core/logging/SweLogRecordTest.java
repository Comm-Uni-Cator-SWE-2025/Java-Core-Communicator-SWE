package com.swe.core.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.Test;

public class SweLogRecordTest {

    @Test
    public void createsRecordWithAllFields() {
        final SweLogRecord record = new SweLogRecord(
            Level.INFO,
            "Test message",
            "core.test",
            "main",
            "context=value"
        );

        assertEquals(Level.INFO, record.getLevel());
        assertEquals("Test message", record.getMessage());
        assertEquals("core.test", record.getModuleTag());
        assertEquals("main", record.getThreadName());
        assertEquals("context=value", record.getContext());
    }

    @Test
    public void nullContextDefaultsToEmptyString() {
        final SweLogRecord record = new SweLogRecord(
            Level.WARNING,
            "Message",
            "core.test",
            "thread-1",
            null
        );

        assertEquals("", record.getContext());
    }

    @Test
    public void fromLogRecordCreatesSweLogRecord() {
        final LogRecord logRecord = new LogRecord(Level.INFO, "Original message");
        logRecord.setLoggerName("com.swe.core.test");
        logRecord.setMillis(System.currentTimeMillis());
        logRecord.setParameters(new Object[]{"context=test"});

        final SweLogRecord sweRecord = SweLogRecord.from(logRecord);

        assertEquals(Level.INFO, sweRecord.getLevel());
        assertEquals("Original message", sweRecord.getMessage());
        assertEquals("com.swe.core.test", sweRecord.getModuleTag());
        assertNotNull(sweRecord.getThreadName());
        assertEquals("context=test", sweRecord.getContext());
    }

    @Test
    public void fromLogRecordWithNullLoggerNameUsesUnknown() {
        final LogRecord logRecord = new LogRecord(Level.WARNING, "Message");
        logRecord.setLoggerName(null);
        logRecord.setMillis(System.currentTimeMillis());

        final SweLogRecord sweRecord = SweLogRecord.from(logRecord);

        assertEquals("unknown", sweRecord.getModuleTag());
    }

    @Test
    public void fromLogRecordWithNullParametersUsesEmptyContext() {
        final LogRecord logRecord = new LogRecord(Level.INFO, "Message");
        logRecord.setLoggerName("com.swe.core.test");
        logRecord.setMillis(System.currentTimeMillis());
        logRecord.setParameters(null);

        final SweLogRecord sweRecord = SweLogRecord.from(logRecord);

        assertEquals("", sweRecord.getContext());
    }

    @Test
    public void fromLogRecordWithInstantPreservesInstant() {
        final Instant instant = Instant.now();
        final LogRecord logRecord = new LogRecord(Level.INFO, "Message");
        logRecord.setLoggerName("com.swe.core.test");
        logRecord.setInstant(instant);

        final SweLogRecord sweRecord = SweLogRecord.from(logRecord);

        assertEquals(instant, sweRecord.getInstant());
    }

    @Test
    public void fromLogRecordWithThrownPreservesThrowable() {
        final Exception exception = new RuntimeException("Test exception");
        final LogRecord logRecord = new LogRecord(Level.SEVERE, "Error message");
        logRecord.setLoggerName("com.swe.core.test");
        logRecord.setMillis(System.currentTimeMillis());
        logRecord.setThrown(exception);

        final SweLogRecord sweRecord = SweLogRecord.from(logRecord);

        assertEquals(exception, sweRecord.getThrown());
    }
}

