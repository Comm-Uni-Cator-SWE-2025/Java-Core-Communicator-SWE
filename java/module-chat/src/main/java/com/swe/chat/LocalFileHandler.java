package com.swe.chat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.Deflater;

/**
 * IMPLEMENTATION: Concrete strategy for performing local file I/O and compression.
 */
public class LocalFileHandler implements IChatFileHandler {

    @Override
    public FileResult processFileForSending(String filePath) throws Exception {
        // Path Sanitization and Validation
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path is null or empty");
        }

        filePath = filePath.trim();
        if (filePath.startsWith("*")) {
            filePath = filePath.substring(1).trim();
        }

        if (!Files.exists(Paths.get(filePath))) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        // Read and Compress (Uses existing Utilities class)
        byte[] uncompressedData = Files.readAllBytes(Paths.get(filePath));
        long originalSize = uncompressedData.length;

        byte[] compressedData = Utilities.Compress(uncompressedData, Deflater.BEST_SPEED);
        if (compressedData == null) {
            throw new IOException("Failed to compress file");
        }

        return new FileResult(compressedData, originalSize);
    }

    @Override
    public void decompressAndSaveFile(String messageId, String fileName, byte[] compressedData) throws Exception {
        // Decompression (Uses existing Utilities class)
        byte[] decompressedData = Utilities.Decompress(compressedData);
        if (decompressedData == null) {
            throw new Exception("Failed to decompress file");
        }

        // Save to Downloads folder with conflict resolution
        String homeDir = System.getProperty("user.home");
        java.nio.file.Path downloadsDir = Paths.get(homeDir, "Downloads");

        if (!Files.exists(downloadsDir)) {
            Files.createDirectories(downloadsDir);
        }

        // Conflict resolution logic
        String originalName = fileName;
        String finalName = originalName;
        java.nio.file.Path savePath = downloadsDir.resolve(finalName);
        int counter = 1;

        while (Files.exists(savePath)) {
            String namePart = originalName;
            String extPart = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                namePart = originalName.substring(0, dotIndex);
                extPart = originalName.substring(dotIndex);
            }
            finalName = namePart + " (" + counter + ")" + extPart;
            savePath = downloadsDir.resolve(finalName);
            counter++;
        }

        // Write file
        Files.write(savePath, decompressedData);
        System.out.println("[Core.FileHandler] Saved message " + messageId + " to: " + savePath.toString());
    }
}