package com.swe.chat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        Path sourcePath = Paths.get(filePath);
        if (!Files.exists(sourcePath)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        // Read and Compress
        byte[] uncompressedData = Files.readAllBytes(sourcePath);
        long originalSize = uncompressedData.length;

        // Assuming Utilities.Compress exists as per your original code
        byte[] compressedData = Utilities.Compress(uncompressedData, Deflater.BEST_SPEED);
        if (compressedData == null) {
            throw new IOException("Failed to compress file");
        }

        return new FileResult(compressedData, originalSize);
    }

    @Override
    public void decompressAndSaveFile(String messageId, String fileName, Path compressedSourcePath) throws Exception {
        // 1. Read the compressed bytes from the TEMP file (Disk -> RAM)
        // We only hold this in memory for the split second required to decompress it.
        if (!Files.exists(compressedSourcePath)) {
            throw new IOException("Temp cache file not found: " + compressedSourcePath);
        }

        byte[] compressedData = Files.readAllBytes(compressedSourcePath);

        // 2. Decompress (RAM processing)
        // Assuming Utilities.Decompress exists as per your original code
        byte[] decompressedData = Utilities.Decompress(compressedData);
        if (decompressedData == null) {
            throw new Exception("Failed to decompress file");
        }

        // 3. Save to Downloads folder with conflict resolution
        String homeDir = System.getProperty("user.home");
        Path downloadsDir = Paths.get(homeDir, "Downloads");

        if (!Files.exists(downloadsDir)) {
            Files.createDirectories(downloadsDir);
        }

        // Conflict resolution logic
        String originalName = fileName;
        String finalName = originalName;
        Path savePath = downloadsDir.resolve(finalName);
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

        // 4. Write final file
        Files.write(savePath, decompressedData);
        System.out.println("[Core.FileHandler] Saved message " + messageId + " to: " + savePath.toString());
    }
}