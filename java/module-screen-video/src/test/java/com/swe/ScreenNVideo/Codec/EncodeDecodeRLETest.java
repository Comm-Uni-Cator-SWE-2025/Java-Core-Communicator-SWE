/**
 * Contributed by Devansh Manoj Kesan.
 * Comprehensive test suite for EncodeDecodeRLE class covering 100% of the code.
 */

package com.swe.ScreenNVideo.Codec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprehensive test suite for EncodeDecodeRLE class.
 * Tests ZigZag scanning and Run-Length Encoding (RLE)
 * compression and decompression for 8x8 short matrices.
 */
public class EncodeDecodeRLETest {

    private static final int BLOCK_SIZE = 8;
    private static final int SINGLE_BLOCK_DIM = 8;
    private static final int MULTI_BLOCK_DIM = 16;
    private static final int BUFFER_CAPACITY = 4096;
    private static final short TEST_VALUE_HIGH = 100;
    private static final short TEST_VALUE_LOW = 10;
    private EncodeDecodeRLE encoder;

    /**
     * Helper method to compare two matrices element by element.
     */
    private void assertMatrixEquals(final short[][] expected, final short[][] actual, final String message) {
        assertEquals(expected.length, actual.length, message + " - row count mismatch");

        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i],
                message + " - mismatch at row " + i);
        }
    }

    /**
     * Sets up test fixture before each test.
     */
    @BeforeEach
    public void setUp() {
        encoder = EncodeDecodeRLE.getInstance();
    }

    /**
     * Tests that getInstance returns a non-null singleton instance. (Covers getInstance)
     */
    @Test
    public void testGetInstanceSingleton() {
        assertNotNull(encoder, "getInstance() should return a non-null instance.");
        final EncodeDecodeRLE instance2 = EncodeDecodeRLE.getInstance();
        assertSame(encoder, instance2, "getInstance() should always return the same singleton instance.");
    }

    /**
     * Tests round-trip encoding and decoding with an all-zero matrix.
     * This hits the maximum compression path (long runs of 0s).
     */
    @Test
    public void testRoundTripZeroMatrix() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertMatrixEquals(original, decoded, "Zero matrix round-trip failed.");
    }

    /**
     * Tests round-trip encoding and decoding with a constant non-zero matrix.
     * This hits the maximum compression path (long runs of non-0s).
     */
    @Test
    public void testRoundTripConstantMatrix() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            Arrays.fill(original[i], TEST_VALUE_HIGH);
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertMatrixEquals(original, decoded, "Constant matrix round-trip failed.");
    }

    /**
     * Tests round-trip with mixed positive and negative values.
     * Ensures correct handling of the full short range.
     */
    @Test
    public void testRoundTripMixedValues() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                original[i][j] = (short) ThreadLocalRandom.current().nextInt(-128, 127);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertMatrixEquals(original, decoded, "Mixed value matrix round-trip failed.");
    }

    /**
     * Tests a checkerboard pattern (alternating values).
     * This forces the worst-case RLE (minimal compression) and ensures
     * diagonal traversal and value changes are handled correctly.
     */
    @Test
    public void testRoundTripCheckerboard() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                // Alternating values forces the 'else' branch (RLE pair write) almost every time
                original[i][j] = ((i + j) % 2 == 0 ? TEST_VALUE_HIGH : TEST_VALUE_LOW);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertMatrixEquals(original, decoded, "Checkerboard round-trip failed.");
    }

    /**
     * Tests round-trip with a large matrix (16x16 = 4 blocks).
     * This verifies the outer loop iteration logic in `zigZagRLE` and `revZigZagRLE`.
     */
    @Test
    public void testRoundTripMultiBlockAligned() {
        final short[][] original = new short[MULTI_BLOCK_DIM][MULTI_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        // Fill with distinct values per block to ensure correct block boundaries
        for (int i = 0; i < MULTI_BLOCK_DIM; i++) {
            for (int j = 0; j < MULTI_BLOCK_DIM; j++) {
                // Value depends on which 8x8 block it's in (i/8, j/8)
                original[i][j] = (short) ((i / BLOCK_SIZE) * 10 + (j / BLOCK_SIZE) * 5 + i * 2 + j);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertMatrixEquals(original, decoded, "Multi-block 16x16 round-trip failed.");
    }

    /**
     * Tests round-trip with a non-square, but block-aligned matrix (16x8 = 2 blocks).
     * Ensures correct handling of loops and dimensions when width != height.
     */
    @Test
    public void testRoundTripNonSquareMatrixAligned() {
        final short[][] original = new short[MULTI_BLOCK_DIM][SINGLE_BLOCK_DIM]; // 16x8
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        for (int i = 0; i < MULTI_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                original[i][j] = (short) (i * 2 + j);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertMatrixEquals(original, decoded, "Non-square 16x8 matrix round-trip failed.");
    }

    /**
     * Tests an 8x16 matrix (2 blocks).
     * Ensures correct block boundaries and dimension handling.
     */
    @Test
    public void testRoundTripNonSquareMatrixAlignedWide() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][MULTI_BLOCK_DIM]; // 8x16
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < MULTI_BLOCK_DIM; j++) {
                original[i][j] = (short) (i + j * 2);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertMatrixEquals(original, decoded, "Non-square 8x16 matrix round-trip failed.");
    }

    /**
     * Tests a non-8x8 aligned matrix (e.g., 9x9).
     * This forces the boundary checks (`if (r >= matrix.length || c >= matrix[0].length)`)
     * inside `encodeBlock` and `decodeBlock` to be exercised.
     */
    @Test
    public void testRoundTripNon8x8AlignedMatrix() {
        final short nonAlignedDim = 9;
        final short[][] original = new short[nonAlignedDim][nonAlignedDim];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        for (int i = 0; i < nonAlignedDim; i++) {
            for (int j = 0; j < nonAlignedDim; j++) {
                original[i][j] = (short) (i * nonAlignedDim + j);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertMatrixEquals(original, decoded, "Non-8x8 aligned matrix round-trip failed.");
    }

    /**
     * Tests the buffer underflow exception path inside `decodeBlock`.
     */
    @Test
    public void testDecodeBufferUnderflow() {
        // Create a minimal encoded matrix that *should* contain one RLE pair (4 bytes of payload).
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        original[0][0] = TEST_VALUE_HIGH;
        final ByteBuffer encodedBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        encoder.zigZagRLE(original, encodedBuffer);

        // Get the bytes: [height(2), width(2), RLE_payloads...]
        final byte[] fullEncoded = Arrays.copyOf(encodedBuffer.array(), encodedBuffer.position());

        // --- 1. Test case: Not enough bytes for RLE pair (less than 4)
        // Buffer has: height(2) + width(2) + 3 bytes from the RLE payload (total 7 bytes)
        final ByteBuffer shortBuffer = ByteBuffer.wrap(fullEncoded, 0, 7);

        // The decoder will read height(2) and width(2), leaving 3 bytes in the buffer.
        // It enters decodeBlock, tries to read the first RLE pair, and fails.
        assertThrows(RuntimeException.class, () -> encoder.revZigZagRLE(shortBuffer),
            "Expected Buffer underflow when fewer than 4 bytes remain for RLE pair.");
    }

}