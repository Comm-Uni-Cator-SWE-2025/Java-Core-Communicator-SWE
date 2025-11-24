package com.swe.chat;

import java.util.Optional;

/**
 * ABSTRACTION: Contract for storing and retrieving compressed file packets.
 */
public interface IChatFileCache {

    /**
     * Data object for storing file details in the cache.
     */
    record FileCacheEntry(String fileName, byte[] compressedData) {}

    void put(String messageId, String fileName, byte[] compressedData);
    Optional<FileCacheEntry> get(String messageId);
    void remove(String messageId);
}