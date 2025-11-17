package com.swe.core.logging;

import java.time.Instant;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Immutable representation of a single parsed log line emitted by {@link SweLogFormatter}.
 * <p>
 * Having a structured type makes it trivial to build diagnostics tooling or crash
 * reporters without re-implementing parsing logic in multiple places.
 */
public record SweLogEntry(
    Instant timestamp,
    long epochMillis,
    Level level,
    String module,
    String thread,
    String message,
    String context,
    String error
) {
    public SweLogEntry {
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(thread, "thread");
        Objects.requireNonNull(message, "message");
        context = context == null ? "" : context;
        error = error == null ? "" : error;
    }
}

