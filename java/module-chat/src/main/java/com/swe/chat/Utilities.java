package com.swe.chat;

import java.io.*;
import java.util.zip.*;

public class Utilities {

    /**
     * Compresses bytes using GZIP with a default (balanced) compression level.
     *
     * @param uncompressedData The raw data to compress.
     * @return The compressed data.
     */
    public static byte[] Compress(byte[] uncompressedData) {
        // Calls the new, more advanced method with the "default" level.
        // GZIP is just Deflate with a specific header/footer,
        // so we use Deflater.DEFAULT_COMPRESSION.
        return Compress(uncompressedData, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * Compresses bytes using the Deflate algorithm with a specified level.
     * This gives you control over speed vs. size.
     *
     * @param uncompressedData The raw data to compress.
     * @param compressionLevel The compression level (e.g., Deflater.BEST_SPEED,
     * Deflater.BEST_COMPRESSION).
     * @return The compressed data.
     */
    public static byte[] Compress(byte[] uncompressedData, int compressionLevel) {
        if (uncompressedData == null) return null;

        // Use DeflaterOutputStream for fine-grained control
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DeflaterOutputStream deflaterStream = new DeflaterOutputStream(byteStream,
                     new Deflater(compressionLevel))) {

            deflaterStream.write(uncompressedData);
            deflaterStream.finish();
            return byteStream.toByteArray();

        } catch (IOException e) {
            System.err.println("Error during compression: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decompresses bytes in-memory using the Inflater (GZIP/Deflate) algorithm.
     *
     * @param compressedData The data to decompress.
     * @return The original uncompressed data.
     */
    public static byte[] Decompress(byte[] compressedData) {
        if (compressedData == null) return null;

        // Use InflaterInputStream, which handles both GZIP and Deflate
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
             InflaterInputStream inflaterStream = new InflaterInputStream(byteStream);
             ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {

            // --- IMPROVEMENT ---
            // Use an 8KB buffer (8192 bytes) instead of 1KB.
            // This significantly reduces the number of read/write system calls.
            byte[] buffer = new byte[8192];
            int len;

            while ((len = inflaterStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }

            return outStream.toByteArray();

        } catch (IOException e) {
            System.err.println("Error during decompression: " + e.getMessage());
            // This may happen if the data is not compressed (e.g., not a GZIP format)
            // Or if it's corrupted.
            return null;
        }
    }
}