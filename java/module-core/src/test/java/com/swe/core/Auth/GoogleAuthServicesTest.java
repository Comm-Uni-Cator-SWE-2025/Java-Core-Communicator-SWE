package com.swe.core.Auth;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GoogleAuthServicesTest {

    private Path tokensDir;

    @Before
    public void setUp() throws IOException {
        tokensDir = Paths.get("tokens").toAbsolutePath();
        deleteTokensDirectory();
    }

    @After
    public void tearDown() throws IOException {
        deleteTokensDirectory();
    }

    @Test
    public void logoutDeletesTokensDirectoryWithContents() throws Exception {
        Files.createDirectories(tokensDir.resolve("nested"));
        Files.writeString(tokensDir.resolve("nested/token.json"), "{}");

        new GoogleAuthServices().logout();

        assertFalse(Files.exists(tokensDir));
    }

    @Test
    public void logoutHandlesMissingTokensDirectory() throws Exception {
        new GoogleAuthServices().logout();
        assertFalse(Files.exists(tokensDir));
    }

    private void deleteTokensDirectory() throws IOException {
        if (tokensDir == null || !Files.exists(tokensDir)) {
            return;
        }
        Files.walk(tokensDir)
            .sorted(Comparator.reverseOrder())
            .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // best effort cleanup for test isolation
                }
            });
    }
}

