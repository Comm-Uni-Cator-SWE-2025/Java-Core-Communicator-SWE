// module-chat/src/main/java/com/swe/chat/InMemoryFileCache.java

package com.swe.chat;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IMPLEMENTATION: Concrete implementation of the file cache using a ConcurrentHashMap.
 */
public class InMemoryFileCache implements IChatFileCache {

    private final Map<String, FileCacheEntry> fileCache = new ConcurrentHashMap<>();

    @Override
    public void put(String messageId, String fileName, byte[] compressedData) {
        fileCache.put(messageId, new FileCacheEntry(fileName, compressedData));
        System.out.println("[Core.Cache] Cached compressed file: " + messageId);
    }

    @Override
    public Optional<FileCacheEntry> get(String messageId) {
        return Optional.ofNullable(fileCache.get(messageId));
    }

    @Override
    public void remove(String messageId) {
        fileCache.remove(messageId);
        System.out.println("[Core.Cache] Removed file from cache: " + messageId);
    }
}