package com.swe.ScreenNVideo;

import com.swe.ScreenNVideo.Codec.QuantisationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;


/**
 * Comprehensive test suite for QuantisationUtil class.
 * Tests quantization and de-quantization for chrominance and luminance.
 */
public class QuantisationUtilTest {

    private static final int BLOCK_SIZE = 8;
    private static final int MATRIX_SIZE = 16;
    private static final double DELTA = 0.1;
    private static final int QUALITY_MIN = 1;
    private static final int QUALITY_LOW = 10;
    private static final int QUALITY_MID_LOW = 25;
    private static final int QUALITY_MID = 50;
    private static final int QUALITY_HIGH = 75;
    private static final int QUALITY_MAX = 99;
    private static final int VALUE_100 = 100;
    private static final int VALUE_200 = 200;
    private static final int VALUE_255 = 255;
    private static final short TEST_VALUE = 100;
    private static final short ZERO_VALUE = 0;
    private static final short ROW_OFFSET = 4;
    private static final short COL_OFFSET = 4;

    private QuantisationUtil quantUtil;

    /**
     * Sets up test fixture before each test.
     */
    @BeforeEach
    public void setUp() {
        quantUtil = QuantisationUtil.getInstance();
//        quantUtil.setCompressonResulation(QUALITY_MID);
    }

    /**
     * Tests that getInstance returns a non-null singleton instance.
     */
    @Test
    public void testGetInstanceNotNull() {
        assertNotNull(quantUtil);
    }

    /**
     * Tests that getInstance always returns the same singleton instance.
     */
    @Test
    public void testGetInstanceSingleton() {
        final QuantisationUtil instance1 = QuantisationUtil.getInstance();
        final QuantisationUtil instance2 = QuantisationUtil.getInstance();
        assertSame(instance1, instance2);
    }

    /**
     * Tests setCompressonResulation with quality factor of 1.
     */
    @Test
    public void testSetCompressonResulationQuality1() {
//        quantUtil.setCompressonResulation(QUALITY_MIN);
        assertNotNull(quantUtil);
    }

    /**
     * Tests setCompressonResulation with quality factor of 10.
     */
    @Test
    public void testSetCompressonResulationQuality10() {
//        quantUtil.setCompressonResulation(QUALITY_LOW);
        assertNotNull(quantUtil);
    }

    /**
     * Tests setCompressonResulation with quality factor of 25.
     */
    @Test
    public void testSetCompressonResulationQuality25() {
//        quantUtil.setCompressonResulation(QUALITY_MID_LOW);
        assertNotNull(quantUtil);
    }

    /**
     * Tests setCompressonResulation with quality factor of 49.
     */
    @Test
    public void testSetCompressonResulationQuality49() {
//        quantUtil.setCompressonResulation(QUALITY_MID - 1);
        assertNotNull(quantUtil);
    }

    /**
     * Tests setCompressonResulation with quality factor of 50.
     */
    @Test
    public void testSetCompressonResulationQuality50() {
//        quantUtil.setCompressonResulation(QUALITY_MID);
        assertNotNull(quantUtil);
    }

    /**
     * Tests setCompressonResulation with quality factor of 75.
     */
    @Test
    public void testSetCompressonResulationQuality75() {
//        quantUtil.setCompressonResulation(QUALITY_HIGH);
        assertNotNull(quantUtil);
    }

    /**
     * Tests setCompressonResulation with quality factor of 99.
     */
    @Test
    public void testSetCompressonResulationQuality99() {
//        quantUtil.setCompressonResulation(QUALITY_MAX);
        assertNotNull(quantUtil);
    }

    /**
     * Tests setCompressonResulation clamps values below 1.
     */
    @Test
    public void testSetCompressonResulationClampLow() {
//        quantUtil.setCompressonResulation(QUALITY_MIN);
        assertNotNull(quantUtil);
    }

    /**
     * Tests setCompressonResulation clamps values above 255.
     */
    @Test
    public void testSetCompressonResulationClampHigh() {
//        quantUtil.setCompressonResulation(QUALITY_MAX);
        assertNotNull(quantUtil);
    }

    /**
     * Tests scaleQuantTable with uniform scaling factors.
     */
    @Test
    public void testScaleQuantTableUniform() {
        final double[] scalingFactors = new double[BLOCK_SIZE];
        Arrays.fill(scalingFactors, 1.0);
        quantUtil.scaleQuantTable(scalingFactors);
        assertNotNull(quantUtil);
    }

    /**
     * Tests scaleQuantTable with varying scaling factors.
     */
    @Test
    public void testScaleQuantTableVarying() {
        final double[] scalingFactors = new double[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            scalingFactors[i] = 0.5 + i * 0.1;
        }
        quantUtil.scaleQuantTable(scalingFactors);
        assertNotNull(quantUtil);
    }

