package com.swe.core.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Test;

public class LogPathResolverTest {

    @After
    public void tearDown() {
        System.clearProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP);
    }

    @Test
    public void selectsLinuxConvention() {
        final Path base = LogPathResolver.resolveBaseDirectory(
            "Linux",
            Paths.get("/home/tester"),
            null,
            null,
            "swecomm"
        );
        assertEquals(Paths.get("/home/tester/.local/share/swecomm/logs"), base);
    }

    @Test
    public void selectsMacConvention() {
        final Path base = LogPathResolver.resolveBaseDirectory(
            "Mac OS X",
            Paths.get("/Users/tester"),
            null,
            null,
            "swecomm"
        );
        assertEquals(Paths.get("/Users/tester/Library/Logs/swecomm"), base);
    }

    @Test
    public void selectsWindowsConvention() {
        final Path base = LogPathResolver.resolveBaseDirectory(
            "Windows 11",
            Paths.get("C:/Users/tester"),
            "C:/Users/tester/AppData/Local",
            null,
            "swecomm"
        );
        assertEquals(Paths.get("C:/Users/tester/AppData/Local/swecomm/logs"), base);
    }

    @Test
    public void honorsOverrideProperty() throws IOException {
        final Path tempDir = Files.createTempDirectory("swecomm-logs");
        System.setProperty(LogPathResolver.LOG_DIR_OVERRIDE_PROP, tempDir.toString());
        final LogPathResolver resolver = new LogPathResolver("swecomm");
        final Path logFile = resolver.resolve(1700000L);
        assertTrue(Files.exists(logFile.getParent()));
        assertEquals(tempDir.resolve("1700000.log"), logFile);
    }
}

