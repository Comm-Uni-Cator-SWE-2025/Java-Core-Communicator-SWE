package com.swe.core.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract-factory style entry point that hands out module-aware {@link SweLogger} instances.
 * <p>
 * The factory configures two handlers under the {@code com.swe.core} logger hierarchy:
 * <ul>
 *   <li>A console handler emitting INFO+ records</li>
 *   <li>A file handler capturing the complete stream to a per-run log file</li>
 * </ul>
 */
public final class SweLoggerFactory {

    private static final String ROOT_NAMESPACE = "com.swe.core";
    private static final String APP_NAME = "swecomm";
    private static final SweLogFormatter FORMATTER = new SweLogFormatter();
    private static final LogPathResolver RESOLVER = new LogPathResolver(APP_NAME);
    private static final long APP_START_MILLIS = System.currentTimeMillis();
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final Logger BOOTSTRAP_LOGGER = Logger.getLogger(ROOT_NAMESPACE + ".bootstrap");

    private static volatile ConsoleHandler consoleHandler;
    private static volatile Path activeLogFile;

    private SweLoggerFactory() {
    }

    public static SweLogger getLogger(final Class<?> owner) {
        Objects.requireNonNull(owner, "owner");
        return getLogger(owner.getName());
    }

    public static SweLogger getLogger(final String moduleTag) {
        bootstrapHandlersIfNeeded();
        final String sanitizedTag = sanitize(moduleTag);
        final Logger julLogger = Logger.getLogger(ROOT_NAMESPACE + "." + sanitizedTag);
        julLogger.setUseParentHandlers(true);
        julLogger.setLevel(Level.ALL);
        return new SweLogger(julLogger, sanitizedTag);
    }

    public static Path getActiveLogFile() {
        return activeLogFile;
    }

    private static String sanitize(final String rawTag) {
        if (rawTag == null || rawTag.isBlank()) {
            return "unknown";
        }
        return rawTag.trim().replaceAll("\\s+", ".");
    }

    private static void bootstrapHandlersIfNeeded() {
        if (INITIALIZED.get()) {
            return;
        }
        synchronized (SweLoggerFactory.class) {
            if (INITIALIZED.get()) {
                return;
            }
            final Logger root = Logger.getLogger(ROOT_NAMESPACE);
            root.setUseParentHandlers(false);
            root.setLevel(Level.ALL);
            removeHandlers(root);

            attachConsoleHandler(root);
            attachFileHandler(root);

            INITIALIZED.set(true);
        }
    }

    private static void attachConsoleHandler(final Logger root) {
        consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(FORMATTER);
        root.addHandler(consoleHandler);
    }

    private static void attachFileHandler(final Logger root) {
        try {
            final Path logFile = RESOLVER.resolve(APP_START_MILLIS);
            final java.util.logging.FileHandler fileHandler =
                new java.util.logging.FileHandler(logFile.toString(), false);
            fileHandler.setEncoding(StandardCharsets.UTF_8.name());
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(FORMATTER);
            activeLogFile = logFile;
            root.addHandler(fileHandler);
        } catch (IOException ioException) {
            BOOTSTRAP_LOGGER.log(Level.SEVERE, "Failed to set up file logging", ioException);
        }
    }

    private static void removeHandlers(final Logger logger) {
        for (Handler handler : logger.getHandlers()) {
            handler.close();
            logger.removeHandler(handler);
        }
    }

    // --- Test hooks -------------------------------------------------------

    static void resetForTests() {
        INITIALIZED.set(false);
        activeLogFile = null;
        final Logger root = Logger.getLogger(ROOT_NAMESPACE);
        removeHandlers(root);
    }

    static void flushHandlers() {
        final Logger root = Logger.getLogger(ROOT_NAMESPACE);
        for (Handler handler : root.getHandlers()) {
            handler.flush();
        }
    }

    /**
     * Adjusts the console handler log level at runtime.
     * <p>
     * All {@link SweLogger} instances delegate to the {@code com.swe.core} logger hierarchy
     * and therefore share the same console handler. Updating the level here affects console
     * output for every module (controller, networking, AI insights, etc.).
     *
     * @param level desired {@link Level}; ignored when {@code null}
     */
    public static void setConsoleLevel(final Level level) {
        if (consoleHandler != null && level != null) {
            consoleHandler.setLevel(level);
        }
    }
}