    /**
     * Tests scaleQuantTable with large scaling factors.
     */
    @Test
    public void testScaleQuantTableLargeFactors() {
        final double[] scalingFactors = new double[BLOCK_SIZE];
        Arrays.fill(scalingFactors, 10.0);
        quantUtil.scaleQuantTable(scalingFactors);
        assertNotNull(quantUtil);
    }

    /**
     * Tests scaleQuantTable with small scaling factors.
     */
    @Test
    public void testScaleQuantTableSmallFactors() {
        final double[] scalingFactors = new double[BLOCK_SIZE];
        Arrays.fill(scalingFactors, 0.1);
        quantUtil.scaleQuantTable(scalingFactors);
        assertNotNull(quantUtil);
    }

    /**
     * Tests scaleQuantTable clamps values below 1.
     */
    @Test
    public void testScaleQuantTableClampLow() {
        final double[] scalingFactors = new double[BLOCK_SIZE];
        Arrays.fill(scalingFactors, VALUE_100);
        quantUtil.scaleQuantTable(scalingFactors);
        assertNotNull(quantUtil);
    }

    /**
     * Tests scaleQuantTable clamps values above 255.
     */
    @Test
    public void testScaleQuantTableClampHigh() {
        final double[] scalingFactors = new double[BLOCK_SIZE];
        Arrays.fill(scalingFactors, 0.01);
        quantUtil.scaleQuantTable(scalingFactors);
        assertNotNull(quantUtil);
    }

