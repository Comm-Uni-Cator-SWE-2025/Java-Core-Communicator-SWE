package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import org.junit.jupiter.api.Test;

class UtilitiesTest {

    @Test
    void compressReturnsNullForNullInput() {
        assertNull(Utilities.Compress(null, Deflater.BEST_SPEED));
    }

    @Test
    void compressThenDecompressRoundTripsData() {
        byte[] original = "roundtrip-data".getBytes(StandardCharsets.UTF_8);

        byte[] compressed = Utilities.Compress(original, Deflater.BEST_COMPRESSION);
        byte[] decompressed = Utilities.Decompress(compressed);

        assertArrayEquals(original, decompressed);
    }

    @Test
    void decompressReturnsNullForNullInput() {
        assertNull(Utilities.Decompress(null));
    }

    @Test
    void decompressReturnsNullForCorruptedInput() {
        byte[] invalid = {0, 1, 2, 3};

        byte[] result = Utilities.Decompress(invalid);

        assertNull(result);
    }

    @Test
    void compressHonorsCompressionLevel() {
        byte[] content = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes(StandardCharsets.UTF_8);
        byte[] fast = Utilities.Compress(content, Deflater.BEST_SPEED);
        byte[] tight = Utilities.Compress(content, Deflater.BEST_COMPRESSION);

        // BEST_COMPRESSION should not produce larger output than BEST_SPEED for repeated data.
        assertTrue(tight.length <= fast.length);
    }
}

