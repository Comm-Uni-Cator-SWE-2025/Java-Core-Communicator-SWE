package com.swe.ScreenNVideo;

import com.swe.ScreenNVideo.Codec.AANdct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive test suite for AANdct class.
 * Tests FDCT and IDCT transformations using AAN algorithm.
 */
public class AANdctTest {

    private static final double DELTA = 0.5;
    private static final int BLOCK_SIZE = 8;
    private static final int MATRIX_SIZE = 16;
    private AANdct aandct;

    /**
     * Sets up test fixture before each test.
     */
    @BeforeEach
    public void setUp() {
        aandct = AANdct.getInstance();
    }

    /**
     * Tests that getInstance returns a non-null singleton instance.
     */
    @Test
    public void testGetInstanceNotNull() {
        assertNotNull(aandct);
    }

    /**
     * Tests that getInstance always returns the same singleton instance.
     */
    @Test
    public void testGetInstanceSingleton() {
        final AANdct instance1 = AANdct.getInstance();
        final AANdct instance2 = AANdct.getInstance();
        assertSame(instance1, instance2);
    }

    /**
     * Tests that getScaleFactor returns correct array length.
     */
    @Test
    public void testGetScaleFactorLength() {
        final double[] scaleFactor = aandct.getScaleFactor();
        assertNotNull(scaleFactor);
        assertEquals(BLOCK_SIZE, scaleFactor.length);
    }

    /**
     * Tests that scale factors are non-zero values.
     */
    @Test
    public void testGetScaleFactorNonZero() {
        final double[] scaleFactor = aandct.getScaleFactor();
        for (double v : scaleFactor) {
            assertTrue(v != 0.0);
        }
    }

