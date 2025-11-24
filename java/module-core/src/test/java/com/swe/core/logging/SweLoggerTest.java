package com.swe.core.logging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SweLoggerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @After
    public void reset() {
        SweLoggerFactory.resetForTests();
        System.clearProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP);
    }

    @Test
    public void traceLogsAtFinestLevel() throws IOException {
        final Path overrideDir = temporaryFolder.newFolder("logs").toPath();
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, overrideDir.toString());

        final SweLogger logger = SweLoggerFactory.getLogger("core.test");
        logger.trace("Trace message");
        SweLoggerFactory.flushHandlers();

        final Path logFile = SweLoggerFactory.getActiveLogFile();
        assertNotNull(logFile);
        final String content = Files.readString(logFile);
        assertTrue(content.contains("level=FINEST"));
        assertTrue(content.contains("msg=\"Trace message\""));
    }

    @Test
    public void debugLogsAtFineLevel() throws IOException {
        final Path overrideDir = temporaryFolder.newFolder("logs").toPath();
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, overrideDir.toString());

        final SweLogger logger = SweLoggerFactory.getLogger("core.test");
        logger.debug("Debug message");
        SweLoggerFactory.flushHandlers();

        final Path logFile = SweLoggerFactory.getActiveLogFile();
        assertNotNull(logFile);
        final String content = Files.readString(logFile);
        assertTrue(content.contains("level=FINE"));
        assertTrue(content.contains("msg=\"Debug message\""));
    }

    @Test
    public void infoLogsAtInfoLevel() throws IOException {
        final Path overrideDir = temporaryFolder.newFolder("logs").toPath();
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, overrideDir.toString());

        final SweLogger logger = SweLoggerFactory.getLogger("core.test");
        logger.info("Info message");
        SweLoggerFactory.flushHandlers();

        final Path logFile = SweLoggerFactory.getActiveLogFile();
        assertNotNull(logFile);
        final String content = Files.readString(logFile);
        assertTrue(content.contains("level=INFO"));
        assertTrue(content.contains("msg=\"Info message\""));
    }

    @Test
    public void warnLogsAtWarningLevel() throws IOException {
        final Path overrideDir = temporaryFolder.newFolder("logs").toPath();
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, overrideDir.toString());

        final SweLogger logger = SweLoggerFactory.getLogger("core.test");
        logger.warn("Warning message");
        SweLoggerFactory.flushHandlers();

        final Path logFile = SweLoggerFactory.getActiveLogFile();
        assertNotNull(logFile);
        final String content = Files.readString(logFile);
        assertTrue(content.contains("level=WARNING"));
        assertTrue(content.contains("msg=\"Warning message\""));
    }

    @Test
    public void errorLogsAtSevereLevel() throws IOException {
        final Path overrideDir = temporaryFolder.newFolder("logs").toPath();
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, overrideDir.toString());

        final SweLogger logger = SweLoggerFactory.getLogger("core.test");
        logger.error("Error message");
        SweLoggerFactory.flushHandlers();

        final Path logFile = SweLoggerFactory.getActiveLogFile();
        assertNotNull(logFile);
        final String content = Files.readString(logFile);
        assertTrue(content.contains("level=SEVERE"));
        assertTrue(content.contains("msg=\"Error message\""));
    }

    @Test
    public void errorWithThrowableLogsException() throws IOException {
        final Path overrideDir = temporaryFolder.newFolder("logs").toPath();
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, overrideDir.toString());

        final SweLogger logger = SweLoggerFactory.getLogger("core.test");
        final Exception exception = new RuntimeException("Test exception");
        logger.error("Error with exception", exception);
        SweLoggerFactory.flushHandlers();

        final Path logFile = SweLoggerFactory.getActiveLogFile();
        assertNotNull(logFile);
        final String content = Files.readString(logFile);
        assertTrue(content.contains("level=SEVERE"));
        assertTrue(content.contains("msg=\"Error with exception\""));
        assertTrue(content.contains("Test exception"));
    }

    @Test
    public void logWithContextIncludesContext() throws IOException {
        final Path overrideDir = temporaryFolder.newFolder("logs").toPath();
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, overrideDir.toString());

        final SweLogger logger = SweLoggerFactory.getLogger("core.test");
        final Map<String, Object> context = new HashMap<>();
        context.put("sessionId", "abc123");
        context.put("userId", "user1");
        logger.log(Level.INFO, "Message with context", context, null);
        SweLoggerFactory.flushHandlers();

        final Path logFile = SweLoggerFactory.getActiveLogFile();
        assertNotNull(logFile);
        final String content = Files.readString(logFile);
        assertTrue(content.contains("msg=\"Message with context\""));
        assertTrue(content.contains("sessionId=abc123"));
        assertTrue(content.contains("userId=user1"));
    }

    @Test
    public void logWithNullContextHandlesGracefully() throws IOException {
        final Path overrideDir = temporaryFolder.newFolder("logs").toPath();
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, overrideDir.toString());

        final SweLogger logger = SweLoggerFactory.getLogger("core.test");
        logger.log(Level.INFO, "Message without context", null, null);
        SweLoggerFactory.flushHandlers();

        final Path logFile = SweLoggerFactory.getActiveLogFile();
        assertNotNull(logFile);
        final String content = Files.readString(logFile);
        assertTrue(content.contains("msg=\"Message without context\""));
        assertTrue(content.contains("ctx=\"\""));
    }
}

