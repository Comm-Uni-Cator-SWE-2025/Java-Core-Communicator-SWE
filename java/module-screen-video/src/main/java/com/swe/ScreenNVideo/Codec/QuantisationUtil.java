// Contributed by Anup Kumar

package com.swe.ScreenNVideo.Codec;

/**
 * Utility class for handling JPEG quantization tables and operations.
 *
 * <p>This class manages the default luminance and chrominance tables,
 * scales them based on a quality factor and AAN DCT factors,
 * and provides methods to quantize and de-quantize 8x8 blocks.
 *
 * <p>This class follows the Singleton pattern.
 */
@SuppressWarnings("checkstyle:MissingJavadocType")
public class QuantisationUtil {
    /**
     * Annex K, Table K.1 of the JPEG Standard (ITU-T T.81 / ISO 10918-1, 1992)
     * Default Chrominance Quantization Table.
     * NOTE: This table is MUTABLE and will be changed by scaling methods.
     */
    private static final int[][] BASECHROME = {
        {17, 18, 24, 47, 99, 99, 99, 99},
        {18, 21, 26, 66, 99, 99, 99, 99},
        {24, 26, 56, 99, 99, 99, 99, 99},
        {47, 66, 99, 99, 99, 99, 99, 99},
        {99, 99, 99, 99, 99, 99, 99, 99},
        {99, 99, 99, 99, 99, 99, 99, 99},
        {99, 99, 99, 99, 99, 99, 99, 99},
        {99, 99, 99, 99, 99, 99, 99, 99},
    };

    /**
     * Annex K, Table K.1 of the JPEG Standard (ITU-T T.81 / ISO 10918-1, 1992)
     * Default Luminance Quantization Table.
     * NOTE: This table is MUTABLE and will be changed by scaling methods.
     */
    private static final int[][] BASELUMIN = {
        {16, 11, 10, 16, 24, 40, 51, 61},
        {12, 12, 14, 19, 26, 58, 60, 55},
        {14, 13, 16, 24, 40, 57, 69, 56},
        {14, 17, 22, 29, 51, 87, 80, 62},
        {18, 22, 37, 56, 68, 109, 103, 77},
        {24, 35, 55, 64, 81, 104, 113, 92},
        {49, 64, 78, 87, 103, 121, 120, 101},
        {72, 92, 95, 98, 112, 100, 103, 99}
    };

    /**
     * Dimension for the 8x8 DCT matrix.
     */
    private static final int MATRIX_DIM = 8;
    /**
     * Base value for cosine calculation in AAN.
     */
    private static final int COS_BASE = 16;
    /**
     * Constant for the number 4, used in AAN scaling.
     */
    private static final double FOUR = 4.0;
    /**
     * Constant for the number 1, used in AAN scaling.
     */
    private static final double ONE = 1.0;
    /**
     * Constant for the number 2, used in AAN scaling.
     */
    private static final double TWO = 2.0;

    // --- Constants for JPEG Quality Scaling ---
    /**
     * Midpoint for quality factor logic switch.
     */
    private static final int JPEG_QUALITY_MID = 50;
    /**
     * Scale factor for high-quality settings.
     */
    private static final int JPEG_QUALITY_SCALE_HIGH = 200;
    /**
     * Multiplier for high-quality scale factor.
     */
    private static final int JPEG_QUALITY_SCALE_HIGH_FACTOR = 2;
    /**
     * Numerator for low-quality scale factor.
     */
    private static final int JPEG_QUALITY_SCALE_LOW_NUM = 5000;
    /**
     * Rounding offset for quality scaling.
     */
    private static final int JPEG_ROUNDING_OFFSET = 50;
    /**
     * Divisor for quality scaling.
     */
    private static final int JPEG_SCALE_DIVISOR = 100;
    /**
     * Minimum allowed value for a quantized table entry.
     */
    private static final int JPEG_CLAMP_MIN = 1;
    /**
     * Maximum allowed value for a quantized table entry.
     */
    private static final int JPEG_CLAMP_MAX = 255;
    /**
     * Rounding offset for double-to-int conversion.
     */
    private static final double ROUNDING_OFFSET_DOUBLE = 0.5;

    /**
     * used for scaling.
     */
    private final float[][] scaledQuantChrome = new float[MATRIX_DIM][MATRIX_DIM];
    /**
     * used for scaling.
     */
    private final float[][] scaledQuantLumin = new float[MATRIX_DIM][MATRIX_DIM];

    /**
     * ScaleFactor which is used in FDCT to scale DCT coefficient
     * but to prevent repeated calculation it will be used
     * to scale quantisation table.
     */
    private final double[] aanScaleFactor;

    /**
     * Singleton instance.
     */
    private static final QuantisationUtil QUANT_INSTANCE = new QuantisationUtil();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private QuantisationUtil() {
        aanScaleFactor = new double[MATRIX_DIM];

        final double[] cVals = new double[MATRIX_DIM];
        for (int i = 0; i < MATRIX_DIM; ++i) {
            cVals[i] = Math.cos(Math.PI * i / COS_BASE);
            aanScaleFactor[i] = ONE / (cVals[i] * FOUR);
        }

        aanScaleFactor[0] = ONE / (TWO * Math.sqrt(TWO));
        // Initialize the scaled tables when created
        setCompressonResulation(JPEG_QUALITY_MID);
//        resetQuantTables();
    }

