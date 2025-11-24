/**
 * contributed by @anup.
 */

package com.swe.ScreenNVideo.Codec;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Integration Test class for DeCompressor.
 * Uses the real AANdct and QuantisationUtil modules via the default constructor.
 */
public class DeCompressorTest {

    /**
     * Class under test.
     */
    private DeCompressor deCompressor;

    /**
     * Standard block size for JPEG dct and Quantisation
     */

    private final short BLOCK_SIZE = 8;

    /**
     * Setup method to initialize the DeCompressor with real dependencies.
     */
    @BeforeEach
    public void setUp() {
        // Use the default constructor which calls getInstance() for real modules
        deCompressor = new DeCompressor();
    }

    /**
     * To get matrix filled with random value
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
                matrix[i][j] = (short) ThreadLocalRandom.current().nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1);;

            }
        }
        return matrix;
    }

    /**
     * Test decompressChrome with a single 8x8 block.
     * Verifies that the real decompression pipeline runs successfully
     *  ( dequantisation -> idct )
     * on a standard block.
     */
    @Test
    public void testDecompressChromeSingleBlock() {
        final short[][] matrix = getRandomMatrixofNM(BLOCK_SIZE,BLOCK_SIZE);

        assertDoesNotThrow(() -> deCompressor.decompressChrome(matrix, BLOCK_SIZE, BLOCK_SIZE),
                "Decompressing a valid 8x8 block should not throw exceptions");
    }

    /**
     * Test decompressLumin with a single 8x8 block.
     * Verifies that the Luminance decompression path executes correctly
     * ( dequantisation -> idct )
     */
    @Test
    public void testDecompressLuminSingleBlock() {
        final short[][] matrix = getRandomMatrixofNM(BLOCK_SIZE,BLOCK_SIZE);

        assertDoesNotThrow(() -> deCompressor.decompressLumin(matrix, BLOCK_SIZE, BLOCK_SIZE),
                "Decompressing a valid 8x8 block should not throw exceptions");
    }

    /**
     * Test decompressChrome with a larger 16x16 matrix.
     * Verifies that the real IDCT and DeQuantization modules handle
     * the coordinate offsets passed by the DeCompressor loops correctly.
     */
    @Test
    public void testDecompressChromeMultiBlock() {
        final short[][] matrix = getRandomMatrixofNM((short)(BLOCK_SIZE << 1),(short)(BLOCK_SIZE << 1));

        assertDoesNotThrow(() -> deCompressor.decompressChrome(matrix, (short)(BLOCK_SIZE << 1), (short)(BLOCK_SIZE << 1)),
                "Decompressing a 16x16 matrix should process all blocks successfully");
    }

    /**
     * Test decompressLumin with a larger 16x16 matrix.
     * Verifies that the real IDCT and DeQuantization modules handle
     * the coordinate offsets passed by the DeCompressor loops correctly.
     */
    @Test
    public void testDecompressLuminMultiBlock() {
        final short[][] matrix = getRandomMatrixofNM((short)(BLOCK_SIZE << 1),(short)(BLOCK_SIZE << 1));

        assertDoesNotThrow(() -> deCompressor.decompressLumin(matrix, (short)(BLOCK_SIZE << 1), (short)(BLOCK_SIZE << 1)),
                "Decompressing a 16x16 matrix should process all blocks successfully");
    }

}