package com.swe.core.logging;

import java.time.Instant;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Small extension around {@link LogRecord} that keeps track of the module tag, thread name,
 * and any contextual payload text we want to persist alongside the log message.
 */
final class SweLogRecord extends LogRecord {

    private final String moduleTag;
    private final String threadName;
    private final String context;

    SweLogRecord(
        final Level level,
        final String message,
        final String moduleTag,
        final String threadName,
        final String context
    ) {
        super(level, message);
        this.moduleTag = Objects.requireNonNull(moduleTag, "moduleTag");
        this.threadName = Objects.requireNonNull(threadName, "threadName");
        this.context = context == null ? "" : context;
    }

    String getModuleTag() {
        return moduleTag;
    }

    String getThreadName() {
        return threadName;
    }

    String getContext() {
        return context;
    }

    static SweLogRecord from(final LogRecord delegate) {
        final String module = delegate.getLoggerName() != null ? delegate.getLoggerName() : "unknown";
        final String thread = Thread.currentThread().getName();
        final String context = delegate.getParameters() != null && delegate.getParameters().length > 0
            ? String.valueOf(delegate.getParameters()[0])
            : "";
        final SweLogRecord record = new SweLogRecord(
            delegate.getLevel(),
            delegate.getMessage(),
            module,
            thread,
            context
        );
        if (delegate.getInstant() != null) {
            record.setInstant(delegate.getInstant());
        } else {
            record.setInstant(Instant.ofEpochMilli(delegate.getMillis()));
        }
        record.setThrown(delegate.getThrown());
        return record;
    }
}

