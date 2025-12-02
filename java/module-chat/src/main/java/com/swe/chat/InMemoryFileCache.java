package com.swe.chat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IMPLEMENTATION: "Disk-First" caching strategy.
 * Offloads file data to the local disk immediately to prevent OutOfMemory errors.
 */
public class InMemoryFileCache implements IChatFileCache {

    // Maps MessageID -> File Metadata (Path on Disk)
    private final Map<String, FileCacheEntry> fileMap = new ConcurrentHashMap<>();

    @Override
    public void put(String messageId, String fileName, byte[] compressedData) {
        try {
            // 1. Create a temporary file (OS handles location, e.g., /tmp or C:\Users\...\AppData\Local\Temp)
            Path tempFile = Files.createTempFile("chat_cache_" + messageId, ".tmp");

            // 2. Write the heavy compressed data to disk immediately
            Files.write(tempFile, compressedData);

            // 3. Mark to delete on exit (safety net)
            tempFile.toFile().deleteOnExit();

            // 4. Store ONLY the path in RAM
            fileMap.put(messageId, new FileCacheEntry(fileName, tempFile));

            System.out.println("[Core.Cache] Offloaded " + compressedData.length + " bytes to disk at: " + tempFile);

        } catch (IOException e) {
            System.err.println("[Core.Cache] Failed to write temp file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Optional<FileCacheEntry> get(String messageId) {
        // Returns the path wrapper. The content is still on disk.
        return Optional.ofNullable(fileMap.get(messageId));
    }

    @Override
    public void remove(String messageId) {
        FileCacheEntry entry = fileMap.remove(messageId);
        if (entry != null) {
            try {
                // Clean up the actual file from disk
                Files.deleteIfExists(entry.tempFilePath());
                System.out.println("[Core.Cache] Deleted temp file: " + entry.tempFilePath());
            } catch (IOException e) {
                System.err.println("[Core.Cache] Failed to delete temp file: " + e.getMessage());
            }
        }
    }
}