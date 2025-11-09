package com.swe.ScreenNVideo.Codec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Test class for Compressor.
 * This class aims for 100% line coverage of the Compressor class
 * and adheres to the project's Checkstyle rules.
 * <p>
 * It relies on stub implementations of QuantisationUtil and EncodeDecodeRLE
 * being present in the same package.
 */
public class CompressorTest {

    /**
     * The standard block size for DCT (8x8).
     */
    private static final short BLOCK_SIZE = 8;
    /**
     * A test matrix size for multi-block tests (16x16).
     */
    private static final short MATRIX_SIZE = 16;
    /**
     * A size for the test ByteBuffer.
     */
    private static final int BUFFER_CAPACITY = 1024;

    private ICompressor compressor;
    private ByteBuffer testBuffer;

    /**
     * Sets up the test environment before each test.
     * This initializes the testBuffer and, critically,
     * constructs the Compressor, covering its constructor logic.
     */
    @BeforeEach
    void setUp() {
        // Allocate a new buffer for each test
        testBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);

        // This call tests the entire constructor of Compressor.
        // It requires AANdct, QuantisationUtil, and EncodeDecodeRLE singletons.
        compressor = new Compressor();
    }

    /**
     * Tests that the Compressor singleton-based constructor runs without error.
     */
    @Test
    void testConstructor() {
        // The real test is in setUp().
        // If setUp() completes, the constructor worked.
        assertNotNull(compressor, "Compressor instance should be created successfully");
    }

    /**
     * Tests compressChrome with a 0x0 matrix.
     * This ensures the loops are skipped and zigZagRLE is still called.
     */
    @Test
    void testCompressChrome_EmptyMatrix() {
        final short[][] matrix = new short[0][0];
        final short height = 0;
        final short width = 0;

        assertEquals(0, testBuffer.position(), "Buffer should be empty before compress");
        assertDoesNotThrow(() -> compressor.compressChrome(matrix, height, width, testBuffer),
                "Compress should not throw on empty matrix");

        // Verify zigZagRLE was called (by checking our stub's side-effect)
        assertEquals(1, testBuffer.position(), "zigZagRLE should be called even for empty matrix");
    }

    /**
     * Tests compressChrome with a single 8x8 block.
     * This ensures the loops are entered once.
     */
    @Test
    void testCompressChrome_SingleBlock() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        assertEquals(0, testBuffer.position(), "Buffer should be empty before compress");
        compressor.compressChrome(matrix, BLOCK_SIZE, BLOCK_SIZE, testBuffer);

        // Verify zigZagRLE was called
        assertEquals(1, testBuffer.position(), "zigZagRLE should be called after processing");
    }

    /**
     * Tests compressChrome with a 16x16 matrix (4 blocks).
     * This ensures the loops iterate correctly.
     */
    @Test
    void testCompressChrome_MultiBlock() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];

        assertEquals(0, testBuffer.position(), "Buffer should be empty before compress");
        compressor.compressChrome(matrix, MATRIX_SIZE, MATRIX_SIZE, testBuffer);

        // Verify zigZagRLE was called
        assertEquals(1, testBuffer.position(), "zigZagRLE should be called after processing");
    }

    /**
     * Tests compressLumin with a 0x0 matrix.
     * This ensures the loops are skipped and zigZagRLE is still called.
     */
    @Test
    void testCompressLumin_EmptyMatrix() {
        final short[][] matrix = new short[0][0];
        final short height = 0;
        final short width = 0;

        assertEquals(0, testBuffer.position(), "Buffer should be empty before compress");
        assertDoesNotThrow(() -> compressor.compressLumin(matrix, height, width, testBuffer),
                "Compress should not throw on empty matrix");

        // Verify zigZagRLE was called (by checking our stub's side-effect)
        assertEquals(1, testBuffer.position(), "zigZagRLE should be called even for empty matrix");
    }

    /**
     * Tests compressLumin with a single 8x8 block.
     * This ensures the loops are entered once.
     */
    @Test
    void testCompressLumin_SingleBlock() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        assertEquals(0, testBuffer.position(), "Buffer should be empty before compress");
        compressor.compressLumin(matrix, BLOCK_SIZE, BLOCK_SIZE, testBuffer);

        // Verify zigZagRLE was called
        assertEquals(1, testBuffer.position(), "zigZagRLE should be called after processing");
    }

    /**
     * Tests compressLumin with a 16x16 matrix (4 blocks).
     * This ensures the loops iterate correctly.
     */
    @Test
    void testCompressLumin_MultiBlock() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];

        assertEquals(0, testBuffer.position(), "Buffer should be empty before compress");
        compressor.compressLumin(matrix, MATRIX_SIZE, MATRIX_SIZE, testBuffer);

        // Verify zigZagRLE was called
        assertEquals(1, testBuffer.position(), "zigZagRLE should be called after processing");
    }
}
