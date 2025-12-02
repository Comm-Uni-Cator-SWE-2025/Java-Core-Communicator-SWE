package com.swe.chat;

import java.nio.file.Path;

/**
 * ABSTRACTION: Contract for all file system and compression operations.
 */
public interface IChatFileHandler {

    record FileResult(byte[] compressedData, long originalFileSize) {}

    FileResult processFileForSending(String filePath) throws Exception;

    // UPDATED: Now accepts a Path to the compressed source file on disk
    void decompressAndSaveFile(String messageId, String fileName, Path compressedSourcePath)
            throws Exception;
}