/**
 * contributed by @anup.
 */
package com.swe.ScreenNVideo.Codec;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Integration Test class for Compressor.
 * Uses the real AANdct and QuantisationUtil modules via the default constructor.
 */
public class CompressorTest {

    /**
     * Class under test.
     */
    private Compressor compressor;

    /**
     * Standard block size for JPEG dct and Quantisation
     */

    private final short BLOCK_SIZE = 8;

    /**
     * Setup method to initialize the Compressor with real dependencies.
     */
    @BeforeEach
    public void setUp() {
        // Use the default constructor which calls getInstance() for real modules
        compressor = new Compressor();
    }

    /**
     * To get matrix filled with value of range [-128, 128]
     * standard data points for dct.
     * @param height of matrix
     * @param width of matrix
     * @return generated matrix.
     */
    short[][] getRandomMatrixofNM(short height, short width){
        short[][] matrix = new short[height][width];
        // Fill with some dummy data
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                matrix[i][j] = (short) ThreadLocalRandom.current().nextInt(-128, 129);

            }
        }
        return matrix;
    }

    /**
     * Test compressChrome with a single 8x8 block.
     * Verifies that the dct -> quantisation compression pipeline runs successfully
     * on a standard block without throwing exceptions.
     */
    @Test
    public void testCompressChromeSingleBlock() {
        final short[][] matrix = getRandomMatrixofNM(BLOCK_SIZE, BLOCK_SIZE) ;

        assertDoesNotThrow(() -> compressor.compressChrome(matrix, BLOCK_SIZE, BLOCK_SIZE),
                "Compressing a valid 8x8 block should not throw exceptions");
    }

    /**
     * Test compressLumin with a single 8x8 block.
     * Verifies that the Luminance compression path executes correctly
     * using the real Quantization tables.
     */
    @Test
    public void testCompressLuminSingleBlock() {
        final short[][] matrix = getRandomMatrixofNM(BLOCK_SIZE, BLOCK_SIZE) ;

        assertDoesNotThrow(() -> compressor.compressLumin(matrix, BLOCK_SIZE, BLOCK_SIZE),
                "Compressing a valid 8x8 block should not throw exceptions");
    }

    /**
     * Test compressChrome with a 16x16 matrix (four 8x8 blocks).
     *  Verifies that the loop logic correctly feeds multiple 8x8 sub-blocks
     * to the real FDCT and Quantization algorithms.
     */
    @Test
    public void testCompressChromeMultiBlock() {
        final short[][] matrix = getRandomMatrixofNM((short) (BLOCK_SIZE << 1), (short) (BLOCK_SIZE << 1)) ;

        assertDoesNotThrow(() -> compressor.compressChrome(matrix,(short) (BLOCK_SIZE << 1), (short) (BLOCK_SIZE << 1)),
                "Compressing a 16x16 matrix should process all blocks successfully");
    }

    /**
     * Test compressLumin with a 16x16 matrix (four 8x8 blocks).
     *  Verifies that the loop logic correctly feeds multiple 8x8 sub-blocks
     * to the real FDCT and Quantization algorithms.
     */
    @Test
    public void testCompressLuminMultiBlock() {
        final short[][] matrix = getRandomMatrixofNM((short) (BLOCK_SIZE << 1), (short) (BLOCK_SIZE << 1)) ;

        assertDoesNotThrow(() -> compressor.compressLumin(matrix,(short) (BLOCK_SIZE << 1), (short) (BLOCK_SIZE << 1)),
                "Compressing a 16x16 matrix should process all blocks successfully");
    }

}