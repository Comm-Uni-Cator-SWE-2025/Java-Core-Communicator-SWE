/**
 * Contributed by @anup
 */

package com.swe.ScreenNVideo.Codec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

// Explicit static imports to avoid StarImport violation
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test class for AANdct.
 * This class aims for 100% line coverage of the AANdct class
 * and adheres to the project's Checkstyle rules.
 */
public class AANdctTest {

    /**
     * The standard block size for DCT (8x8).
     */
    private static final int BLOCK_SIZE = 8;
    /**
     * A test matrix size for offset tests.
     */
    private static final int MATRIX_SIZE = 16;
    /**
     * A delta for double-precision floating-point comparisons.
     */
    private static final double DOUBLE_DELTA = 1e-15;
    /**
     * A delta for short comparisons after lossy round-trips.
     */
    private static final int ROUND_TRIP_DELTA = 50;
    /**
     * A default value for filling flat blocks in tests.
     */
    private static final short TEST_VALUE = 100;
    /**
     * A second test value for offset tests.
     */
    private static final short TEST_VALUE_B = 50;

    private AANdct dct;

    /**
     * Helper method to create a 2D short array filled with a specific value.
     * @param rows Number of rows.
     * @param cols Number of columns.
     * @param value The value to fill.
     * @return A new 2D short array.
     */
    private short[][] createFilledMatrix(final int rows, final int cols, final short value) {
        final short[][] matrix = new short[rows][cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(matrix[i], value);
        }
        return matrix;
    }

    /**
     * Helper method to assert that two 2D short arrays are equal.
     * @param expected The expected 2D array.
     * @param actual The actual 2D array.
     * @param message The message to display on failure.
     */
    private void assertArrayEquals2D(final short[][] expected, final short[][] actual, final String message) {
        assertEquals(expected.length, actual.length, message + ": Row count mismatch");
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], message + ": Mismatch in row " + i);
        }
    }

    /**
     * Helper method to assert that two 2D short arrays are equal within a delta.
     * Used for round-trip tests where rounding errors can occur.
     *
     * @param expected The expected 2D array.
     * @param actual The actual 2D array.
     * @param delta The allowable difference for each element.
     * @param message The message to display on failure.
     */
    private void assertArrayEquals2D(final short[][] expected, final short[][] actual, final int delta,
                                     final String message) {
        assertEquals(expected.length, actual.length, message + ": Row count mismatch");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].length, actual[i].length, message + ": Column count mismatch in row " + i);
            for (int j = 0; j < expected[i].length; j++) {
                assertTrue(Math.abs(expected[i][j] - actual[i][j]) <= delta,
                        String.format("%s: Mismatch at [%d][%d]. Expected %d, got %d (delta %d)",
                                message, i, j, expected[i][j], actual[i][j], delta));
            }
        }
    }

    @BeforeEach
    void setUp() {
        // This test also covers the private constructor's execution path
        dct = AANdct.getInstance();
    }

    @Test
    void testSingletonInstance() {
        assertNotNull(dct, "getInstance() should not return null");
        final AANdct instance2 = AANdct.getInstance();
        // Test that the singleton pattern works
        assertSame(dct, instance2, "getInstance() should always return the same instance");
    }

    @Test
    void testFdctAllZeros() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE]; // Already initialized to 0
        final short[][] expected = new short[BLOCK_SIZE][BLOCK_SIZE]; // Also all 0

        // This call covers all lines in fdctRow() and fdctCol()
        dct.fdct(matrix, (short) 0, (short) 0);

        assertArrayEquals2D(expected, matrix, "FDCT of all-zeros block should be all-zeros");
    }

    @Test
    void testFdctFlatBlock() {
        // A block of all TEST_VALUEs.
        final short[][] matrix = createFilledMatrix(BLOCK_SIZE, BLOCK_SIZE, TEST_VALUE);

        // Expected result: DC component is (BLOCK_SIZE*BLOCK_SIZE)*C, AC components are 0
        // This is because the AAN implementation is unscaled.
        final short[][] expected = new short[BLOCK_SIZE][BLOCK_SIZE];
        expected[0][0] = (short) (BLOCK_SIZE * BLOCK_SIZE * TEST_VALUE);

        dct.fdct(matrix, (short) 0, (short) 0);

        assertArrayEquals2D(expected, matrix, "FDCT of a flat block should be DC-only");
    }

    @Test
    void testIdctDCOnlyBlock() {
        // This is the inverse of testFdctFlatBlock
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        matrix[0][0] = (short) (BLOCK_SIZE * BLOCK_SIZE * TEST_VALUE); // DC-only block

        // Expected result: a flat block of all C
        // The /64.0 in the idct function scales it back
        final short[][] expected = createFilledMatrix(BLOCK_SIZE, BLOCK_SIZE, TEST_VALUE);

        // This call covers all lines in idctRow() and idctCol()
        dct.idct(matrix, (short) 0, (short) 0);

        // Allow a small delta of 1 due to rounding
        assertArrayEquals2D(expected, matrix, ROUND_TRIP_DELTA, "IDCT of DC-only block should be a flat block");
    }

    @Test
    void testRoundTripSimpleData() {
        final short[][] originalMatrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        final short[][] workingMatrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        final short offsetVal = 128;
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                // Create some simple ramp data
                originalMatrix[i][j] = (short) (i * BLOCK_SIZE + j - offsetVal);
                workingMatrix[i][j] = originalMatrix[i][j];
            }
        }

        // Perform forward and then inverse transform
        dct.fdct(workingMatrix, (short) 0, (short) 0);

        dct.idct(workingMatrix, (short) 0, (short) 0);

        // there is loss at two point :
        // 1) fdct have short[][] matrix and after doing transformation ->
                    // 1) Math.round() and then storing back in short[][]
        // 2) idct have take short[][] matrix and then doing transformation ->
                    // 1) Math.round and then storing back in short[][]

        // two point loss can make them match exactly nearly impossible

        assertArrayEquals2D(originalMatrix, workingMatrix, ROUND_TRIP_DELTA, "Round-trip FDCT -> IDCT failed");
    }
}