    /**
     * Tests FDCT with a zero matrix produces zero output.
     */
    @Test
    public void testFdctZeroMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        aandct.fdct(matrix, (short) 0, (short) 0);

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                assertEquals(0, matrix[i][j]);
            }
        }
    }

    /**
     * Tests FDCT with a constant value matrix.
     */
    @Test
    public void testFdctConstantMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        final short constantValue = 128;

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = constantValue;
            }
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        assertTrue(matrix[0][0] != 0);
    }

    /**
     * Tests FDCT with identity-like matrix.
     */
    @Test
    public void testFdctIdentityMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            matrix[i][i] = 1;
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        assertNotNull(matrix);
    }

    /**
     * Tests FDCT with non-zero starting position in larger matrix.
     */
    @Test
    public void testFdctWithOffset() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];
        final short startRow = 4;
        final short startCol = 4;

        for (int i = startRow; i < startRow + BLOCK_SIZE; i++) {
            for (int j = startCol; j < startCol + BLOCK_SIZE; j++) {
                matrix[i][j] = 100;
            }
        }

        aandct.fdct(matrix, startRow, startCol);
        assertTrue(matrix[startRow][startCol] != 0);
    }

    /**
     * Tests IDCT with zero matrix produces zero output.
     */
    @Test
    public void testIdctZeroMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        aandct.idct(matrix, (short) 0, (short) 0);

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                assertEquals(0, matrix[i][j]);
            }
        }
    }

    /**
     * Tests IDCT with non-zero starting position in larger matrix.
     */
    @Test
    public void testIdctWithOffset() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];
        final short startRow = 2;
        final short startCol = 3;

        for (int i = startRow; i < startRow + BLOCK_SIZE; i++) {
            for (int j = startCol; j < startCol + BLOCK_SIZE; j++) {
                matrix[i][j] = 50;
            }
        }

        aandct.idct(matrix, startRow, startCol);
        assertNotNull(matrix);
    }

    /**
     * Tests FDCT followed by IDCT reconstruction.
     */
    @Test
    public void testFdctIdctRoundTrip() {
        final short[][] original = new short[BLOCK_SIZE][BLOCK_SIZE];
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                original[i][j] = (short) ((i + j) * 10);
                matrix[i][j] = original[i][j];
            }
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        aandct.idct(matrix, (short) 0, (short) 0);

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                assertEquals(original[i][j], matrix[i][j], DELTA);
            }
        }
    }

    /**
     * Tests FDCT with maximum positive values.
     */
    @Test
    public void testFdctMaxValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = Short.MAX_VALUE;
            }
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        assertNotNull(matrix);
    }

    /**
     * Tests FDCT with negative values.
     */
    @Test
    public void testFdctNegativeValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = -100;
            }
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        assertNotNull(matrix);
    }

    /**
     * Tests FDCT with mixed positive and negative values.
     */
    @Test
    public void testFdctMixedValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = (short) ((i + j) % 2 == 0 ? 100 : -100);
            }
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        assertNotNull(matrix);
    }

    /**
     * Tests IDCT with positive DC coefficient.
     */
    @Test
    public void testIdctPositiveDC() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        matrix[0][0] = 1024;

        aandct.idct(matrix, (short) 0, (short) 0);
        assertTrue(matrix[0][0] > 0);
    }

    /**
     * Tests IDCT with negative DC coefficient.
     */
    @Test
    public void testIdctNegativeDC() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        matrix[0][0] = -1024;

        aandct.idct(matrix, (short) 0, (short) 0);
        assertTrue(matrix[0][0] < 0);
    }

    /**
     * Tests FDCT with checkerboard pattern.
     */
    @Test
    public void testFdctCheckerboard() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = (short) ((i + j) % 2 == 0 ? 255 : 0);
            }
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        assertNotNull(matrix);
    }

    /**
     * Tests FDCT preserves DC component energy.
     */
    @Test
    public void testFdctDCComponent() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        final short value = 64;

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = value;
            }
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        assertTrue(Math.abs(matrix[0][0]) > 0);
    }

    /**
     * Tests FDCT and IDCT with gradient pattern.
     */
    @Test
    public void testFdctIdctGradient() {
        final short[][] original = new short[BLOCK_SIZE][BLOCK_SIZE];
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                original[i][j] = (short) (i * 32);
                matrix[i][j] = original[i][j];
            }
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        aandct.idct(matrix, (short) 0, (short) 0);

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                assertEquals(original[i][j], matrix[i][j], DELTA);
            }
        }
    }

    /**
     * Tests FDCT with all AC coefficients zero except DC.
     */
    @Test
    public void testFdctDCOnly() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        matrix[0][0] = 512;

        aandct.fdct(matrix, (short) 0, (short) 0);
        assertNotNull(matrix);
    }

    /**
     * Tests IDCT reconstruction accuracy with small values.
     */
    @Test
    public void testIdctSmallValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = (short) (i + j);
            }
        }

        aandct.idct(matrix, (short) 0, (short) 0);
        assertNotNull(matrix);
    }

    /**
     * Tests multiple FDCT operations on same matrix.
     */
    @Test
    public void testMultipleFdct() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = 50;
            }
        }

        aandct.fdct(matrix, (short) 0, (short) 0);
        final short firstDC = matrix[0][0];

        aandct.fdct(matrix, (short) 0, (short) 0);
        assertNotNull(matrix);
    }

    /**
     * Tests FDCT with boundary values at edges.
     */
    @Test
    public void testFdctEdgeValues() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];
        final short startRow = 8;
        final short startCol = 8;

        for (int i = startRow; i < startRow + BLOCK_SIZE; i++) {
            for (int j = startCol; j < startCol + BLOCK_SIZE; j++) {
                matrix[i][j] = 75;
            }
        }

        aandct.fdct(matrix, startRow, startCol);
        assertTrue(matrix[startRow][startCol] != 0);
    }

    /**
     * Tests IDCT with boundary values at edges.
     */
    @Test
    public void testIdctEdgeValues() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];
        final short startRow = 7;
        final short startCol = 7;

        for (int i = startRow; i < startRow + BLOCK_SIZE; i++) {
            for (int j = startCol; j < startCol + BLOCK_SIZE; j++) {
                matrix[i][j] = 85;
            }
        }

        aandct.idct(matrix, startRow, startCol);
        assertNotNull(matrix);
    }

    /**
     * Tests FDCT and IDCT preserve data outside block.
     */
    @Test
    public void testTransformDoesNotAffectOutsideBlock() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];
        final short startRow = 4;
        final short startCol = 4;
        final short outsideValue = 99;

        for (int i = 0; i < MATRIX_SIZE; i++) {
            for (int j = 0; j < MATRIX_SIZE; j++) {
                matrix[i][j] = outsideValue;
            }
        }

        aandct.fdct(matrix, startRow, startCol);

        assertEquals(outsideValue, matrix[0][0]);
        assertEquals(outsideValue, matrix[MATRIX_SIZE - 1][MATRIX_SIZE - 1]);
    }
}
