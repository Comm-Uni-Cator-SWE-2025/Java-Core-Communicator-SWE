package com.swe.core.logging;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thin wrapper around {@link java.util.logging.Logger} that enforces module-aware
 * logging and a consistent message format.
 */
public final class SweLogger {

    private final Logger delegate;
    private final String moduleTag;

    SweLogger(final Logger delegate, final String moduleTag) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.moduleTag = Objects.requireNonNull(moduleTag, "moduleTag");
    }

    public void trace(final String message) {
        log(Level.FINEST, message, null, null);
    }

    public void debug(final String message) {
        log(Level.FINE, message, null, null);
    }

    public void info(final String message) {
        log(Level.INFO, message, null, null);
    }

    public void warn(final String message) {
        log(Level.WARNING, message, null, null);
    }

    public void error(final String message) {
        log(Level.SEVERE, message, null, null);
    }

    public void error(final String message, final Throwable throwable) {
        log(Level.SEVERE, message, null, throwable);
    }

    public void log(final Level level, final String message, final Map<String, ?> context, final Throwable throwable) {
        if (!delegate.isLoggable(level)) {
            return;
        }
        final String formattedContext = toContextString(context);
        final SweLogRecord record = new SweLogRecord(
            level,
            message,
            moduleTag,
            Thread.currentThread().getName(),
            formattedContext
        );
        record.setInstant(Instant.now());
        record.setLoggerName(delegate.getName());
        record.setThrown(throwable);
        delegate.log(record);
    }

    private static String toContextString(final Map<String, ?> context) {
        if (context == null || context.isEmpty()) {
            return "";
        }
        final StringJoiner joiner = new StringJoiner(", ");
        context.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }
}

