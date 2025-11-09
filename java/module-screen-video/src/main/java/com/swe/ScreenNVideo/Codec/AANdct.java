package com.swe.ScreenNVideo.Codec;

// Contributed by Anup Kumar.

/**
 * This FDCT and IDCT transformation is implemented
 * by using AAN (Arai-Agui-Nakajima) Fast DCT algorithm.
 */
public class AANdct implements IFIDCT {
    /**
     * count of ScaleFactor.
     */
    private final int SCALE_FACTOR_COUNT = 8;
    /**
     * multipliers are intermediate coefficient which will be used in FDCT
     * (Forward Discrete Cosine Transform).
     */
    private final double[] multipliers;
    /**
     * multipliers are intermediate coefficient which will be used in IDCT
     * (Inverse Discrete Cosine Transform).
     */
    private final double[] imultipliers;
    /**
     * this is AANdct Instance allocated statically
     * Here Singleton Pattern is used.
     */
    private static final AANdct AANDCTINSTANCE = new AANdct();

    /**
     * index three.
     */
    private final int THREE = 3;

    /**
     * index four.
     */

    private final int FOUR = 4;

    /**
     * index five.
     */
    private final int FIVE = 5;

    /**
     * index six.
     */
    private final int SIX = 6;

    /**
     * index seven.
     */
    private final int SEVEN = 7;


    private AANdct() {

        final int multipliersCount = 5;
        multipliers = new double[multipliersCount];
        imultipliers = new double[multipliersCount];

        final double[] cVals = new double[SCALE_FACTOR_COUNT];
        final int cosBase = 16;
        for (int i = 0; i < SCALE_FACTOR_COUNT; ++i) {
            cVals[i] = Math.cos(Math.PI * i / cosBase);
        }

        final int two = 2;

        multipliers[0] = cVals[FOUR];
        multipliers[1] = cVals[two] - cVals[SIX];
        multipliers[2] = cVals[FOUR];
        multipliers[THREE] = cVals[two] + cVals[SIX];
        multipliers[FOUR] = cVals[SIX];

        imultipliers[1] = 1.0 / Math.cos(Math.PI / FOUR);
        final int eight = 8;
        final double cos = Math.cos(Math.PI * THREE / eight);
        imultipliers[2] = 1.0 / cos;
        imultipliers[THREE] = 1.0 / Math.cos(Math.PI / eight);
        imultipliers[FOUR] = 1.0 / (Math.cos(Math.PI / eight) + cos);

    }

    /**
     * Returns the singleton instance of the AANdct class.
     *
     * @return the AANdct instance.
     */
    public static AANdct getInstance() {
        return AANDCTINSTANCE;
    }

    /**
     * Applies the 1D FDCT to each row of the 8x8 data block.
     *
     * @param data store the transformed coefficient.
     */
    private void fdctRow(final double[][] data) {

        for (int i = 0; i < SCALE_FACTOR_COUNT; i++) {

            // --- Phase 1 ---
            // Calculate all 8 intermediate sums/differences from the input row
            final double tmp0 = data[i][0] + data[i][SEVEN];
            final double tmp7 = data[i][0] - data[i][SEVEN];
            final double tmp1 = data[i][1] + data[i][SIX];
            final double tmp6 = data[i][1] - data[i][SIX];
            final double tmp2 = data[i][2] + data[i][FIVE];
            final double tmp5 = data[i][2] - data[i][FIVE];
            final double tmp3 = data[i][THREE] + data[i][FOUR];
            final double tmp4 = data[i][THREE] - data[i][FOUR];

            // --- Phase 2 ---
            // Combine the sums/differences from phase 1
            double tmp10 = tmp0 + tmp3;
            double tmp11 = tmp1 + tmp2;
            double tmp12 = tmp1 - tmp2;
            final double tmp13 = tmp0 - tmp3;

            // --- Phase 3 (Even part) ---
            // Calculate and store Y(0), Y(4)
            data[i][0] = tmp10 + tmp11;
            data[i][FOUR] = tmp10 - tmp11;

            // Calculate and store Y(2), Y(6)
            final double z1 = (tmp12 + tmp13) * multipliers[0];
            data[i][2] = z1 + tmp13;
            data[i][SIX] = tmp13 - z1;

            // --- Phase 3 (Odd part) ---
            // Reuse tmp10, tmp11, tmp12 for odd calculations
            tmp10 = -tmp4 - tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;

            final double z5 = (tmp10 + tmp12) * multipliers[FOUR];
            final double z2 = -multipliers[1] * tmp10 - z5;
            final double z3 = multipliers[2] * tmp11;
            final double z4 = multipliers[THREE] * tmp12 - z5;

            final double z11 = tmp7 + z3;
            final double z13 = tmp7 - z3;

            // Calculate and store Y(1), Y(3), Y(5), Y(7)
            data[i][FIVE] = z13 + z2;
            data[i][1] = z11 + z4;
            data[i][SEVEN] = z11 - z4;
            data[i][THREE] = z13 - z2;
        }
    }

