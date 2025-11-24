package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Deflater;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileHandlerTest {

    private LocalFileHandler handler;
    private String originalUserHome;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        handler = new LocalFileHandler();
        originalUserHome = System.getProperty("user.home");
    }

    @AfterEach
    void tearDown() {
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void processFileForSendingCompressesContent() throws Exception {
        Path file = tempDir.resolve("note.txt");
        Files.write(file, "hello world".getBytes(StandardCharsets.UTF_8));

        IChatFileHandler.FileResult result = handler.processFileForSending(file.toString());

        assertEquals("hello world".getBytes(StandardCharsets.UTF_8).length, result.originalFileSize());
        byte[] decompressed = Utilities.Decompress(result.compressedData());
        assertArrayEquals("hello world".getBytes(StandardCharsets.UTF_8), decompressed);
    }

    @Test
    void processFileForSendingTrimsPrefixedAsterisk() throws Exception {
        Path file = tempDir.resolve("data.bin");
        Files.write(file, new byte[] {5, 6, 7});

        IChatFileHandler.FileResult result =
                handler.processFileForSending("* " + file.toString());

        assertEquals(3, result.originalFileSize());
    }

    @Test
    void processFileForSendingRejectsInvalidPaths() {
        assertThrows(IllegalArgumentException.class, () -> handler.processFileForSending(null));
        assertThrows(IllegalArgumentException.class, () -> handler.processFileForSending(""));
        assertThrows(
                IllegalArgumentException.class,
                () -> handler.processFileForSending(tempDir.resolve("missing.txt").toString()));
    }

    @Test
    void decompressAndSaveFileWritesToDownloads() throws Exception {
        byte[] original = "file-body".getBytes(StandardCharsets.UTF_8);
        byte[] compressed = Utilities.Compress(original, Deflater.BEST_SPEED);
        System.setProperty("user.home", tempDir.toString());

        handler.decompressAndSaveFile("msg-1", "report.txt", compressed);

        Path saved = tempDir.resolve("Downloads").resolve("report.txt");
        assertTrue(Files.exists(saved));
        assertArrayEquals(original, Files.readAllBytes(saved));
    }

    @Test
    void decompressAndSaveFileResolvesConflicts() throws Exception {
        byte[] original = "new".getBytes(StandardCharsets.UTF_8);
        byte[] compressed = Utilities.Compress(original, Deflater.BEST_SPEED);
        System.setProperty("user.home", tempDir.toString());
        Path downloads = tempDir.resolve("Downloads");
        Files.createDirectories(downloads);
        Files.write(downloads.resolve("report.txt"), "existing".getBytes(StandardCharsets.UTF_8));

        handler.decompressAndSaveFile("msg-2", "report.txt", compressed);

        Path conflictResolved = downloads.resolve("report (1).txt");
        assertTrue(Files.exists(conflictResolved));
        assertArrayEquals(original, Files.readAllBytes(conflictResolved));
    }

    @Test
    void decompressAndSaveFileThrowsWhenDecompressFails() {
        byte[] invalidCompressed = {1, 2, 3};

        assertThrows(
                Exception.class,
                () -> handler.decompressAndSaveFile("id", "bad.txt", invalidCompressed));
    }
}

