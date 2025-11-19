/**
 * Contributed by @Devansh-Kesan
 */

package com.swe.ScreenNVideo.Codec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive test suite for EncodeDecodeRLE class.
 * Tests ZigZag scanning and Run-Length Encoding (RLE)
 * compression and decompression for 8x8 short matrices.
 */
public class EncodeDecodeRLETest {

    /**
     * Block size for processing matrices (8x8 blocks).
     */
    private static final int BLOCK_SIZE = 8;

    /**
     * Single block dimension.
     */
    private static final int SINGLE_BLOCK_DIM = 8;

    /**
     * Double block dimension.
     */
    private static final int DOUBLE_BLOCK_DIM = 16;

    /**
     * Triple block dimension.
     */
    private static final int TRIPLE_BLOCK_DIM = 24;

    /**
     * Quad block dimension.
     */
    private static final int QUAD_BLOCK_DIM = 32;

    /**
     * Buffer size for encoding operations.
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * Test value for constant matrices.
     */
    private static final short TEST_VALUE = 100;

    /**
     * Zero value constant.
     */
    private static final short ZERO_VALUE = 0;

    /**
     * Encoder instance for testing.
     */
    private EncodeDecodeRLE encoder;

    /**
     * Sets up test fixture before each test.
     */
    @BeforeEach
    public void setUp() {
        encoder = EncodeDecodeRLE.getInstance();
    }

    /**
     * Tests that getInstance returns a non-null singleton instance.
     */
    @Test
    public void testGetInstanceNotNull() {
        assertNotNull(encoder);
    }

    /**
     * Tests that getInstance always returns the same singleton instance.
     */
    @Test
    public void testGetInstanceSingleton() {
        final EncodeDecodeRLE instance1 = EncodeDecodeRLE.getInstance();
        final EncodeDecodeRLE instance2 = EncodeDecodeRLE.getInstance();
        assertSame(instance1, instance2);
    }

    /**
     * Tests zigZagRLE with single 8x8 zero matrix.
     */
    @Test
    public void testZigZagRLESingleBlockZero() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        encoder.zigZagRLE(matrix, buffer);

