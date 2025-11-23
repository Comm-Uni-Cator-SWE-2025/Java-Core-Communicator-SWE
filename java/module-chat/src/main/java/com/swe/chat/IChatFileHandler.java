package com.swe.chat;

/**
 * ABSTRACTION: Contract for all file system and compression operations.
 */
public interface IChatFileHandler {

    /**
     * DTO for the return value of file processing.
     */
    record FileResult(byte[] compressedData, long originalFileSize) {}

    FileResult processFileForSending(String filePath) throws Exception;

    void decompressAndSaveFile(String messageId, String fileName, byte[] compressedData)
            throws Exception;
}