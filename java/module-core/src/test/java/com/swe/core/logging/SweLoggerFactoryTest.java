package com.swe.core.logging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SweLoggerFactoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @After
    public void reset() {
        SweLoggerFactory.resetForTests();
        System.clearProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP);
    }

    @Test
    public void writesStructuredFileLogs() throws IOException {
        final Path overrideDir = temporaryFolder.newFolder("logs").toPath();
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, overrideDir.toString());

        final SweLogger logger = SweLoggerFactory.getLogger("core.meeting");
        logger.info("logger smoke test");
        SweLoggerFactory.flushHandlers();

        final Path logFile = SweLoggerFactory.getActiveLogFile();
        assertNotNull(logFile);
        final String content = Files.readString(logFile);
        assertTrue(content.contains("module=core.meeting"));
        assertTrue(content.contains("msg=\"logger smoke test\""));
    }
}