        assertTrue(buffer.position() > 0);
        assertEquals(SINGLE_BLOCK_DIM, buffer.getShort(0));
        assertEquals(SINGLE_BLOCK_DIM, buffer.getShort(2));
    }

    /**
     * Tests zigZagRLE with single 8x8 constant matrix.
     */
    @Test
    public void testZigZagRLESingleBlockConstant() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests zigZagRLE with 16x16 matrix (4 blocks).
     */
    @Test
    public void testZigZagRLEMultipleBlocks() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = 50;
            }
        }

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests zigZagRLE with 24x24 matrix (9 blocks).
     */
    @Test
    public void testZigZagRLENineBlocks() {
        final short[][] matrix = new short[TRIPLE_BLOCK_DIM][TRIPLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < TRIPLE_BLOCK_DIM; i++) {
            for (int j = 0; j < TRIPLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) (i + j);
            }
        }

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests zigZagRLE with 32x32 matrix (16 blocks).
     */
    @Test
    public void testZigZagRLELargeMatrix() {
        final short[][] matrix = new short[QUAD_BLOCK_DIM][QUAD_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE * 2);

        for (int i = 0; i < QUAD_BLOCK_DIM; i++) {
            for (int j = 0; j < QUAD_BLOCK_DIM; j++) {
                matrix[i][j] = (short) ((i * j) % 256);
            }
        }

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests revZigZagRLE decodes single 8x8 zero matrix correctly.
     */
    @Test
    public void testRevZigZagRLESingleBlockZero() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        encoder.zigZagRLE(matrix, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertNotNull(decoded);
        assertEquals(SINGLE_BLOCK_DIM, decoded.length);
        assertEquals(SINGLE_BLOCK_DIM, decoded[0].length);
    }

    /**
     * Tests revZigZagRLE decodes single 8x8 constant matrix correctly.
     */
    @Test
    public void testRevZigZagRLESingleBlockConstant() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }

        encoder.zigZagRLE(matrix, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertNotNull(decoded);
        assertEquals(SINGLE_BLOCK_DIM, decoded.length);
        assertEquals(SINGLE_BLOCK_DIM, decoded[0].length);
    }

    /**
     * Tests round-trip encoding and decoding with zero matrix.
     */
    @Test
    public void testRoundTripZeroMatrix() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests round-trip encoding and decoding with constant matrix.
     */
    @Test
    public void testRoundTripConstantMatrix() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                original[i][j] = TEST_VALUE;
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests round-trip encoding and decoding with gradient pattern.
     */
    @Test
    public void testRoundTripGradientPattern() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                original[i][j] = (short) (i * BLOCK_SIZE + j);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests round-trip encoding and decoding with 16x16 matrix.
     */
    @Test
    public void testRoundTripMultipleBlocks() {
        final short[][] original = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                original[i][j] = (short) ((i + j) * 10);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests round-trip encoding and decoding with checkerboard pattern.
     */
    @Test
    public void testRoundTripCheckerboard() {
        final short[][] original = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                original[i][j] = (short) ((i + j) % 2 == 0 ? 255 : 0);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests round-trip encoding and decoding with negative values.
     */
    @Test
    public void testRoundTripNegativeValues() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                original[i][j] = -100;
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests round-trip encoding and decoding with mixed values.
     */
    @Test
    public void testRoundTripMixedValues() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                original[i][j] = (short) ((i + j) % 2 == 0 ? 100 : -100);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests round-trip encoding and decoding with 24x24 matrix.
     */
    @Test
    public void testRoundTripNineBlocks() {
        final short[][] original = new short[TRIPLE_BLOCK_DIM][TRIPLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < TRIPLE_BLOCK_DIM; i++) {
            for (int j = 0; j < TRIPLE_BLOCK_DIM; j++) {
                original[i][j] = (short) (i * 10 + j);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests round-trip encoding and decoding with 32x32 matrix.
     */
    @Test
    public void testRoundTripLargeMatrix() {
        final short[][] original = new short[QUAD_BLOCK_DIM][QUAD_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE * 2);

        for (int i = 0; i < QUAD_BLOCK_DIM; i++) {
            for (int j = 0; j < QUAD_BLOCK_DIM; j++) {
                original[i][j] = (short) ((i * j) % 256);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests zigZagRLE with diagonal pattern.
     */
    @Test
    public void testZigZagRLEDiagonalPattern() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            matrix[i][i] = TEST_VALUE;
        }

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests round-trip with diagonal pattern.
     */
    @Test
    public void testRoundTripDiagonalPattern() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            original[i][i] = TEST_VALUE;
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests zigZagRLE with alternating rows.
     */
    @Test
    public void testZigZagRLEAlternatingRows() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) (i % 2 == 0 ? 100 : 50);
            }
        }

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests round-trip with alternating rows.
     */
    @Test
    public void testRoundTripAlternatingRows() {
        final short[][] original = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                original[i][j] = (short) (i % 2 == 0 ? 100 : 50);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests zigZagRLE with maximum short values.
     */
    @Test
    public void testZigZagRLEMaxValues() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = Short.MAX_VALUE;
            }
        }

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests round-trip with maximum short values.
     */
    @Test
    public void testRoundTripMaxValues() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                original[i][j] = Short.MAX_VALUE;
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests zigZagRLE with minimum short values.
     */
    @Test
    public void testZigZagRLEMinValues() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = Short.MIN_VALUE;
            }
        }

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests round-trip with minimum short values.
     */
    @Test
    public void testRoundTripMinValues() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                original[i][j] = Short.MIN_VALUE;
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests zigZagRLE with sparse non-zero values.
     */
    @Test
    public void testZigZagRLESparseValues() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        matrix[0][0] = TEST_VALUE;
        matrix[7][7] = TEST_VALUE;
        matrix[15][15] = TEST_VALUE;

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests round-trip with sparse non-zero values.
     */
    @Test
    public void testRoundTripSparseValues() {
        final short[][] original = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        original[0][0] = TEST_VALUE;
        original[7][7] = TEST_VALUE;
        original[15][15] = TEST_VALUE;

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests zigZagRLE encodes dimensions correctly.
     */
    @Test
    public void testZigZagRLEStoresDimensions() {
        final short[][] matrix = new short[TRIPLE_BLOCK_DIM][QUAD_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE * 2);

        encoder.zigZagRLE(matrix, buffer);

        assertEquals(TRIPLE_BLOCK_DIM, buffer.getShort(0));
        assertEquals(QUAD_BLOCK_DIM, buffer.getShort(2));
    }

    /**
     * Tests round-trip with non-square matrix 24x32.
     */
    @Test
    public void testRoundTripNonSquareMatrix() {
        final short[][] original = new short[TRIPLE_BLOCK_DIM][QUAD_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE * 2);

        for (int i = 0; i < TRIPLE_BLOCK_DIM; i++) {
            for (int j = 0; j < QUAD_BLOCK_DIM; j++) {
                original[i][j] = (short) ((i * j) % 200);
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests round-trip with sequential values.
     */
    @Test
    public void testRoundTripSequentialValues() {
        final short[][] original = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        short value = 0;
        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                original[i][j] = value++;
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }

    /**
     * Tests zigZagRLE with boundary conditions at matrix edges.
     */
    @Test
    public void testZigZagRLEBoundaryConditions() {
        final short[][] matrix = new short[TRIPLE_BLOCK_DIM][TRIPLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < TRIPLE_BLOCK_DIM; i++) {
            for (int j = 0; j < TRIPLE_BLOCK_DIM; j++) {
                if (i == 0 || i == TRIPLE_BLOCK_DIM - 1
                        || j == 0 || j == TRIPLE_BLOCK_DIM - 1) {
                    matrix[i][j] = TEST_VALUE;
                }
            }
        }

        encoder.zigZagRLE(matrix, buffer);
        assertTrue(buffer.position() > 0);
    }

    /**
     * Tests round-trip with boundary conditions.
     */
    @Test
    public void testRoundTripBoundaryConditions() {
        final short[][] original = new short[TRIPLE_BLOCK_DIM][TRIPLE_BLOCK_DIM];
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        for (int i = 0; i < TRIPLE_BLOCK_DIM; i++) {
            for (int j = 0; j < TRIPLE_BLOCK_DIM; j++) {
                if (i == 0 || i == TRIPLE_BLOCK_DIM - 1
                        || j == 0 || j == TRIPLE_BLOCK_DIM - 1) {
                    original[i][j] = TEST_VALUE;
                }
            }
        }

        encoder.zigZagRLE(original, buffer);
        buffer.flip();

        final short[][] decoded = encoder.revZigZagRLE(buffer);

        assertArrayEquals(original, decoded);
    }
}

