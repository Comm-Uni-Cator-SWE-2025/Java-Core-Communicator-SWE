package com.swe.ScreenNVideo.Codec;

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
     * used for scaling.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private int[][] scaledQuantChrome = new int[8][8];
    /**
     * used for scaling.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private int[][] scaledQuantLumin = new int[8][8];
    /**
     * Singleton instance.
     */
    @SuppressWarnings("checkstyle:ConstantName")
    private static final QuantisationUtil QUANTINSTANCE = new QuantisationUtil();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private QuantisationUtil() {
        // Initialize the scaled tables when created
        resetQuantTables();
    }

    /**
     * This will be set the quant tables to default.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private void resetQuantTables() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                scaledQuantChrome[i][j] = BASECHROME[i][j];
                scaledQuantLumin[i][j] = BASELUMIN[i][j];
            }
        }
    }

    /**
     * Gets the singleton instance of the QuantisationUtil.
     * @return The single instance of this class.
     */
    public static QuantisationUtil getInstance() {
        return QUANTINSTANCE;
    }

    /**
     * Scales the quantization tables based on a JPEG quality factor 'q'.
     * This method MODIFIES the class's quantChrome and quantLumin tables in-place.
     *
     * @param q : A quality factor from 1 to 99.
     */
    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:WhitespaceAround", "checkstyle:FinalParameters"})
    public void setCompressonResulation(int q) {

        resetQuantTables();

        int scaleFactor = 200 - 2 * q;

        // Standard JPEG quality scale factor calculation.
        if (q >= 1 && q < 50) {
            scaleFactor = 5000 / q;
        }

        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                // Apply scale factor with rounding (+50 / 100)
                int scaledCVal = (scaledQuantChrome[i][j] * scaleFactor + 50) / 100;
                int scaledLVal = (scaledQuantLumin[i][j] * scaleFactor + 50) / 100;

                // Clamp values to the valid JPEG range [1, 255]
                if (scaledCVal<1) {
                    scaledCVal = 1;
                } else if (scaledCVal>255) {
                    scaledCVal = 255;
                }

                if (scaledLVal < 1) {
                    scaledLVal = 1;
                } else if (scaledLVal > 255) {
                    scaledLVal = 255;
                }

                scaledQuantChrome[i][j] = scaledCVal;
                scaledQuantLumin[i][j] = scaledLVal;
            }
        }
    }

    /**
     * Scales the quantization tables by a given set of 1D scaling factors.
     * This is used to "absorb" the scaling factors from a fast DCT (like AAN)
     * into the quantization table, as you mentioned.
     *
     * This method MODIFIES the class's quantChrome and quantLumin tables in-place.
     *
     * @param scalingFactors An 8-element array of 1D scaling factors from the DCT.
     */
    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:WhitespaceAround", "checkstyle:WhitespaceAfter", "checkstyle:NeedBraces"})
    public void scaleQuantTable(final double[] scalingFactors) {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j<8; ++j) {

                // Apply 2D scaling by dividing by the outer product (scale[i] * scale[j])
                // Add 0.5 before casting to int to perform rounding.
                int scaledCVal = (int) ((scaledQuantChrome[i][j] / (scalingFactors[i] * scalingFactors[j])) + 0.5);
                int scaledLVal = (int) ((scaledQuantLumin[i][j] / (scalingFactors[i] * scalingFactors[j])) + 0.5);

//                // Clamp values to the valid JPEG range [1, 255]
//                if(scaledCVal<1) scaledCVal = 1;
//                else if(scaledCVal>255) scaledCVal = 255;
//
//                if(scaledLVal<1) scaledLVal = 1;
//                else if(scaledLVal>255) scaledLVal = 255;

                scaledQuantChrome[i][j] = scaledCVal;
                scaledQuantLumin[i][j] = scaledLVal;
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
    @SuppressWarnings({"checkstyle:LineLength", "checkstyle:MagicNumber", "checkstyle:FinalParameters", "checkstyle:MethodParamPad"})
    public void quantisationChrome(final short[][] dMatrix, final short row, final short col) {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                // Quantize: divide by the table value and round.
                dMatrix[i + row][j + col] = (short) Math.round((float) dMatrix[i + row][j + col] / scaledQuantChrome[i][j]);
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
    @SuppressWarnings({"checkstyle:LineLength", "checkstyle:MagicNumber", "checkstyle:FinalParameters", "checkstyle:MethodParamPad"})
    public void quantisationLumin(final short[][] dMatrix, final short row, final short col) {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                // Quantize: divide by the table value and round.
                dMatrix[i + row][j + col] = (short) Math.round((float) dMatrix[i + row][j + col] / scaledQuantLumin[i][j]);
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
    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:FinalParameters", "checkstyle:WhitespaceAfter", "checkstyle:MethodParamPad"})
    public void deQuantisationChrome(final short[][] dMatrix, final short row, final short col) {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                // De-quantize: multiply by the table value.
                dMatrix[i + row][j + col] = (short) (dMatrix[i + row][j + col] * scaledQuantChrome[i][j]);
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
    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:FinalParameters", "checkstyle:WhitespaceAfter", "checkstyle:MethodParamPad"})
    public void deQuantisationLumin(final short[][] dMatrix, final short row, final short col) {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                // De-quantize: multiply by the table value.
                dMatrix[i + row][j + col] = (short) (dMatrix[i + row][j + col]  * scaledQuantLumin[i][j]);
            }
        }
    }
}