    /**
     * Applies the 1D FDCT to each column of the 8x8 data block.
     *
     * @param data The 8x8 block of data to be transformed.
     */
    private void fdctCol(final double[][] data) {
        for (int j = 0; j < SCALE_FACTOR_COUNT; j++) {

            // --- Phase 1 ---
            // Calculate all 8 intermediate sums/differences from the input column
            final double tmp0 = data[0][j] + data[SEVEN][j];
            final double tmp7 = data[0][j] - data[SEVEN][j];
            final double tmp1 = data[1][j] + data[SIX][j];
            final double tmp6 = data[1][j] - data[SIX][j];
            final double tmp2 = data[2][j] + data[FIVE][j];
            final double tmp5 = data[2][j] - data[FIVE][j];
            final double tmp3 = data[THREE][j] + data[FOUR][j];
            final double tmp4 = data[THREE][j] - data[FOUR][j];

            // --- Phase 2 ---
            // Combine the sums/differences from phase 1
            double tmp10 = tmp0 + tmp3;
            double tmp11 = tmp1 + tmp2;
            double tmp12 = tmp1 - tmp2;
            final double tmp13 = tmp0 - tmp3;

            // --- Phase 3 (Even part) ---
            // Calculate and store Y(0), Y(4)
            data[0][j] = tmp10 + tmp11;
            data[FOUR][j] = tmp10 - tmp11;

            // Calculate and store Y(2), Y(6)
            final double z1 = (tmp12 + tmp13) * multipliers[0];
            data[2][j] = z1 + tmp13;
            data[SIX][j] = tmp13 - z1;

            // --- Phase 3 (Odd part) ---
            // Reuse tmp10, tmp11, tmp12 for odd calculations
            tmp10 = -tmp4 - tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;

            final double z5 = (tmp10 + tmp12) * multipliers[FOUR];
            final double z2 = -multipliers[1] * tmp10 - z5;
            final double z3 = multipliers[2] * tmp11;
            final double z4 = multipliers[THREE] * tmp12 - z5;

            final double z11 = tmp7 + z3;
            final double z13 = tmp7 - z3;

            // Calculate and store Y(1), Y(3), Y(5), Y(7)
            data[FIVE][j] = z13 + z2;
            data[1][j] = z11 + z4;
            data[SEVEN][j] = z11 - z4;
            data[THREE][j] = z13 - z2;
        }
    }

    /**
     * Main public function to perform the 2D Forward DCT.
     *
     * @param matrix The input/output matrix.
     * @param row    The starting row of the 8x8 block.
     * @param col    The starting col of the 8x8 block.
     */
    @Override
    public void fdct(final short[][] matrix, final short row, final short col) {

        //final transformed 8X8 matrix.
        final double[][] data = new double[SCALE_FACTOR_COUNT][SCALE_FACTOR_COUNT];

        // Copy 8x8 block into double array
        for (int i = 0; i < SCALE_FACTOR_COUNT; i++) {
            for (int j = 0; j < SCALE_FACTOR_COUNT; j++) {
                data[i][j] = matrix[row + i][col + j];
            }
        }

        // row wise transformation
        fdctRow(data);

        // column wise transformation
        fdctCol(data);


        // ---- store back the transformed block ----
        for (int i = 0; i < SCALE_FACTOR_COUNT; i++) {
            for (int j = 0; j < SCALE_FACTOR_COUNT; j++) {
                matrix[row + i][col + j] = (short) Math.round(data[i][j]);
            }
        }
    }

    /**
     * Main public function to perform the 2D Inverse DCT.
     *
     * @param matrix The input/output matrix.
     * @param row    The starting row of the 8x8 block.
     * @param col    The starting col of the 8x8 block.
     */
    @Override
    public void idct(final short[][] matrix, final short row, final short col) {

        // 1. Copy 8x8 block into double array
        final double[][] data = new double[SCALE_FACTOR_COUNT][SCALE_FACTOR_COUNT];
        for (int i = 0; i < SCALE_FACTOR_COUNT; ++i) {
            for (int j = 0; j < SCALE_FACTOR_COUNT; ++j) {
                data[i][j] = matrix[row + i][col + j];
            }
        }

        // 2. 1D IDCT on columns
        idctCol(data);

        // 3. 1D IDCT on rows
        idctRow(data);

        // 4. Store back the reconstructed block
        for (int i = 0; i < SCALE_FACTOR_COUNT; i++) {
            for (int j = 0; j < SCALE_FACTOR_COUNT; j++) {
                matrix[row + i][col + j] = (short) Math.round(data[i][j] / 64.0);
            }
        }
    }