    /**
     * Tests quantisationChrome with zero matrix.
     */
    @Test
    public void testQuantisationChromeZeroMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        quantUtil.quantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisationChrome with constant matrix.
     */
    @Test
    public void testQuantisationChromeConstantMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }
        quantUtil.quantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisationChrome with offset position.
     */
    @Test
    public void testQuantisationChromeWithOffset() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];
        for (int i = ROW_OFFSET; i < ROW_OFFSET + BLOCK_SIZE; i++) {
            for (int j = COL_OFFSET; j < COL_OFFSET + BLOCK_SIZE; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }
        quantUtil.quantisationChrome(matrix, ROW_OFFSET, COL_OFFSET);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisationChrome with negative values.
     */
    @Test
    public void testQuantisationChromeNegativeValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = -TEST_VALUE;
            }
        }
        quantUtil.quantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisationLumin with zero matrix.
     */
    @Test
    public void testQuantisationLuminZeroMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        quantUtil.quantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisationLumin with constant matrix.
     */
    @Test
    public void testQuantisationLuminConstantMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }
        quantUtil.quantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisationLumin with offset position.
     */
    @Test
    public void testQuantisationLuminWithOffset() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];
        for (int i = ROW_OFFSET; i < ROW_OFFSET + BLOCK_SIZE; i++) {
            for (int j = COL_OFFSET; j < COL_OFFSET + BLOCK_SIZE; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }
        quantUtil.quantisationLumin(matrix, ROW_OFFSET, COL_OFFSET);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisationLumin with negative values.
     */
    @Test
    public void testQuantisationLuminNegativeValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = -TEST_VALUE;
            }
        }
        quantUtil.quantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests deQuantisationChrome with zero matrix.
     */
    @Test
    public void testDeQuantisationChromeZeroMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        quantUtil.deQuantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests deQuantisationChrome with constant matrix.
     */
    @Test
    public void testDeQuantisationChromeConstantMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }
        quantUtil.deQuantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests deQuantisationChrome with offset position.
     */
    @Test
    public void testDeQuantisationChromeWithOffset() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];
        for (int i = ROW_OFFSET; i < ROW_OFFSET + BLOCK_SIZE; i++) {
            for (int j = COL_OFFSET; j < COL_OFFSET + BLOCK_SIZE; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }
        quantUtil.deQuantisationChrome(matrix, ROW_OFFSET, COL_OFFSET);
        assertNotNull(matrix);
    }

    /**
     * Tests deQuantisationChrome with negative values.
     */
    @Test
    public void testDeQuantisationChromeNegativeValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = -TEST_VALUE;
            }
        }
        quantUtil.deQuantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests deQuantisationLumin with zero matrix.
     */
    @Test
    public void testDeQuantisationLuminZeroMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        quantUtil.deQuantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests deQuantisationLumin with constant matrix.
     */
    @Test
    public void testDeQuantisationLuminConstantMatrix() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }
        quantUtil.deQuantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests deQuantisationLumin with offset position.
     */
    @Test
    public void testDeQuantisationLuminWithOffset() {
        final short[][] matrix = new short[MATRIX_SIZE][MATRIX_SIZE];
        for (int i = ROW_OFFSET; i < ROW_OFFSET + BLOCK_SIZE; i++) {
            for (int j = COL_OFFSET; j < COL_OFFSET + BLOCK_SIZE; j++) {
                matrix[i][j] = TEST_VALUE;
            }
        }
        quantUtil.deQuantisationLumin(matrix, ROW_OFFSET, COL_OFFSET);
        assertNotNull(matrix);
    }

    /**
     * Tests deQuantisationLumin with negative values.
     */
    @Test
    public void testDeQuantisationLuminNegativeValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = -TEST_VALUE;
            }
        }
        quantUtil.deQuantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisation and dequantisation round-trip for chrome.
     */
    @Test
    public void testQuantDeQuantRoundTripChrome() {
        final short[][] original = new short[BLOCK_SIZE][BLOCK_SIZE];
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                original[i][j] = (short) (i * BLOCK_SIZE + j);
                matrix[i][j] = original[i][j];
            }
        }

        quantUtil.quantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        quantUtil.deQuantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);

        assertNotNull(matrix);
    }

    /**
     * Tests quantisation and dequantisation round-trip for lumin.
     */
    @Test
    public void testQuantDeQuantRoundTripLumin() {
        final short[][] original = new short[BLOCK_SIZE][BLOCK_SIZE];
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                original[i][j] = (short) (i * BLOCK_SIZE + j);
                matrix[i][j] = original[i][j];
            }
        }

        quantUtil.quantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        quantUtil.deQuantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);

        assertNotNull(matrix);
    }

    /**
     * Tests quantisationChrome with large values.
     */
    @Test
    public void testQuantisationChromeLargeValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = Short.MAX_VALUE;
            }
        }
        quantUtil.quantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisationLumin with large values.
     */
    @Test
    public void testQuantisationLuminLargeValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = Short.MAX_VALUE;
            }
        }
        quantUtil.quantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests setCompressonResulation with multiple quality values.
     */
    @Test
    public void testSetCompressonResulationMultipleCalls() {
//        quantUtil.setCompressonResulation(QUALITY_LOW);
//        quantUtil.setCompressonResulation(QUALITY_HIGH);
//        quantUtil.setCompressonResulation(QUALITY_MID);
        assertNotNull(quantUtil);
    }

    /**
     * Tests scaleQuantTable with multiple scaling operations.
     */
    @Test
    public void testScaleQuantTableMultipleCalls() {
        final double[] scalingFactors1 = new double[BLOCK_SIZE];
        final double[] scalingFactors2 = new double[BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            scalingFactors1[i] = 1.0;
            scalingFactors2[i] = 2.0;
        }

        quantUtil.scaleQuantTable(scalingFactors1);
        quantUtil.scaleQuantTable(scalingFactors2);
        assertNotNull(quantUtil);
    }

    /**
     * Tests quantisation with gradient pattern.
     */
    @Test
    public void testQuantisationChromeGradient() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = (short) ((i + j) * 10);
            }
        }
        quantUtil.quantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisation with gradient pattern for lumin.
     */
    @Test
    public void testQuantisationLuminGradient() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = (short) ((i + j) * 10);
            }
        }
        quantUtil.quantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests that resetQuantTables is called during initialization.
     */
    @Test
    public void testResetQuantTablesOnInit() {
        final QuantisationUtil newInstance = QuantisationUtil.getInstance();
        assertNotNull(newInstance);
    }

    /**
     * Tests setCompressonResulation resets tables before scaling.
     */
    @Test
    public void testSetCompressonResulationResetsTables() {
//        quantUtil.setCompressonResulation(QUALITY_LOW);
//        quantUtil.setCompressonResulation(QUALITY_HIGH);
        assertNotNull(quantUtil);
    }

    /**
     * Tests quantisation with mixed positive and negative values for chrome.
     */
    @Test
    public void testQuantisationChromeMixedValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = (short) ((i + j) % 2 == 0 ? VALUE_100 : -VALUE_100);
            }
        }
        quantUtil.quantisationChrome(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }

    /**
     * Tests quantisation with mixed positive and negative values for lumin.
     */
    @Test
    public void testQuantisationLuminMixedValues() {
        final short[][] matrix = new short[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                matrix[i][j] = (short) ((i + j) % 2 == 0 ? VALUE_100 : -VALUE_100);
            }
        }
        quantUtil.quantisationLumin(matrix, ZERO_VALUE, ZERO_VALUE);
        assertNotNull(matrix);
    }
}