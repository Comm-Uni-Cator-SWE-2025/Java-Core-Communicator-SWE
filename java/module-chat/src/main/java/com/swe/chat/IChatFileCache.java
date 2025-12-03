package com.swe.chat;

import java.nio.file.Path;
import java.util.Optional;

/**
 * ABSTRACTION: Contract for storing and retrieving compressed file packets.
 * UPDATED: Now uses disk-based references (Path) instead of holding raw bytes in RAM.
 */
public interface IChatFileCache {

    /**
     * Data object for storing file details in the cache.
     * We store the 'tempFilePath' to the disk location, not the actual bytes.
     */
    record FileCacheEntry(String fileName, Path tempFilePath) {}

    void put(String messageId, String fileName, byte[] compressedData);
    Optional<FileCacheEntry> get(String messageId);
    void remove(String messageId);
}