    /**
     * This will be set the quant tables to default.
     */
    private void resetQuantTables() {
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                scaledQuantChrome[i][j] = BASECHROME[i][j];
                scaledQuantLumin[i][j] = BASELUMIN[i][j];
            }
        }
        scaleQuantTable(aanScaleFactor);
    }

    /**
     * Gets the singleton instance of the QuantisationUtil.
     *
     * @return The single instance of this class.
     */
    public static QuantisationUtil getInstance() {
        return QUANT_INSTANCE;
    }

    /**
     * Scales the quantization tables based on a JPEG quality factor 'q'.
     * This method MODIFIES the class's quantChrome and quantLumin tables in-place.
     *
     * @param q : A quality factor from 1 to 99.
     */
    public void setCompressonResulation(final int q) {

        resetQuantTables();

        int scaleFactor = JPEG_QUALITY_SCALE_HIGH - JPEG_QUALITY_SCALE_HIGH_FACTOR * q;

        // Standard JPEG quality scale factor calculation.
        if (q >= JPEG_CLAMP_MIN && q < JPEG_QUALITY_MID) {
            scaleFactor = JPEG_QUALITY_SCALE_LOW_NUM / q;
        }

        for (int i = 0; i < MATRIX_DIM; ++i) {
            for (int j = 0; j < MATRIX_DIM; ++j) {
                // Apply scale factor with rounding (+50 / 100)
                int scaledCVal = (BASECHROME[i][j] * scaleFactor + JPEG_ROUNDING_OFFSET) / JPEG_SCALE_DIVISOR;
                int scaledLVal = (BASELUMIN[i][j] * scaleFactor + JPEG_ROUNDING_OFFSET) / JPEG_SCALE_DIVISOR;

                // Clamp values to the valid JPEG range [1, 255]
                if (scaledCVal < JPEG_CLAMP_MIN) {
                    scaledCVal = JPEG_CLAMP_MIN;
                } else if (scaledCVal > JPEG_CLAMP_MAX) {
                    scaledCVal = JPEG_CLAMP_MAX;
                }

                if (scaledLVal < JPEG_CLAMP_MIN) {
                    scaledLVal = JPEG_CLAMP_MIN;
                } else if (scaledLVal > JPEG_CLAMP_MAX) {
                    scaledLVal = JPEG_CLAMP_MAX;
                }

                BASECHROME[i][j] = scaledCVal;
                BASELUMIN[i][j] = scaledLVal;
            }
        }
    }

    /**
     * Scales the quantization tables by a given set of 1D scaling factors.
     * This is used to "absorb" the scaling factors from a fast DCT (like AAN)
     * into the quantization table, as you mentioned.
     *
     * <p>This method MODIFIES the class's quantChrome and quantLumin tables in-place.
     *
     * @param scalingFactors An 8-element array of 1D scaling factors from the DCT.
     */
    public void scaleQuantTable(final double[] scalingFactors) {
        for (int i = 0; i < MATRIX_DIM; ++i) {
            for (int j = 0; j < MATRIX_DIM; ++j) {
                final double scale = scalingFactors[i] * scalingFactors[j];
                scaledQuantChrome[i][j] = (float) (BASECHROME[i][j] / scale);
                scaledQuantLumin[i][j] = (float) (BASELUMIN[i][j] / scale);
            }
        }
    }

    /**
     * Applies quantization to a Chrominance block of DCT coefficients.
     *
     * @param dMatrix The 8x8 block of DCT coefficients.
     * @param row     The starting row of the block.
     * @param col     The starting column of the block.
     */
    public void quantisationChrome(final short[][] dMatrix, final short row, final short col) {
        for (int i = 0; i < MATRIX_DIM; ++i) {
            for (int j = 0; j < MATRIX_DIM; ++j) {
                // Quantize: divide by the table value and round.
                dMatrix[i + row][j + col] = (short) Math.round(dMatrix[i + row][j + col] / scaledQuantChrome[i][j]);
            }
        }
    }

    /**
     * Applies quantization to a Luminance block of DCT coefficients.
     *
     * @param dMatrix The 8x8 block of DCT coefficients.
     * @param row     The starting row of the block.
     * @param col     The starting column of the block.
     */
    public void quantisationLumin(final short[][] dMatrix, final short row, final short col) {
        for (int i = 0; i < MATRIX_DIM; ++i) {
            for (int j = 0; j < MATRIX_DIM; ++j) {
                // Quantize: divide by the table value and round.
                dMatrix[i + row][j + col] = (short) Math.round(dMatrix[i + row][j + col] / scaledQuantLumin[i][j]);
            }
        }
    }

    /**
     * Applies de-quantization to a Chrominance block of DCT coefficients.
     *
     * @param dMatrix The 8x8 block of quantized coefficients.
     * @param row     The starting row of the block.
     * @param col     The starting column of the block.
     */
    public void deQuantisationChrome(final short[][] dMatrix, final short row, final short col) {
        for (int i = 0; i < MATRIX_DIM; ++i) {
            for (int j = 0; j < MATRIX_DIM; ++j) {
                // De-quantize: multiply by the table value and round.
                dMatrix[i + row][j + col] = (short) Math.round(dMatrix[i + row][j + col] * scaledQuantChrome[i][j]);
            }
        }
    }

    /**
     * Applies de-quantization to a Luminance block of DCT coefficients.
     *
     * @param dMatrix The 8x8 block of quantized coefficients.
     * @param row     The starting row of the block.
     * @param col     The starting column of the block.
     */
    public void deQuantisationLumin(final short[][] dMatrix, final short row, final short col) {
        for (int i = 0; i < MATRIX_DIM; ++i) {
            for (int j = 0; j < MATRIX_DIM; ++j) {
                // De-quantize: multiply by the table value and round.
                dMatrix[i + row][j + col] = (short) Math.round(dMatrix[i + row][j + col] * scaledQuantLumin[i][j]);
            }
        }
    }
}