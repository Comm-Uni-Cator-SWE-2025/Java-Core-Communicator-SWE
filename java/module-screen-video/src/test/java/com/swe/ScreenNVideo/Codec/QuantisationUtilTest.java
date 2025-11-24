/**
 * contributed by @anup.
 */

package com.swe.ScreenNVideo.Codec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

// Explicit static imports to avoid StarImport violation
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test class for QuantisationUtil.
 */
public class QuantisationUtilTest {

    /**
     * The standard block size for DCT (8x8).
     */
    private static final int BLOCK_SIZE = 8;

    /**
     * A test value for quantization.
     */
    private static final short TEST_VALUE = 200;

    /**
     * dummy input matrix used every time for each test case
     */
    private final short[][] inputMatrix = new short[BLOCK_SIZE][BLOCK_SIZE];

    private QuantisationUtil quantUtil;

    /**
     *initialise input matrix for each test.
     */
    private void intitMatrix() {
        for(int i = 0;i < BLOCK_SIZE; ++i ) {
            for(int j = 0; j < BLOCK_SIZE; ++j) {
                inputMatrix[i][j] = (short) (TEST_VALUE / (i+j+1));
            }
        }
    }

    /**
     *
     * @return the copy of initial matrix
     */
    private short[][] getCopyofInputMatrix(){

        short[][] copyInitMatrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for(int i = 0; i < BLOCK_SIZE; ++i) {
            System.arraycopy(inputMatrix[i], 0, copyInitMatrix[i], 0, BLOCK_SIZE);
        }

        return copyInitMatrix;
    }

    /**
     * Helper method to get a private field value using reflection.
     * Used to inspect the internal state of the singleton.
     *
     * @param target    The object to inspect.
     * @param fieldName The name of the field to get.
     * @return The value of the field.
     * @throws Exception if reflection fails.
     */
    private Object getPrivateField(final Object target, final String fieldName) throws Exception {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @BeforeEach
    void setUp() {
        // This call covers the private constructor's execution path
        quantUtil = QuantisationUtil.getInstance();
    }

    @Test
    void testGetInstanceAndInitialState() {
        assertNotNull(quantUtil, "getInstance() should not return null");
        final QuantisationUtil instance2 = QuantisationUtil.getInstance();
        // Test that the singleton pattern works
        assertSame(quantUtil, instance2, "getInstance() should always return the same instance");

    }

    @Test
    void testQuantAndDeQuantLumin() throws Exception {
        intitMatrix();
        // Get the internal scaled table to predict the result
        final float[][] scaledLumin = (float[][]) getPrivateField(quantUtil, "scaledQuantLumin");

        // copy the input matrix to test
        final short[][] initialMatrix = getCopyofInputMatrix();

        // 1. Test quantisationLumin
        quantUtil.quantisationLumin(inputMatrix, (short) 0, (short) 0);

        // Check that the target 8x8 block was modified
        for(int i = 0; i < BLOCK_SIZE; ++i) {
            for(int j = 0; j < BLOCK_SIZE; ++j) {
                assertEquals((short) Math.round(initialMatrix[i][j]/scaledLumin[i][j]), inputMatrix[i][j],
                        "quantisationLumin did not produce expected value at row : + " + i + " and col : "  + j);
            }
        }



        // 2. Test deQuantisationLumin
        // After quantisation, many coefficients become zero due to division by the
        // quantisation table values. Once a coefficient becomes zero, dequantisation
        // cannot recover the original value — that information is permanently lost.
        // The DC coefficient, however, is usually much larger and is divided by a
        // small quantisation value, so it typically survives quantisation and can be
        // accurately reconstructed during dequantisation.

        final short dcquantVal = (short) Math.round((TEST_VALUE/scaledLumin[0][0]));
        final short dcdequantVal = (short) Math.round(dcquantVal * scaledLumin[0][0]);

        quantUtil.deQuantisationLumin(inputMatrix, (short) 0, (short) 0);

        assertEquals(dcdequantVal , inputMatrix[0][0],"deQuantisationLumin is not producing correct dc value ");
    }

    @Test
    void testQuantAndDeQuantChrome() throws Exception {
        intitMatrix();
        // Get the internal scaled table to predict the result
        final float[][] scaledChrome = (float[][]) getPrivateField(quantUtil, "scaledQuantChrome");

        // copy the input matrix to test
        final short[][] initialMatrix = getCopyofInputMatrix();

        // 1. Test quantisationChrome
        quantUtil.quantisationChrome(inputMatrix, (short) 0, (short) 0);

        // Check that the target 8x8 block was modified
        for(int i = 0; i < BLOCK_SIZE; ++i) {
            for(int j = 0; j < BLOCK_SIZE; ++j) {
                assertEquals((short) Math.round(initialMatrix[i][j]/scaledChrome[i][j]), inputMatrix[i][j],
                        "quantisationChrome did not produce expected value at row : + " + i + " and col : "  + j);
            }
        }

        // 2. Test deQuantisationChrome
        // After quantisation, many coefficients become zero due to division by the
        // quantisation table values. Once a coefficient becomes zero, dequantisation
        // cannot recover the original value — that information is permanently lost.
        // The DC coefficient, however, is usually much larger and is divided by a
        // small quantisation value, so it typically survives quantisation and can be
        // accurately reconstructed during dequantisation.

        final short dcquantVal = (short) Math.round((TEST_VALUE/scaledChrome[0][0]));
        final short dcdequantVal = (short) Math.round(dcquantVal * scaledChrome[0][0]);

        quantUtil.deQuantisationChrome(inputMatrix, (short) 0, (short) 0);

        assertEquals(dcdequantVal , inputMatrix[0][0],"deQuantisationChrome is not producing correct dc value ");

    }

    private static final int[] QUALITY_VALUES = {1, 49, 50, 55, 99, 100};

    @Test
    void testQuantAndDequantForQualityq() throws  Exception{
        for(int quality : QUALITY_VALUES) {
            quantUtil.setCompressonResolution(quality);
            testQuantAndDeQuantLumin();
            testQuantAndDeQuantChrome();
        }
    }
}