    /**
     * Performs a 1D IDCT on all columns of the 8x8 data block.
     *
     * @param data The 8x8 data block.
     */
    private void idctCol(final double[][] data) {
        for (int j = 0; j < SCALE_FACTOR_COUNT; j++) {
            // --- Even part ---
            final double tmp10 = data[0][j] + data[FOUR][j];
            double tmp11 = data[0][j] - data[FOUR][j];
            final double tmp12 = (data[2][j] - data[SIX][j]) * imultipliers[1];
            final double tmp13 = data[2][j] + data[SIX][j];

            final double tmp0 = tmp10 + tmp13;
            final double tmp1 = tmp11 + tmp12;
            final double tmp2 = tmp11 - tmp12;
            final double tmp3 = tmp10 - tmp13;

            // --- Odd part ---
            double z2 = data[FIVE][j] - data[THREE][j];
            final double z11 = data[1][j] + data[SEVEN][j];
            final double z4 = data[1][j] - data[SEVEN][j];
            final double z13 = data[FIVE][j] + data[THREE][j];

            tmp11 = z11 - z13; // Reusing tmp11
            final double tmp7 = z11 + z13;
            final double z5 = (z2 + z4) * imultipliers[FOUR];

            final double z1 = z2 * imultipliers[2] + z5;
            z2 = tmp11 * imultipliers[1]; // Reusing z2
            final double z3 = z4 * imultipliers[THREE] + z5;

            final double tmp6 = z3 - tmp7;
            final double tmp5 = z2 - tmp6;
            final double tmp4 = -z1 - tmp5;

            // --- Final values ---
            data[0][j] = tmp0 + tmp7;
            data[SEVEN][j] = tmp0 - tmp7;
            data[1][j] = tmp1 + tmp6;
            data[SIX][j] = tmp1 - tmp6;
            data[2][j] = tmp2 + tmp5;
            data[FIVE][j] = tmp2 - tmp5;
            data[THREE][j] = tmp3 + tmp4;
            data[FOUR][j] = tmp3 - tmp4;
        }
    }

    /**
     * Performs a 1D IDCT on all rows of the 8x8 data block.
     *
     * @param data The 8x8 data block.
     */
    private void idctRow(final double[][] data) {
        for (int i = 0; i < SCALE_FACTOR_COUNT; i++) {
            // --- Even part ---
            final double tmp10 = data[i][0] + data[i][FOUR];
            double tmp11 = data[i][0] - data[i][FOUR];
            final double tmp12 = (data[i][2] - data[i][SIX]) * imultipliers[1];
            final double tmp13 = data[i][2] + data[i][SIX];

            final double tmp0 = tmp10 + tmp13;
            final double tmp1 = tmp11 + tmp12;
            final double tmp2 = tmp11 - tmp12;
            final double tmp3 = tmp10 - tmp13;

            // --- Odd part ---
            double z2 = data[i][FIVE] - data[i][THREE];
            final double z11 = data[i][1] + data[i][SEVEN];
            final double z4 = data[i][1] - data[i][SEVEN];
            final double z13 = data[i][FIVE] + data[i][THREE];

            tmp11 = z11 - z13; // Reusing tmp11
            final double tmp7 = z11 + z13;
            final double z5 = (z2 + z4) * imultipliers[FOUR];

            final double z1 = z2 * imultipliers[2] + z5;
            z2 = tmp11 * imultipliers[1]; // Reusing z2
            final double z3 = z4 * imultipliers[THREE] + z5;

            final double tmp6 = z3 - tmp7;
            final double tmp5 = z2 - tmp6;
            final double tmp4 = -z1 - tmp5;

            // --- Final values ---
            data[i][0] = tmp0 + tmp7;
            data[i][SEVEN] = tmp0 - tmp7;
            data[i][1] = tmp1 + tmp6;
            data[i][SIX] = tmp1 - tmp6;
            data[i][2] = tmp2 + tmp5;
            data[i][FIVE] = tmp2 - tmp5;
            data[i][THREE] = tmp3 + tmp4;
            data[i][FOUR] = tmp3 - tmp4;
        }
    }
}