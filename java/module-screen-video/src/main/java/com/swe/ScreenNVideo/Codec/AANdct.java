package com.swe.ScreenNVideo.Codec;

/**
 * This FDCT and IDCT transformation is implemented
 * by using AAN (Arai-Agui-Nakajima) Fast DCT algorithm.
 */
public class AANdct implements IFIDCT {
    /**
     * ScaleFactor which is used in FDCT to scale DCT coefficient
     * but to prevent repeated calculation it will be used
     * to scale quantisation table.
     */
    private final double[] aanScaleFactor;
    /**
     * count of aanScaleFactor.
     */
    private final int scaleFactorCount = 8;
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
    private final int three = 3;

    /**
     * index four.
     */

    private final int four = 4;

    /**
     * index five.
     */
    private final int five = 5;

    /**
     * index six.
     */
    private final int six = 6;

    /**
     * index seven.
     */
    private final int seven = 7;


    private AANdct() {

        aanScaleFactor = new double[scaleFactorCount];
        final int multipliersCount = 5;
        multipliers = new double[multipliersCount];
        imultipliers = new double[multipliersCount];

        final double[] cVals = new double[scaleFactorCount];
        final int cosBase = 16;
        for (int i = 0; i < scaleFactorCount; ++i) {
            
            cVals[i] = Math.cos(Math.PI * i / cosBase);
            aanScaleFactor[i] = 1 / (cVals[i] * four);
        }
        
        final int one = 1;

        final int two = 2;
        aanScaleFactor[0] = one / (two * Math.sqrt(two));

        multipliers[0] = cVals[four];
        multipliers[1] = cVals[two] - cVals[six];
        multipliers[2] = cVals[four];
        multipliers[three] = cVals[two] + cVals[six];
        multipliers[four] = cVals[six];

        imultipliers[1] = 1 / Math.cos(Math.PI  / four);
        final int eight = 8;
        final double cos = Math.cos(Math.PI * three / eight);
        imultipliers[2] = 1 / cos;
        imultipliers[three] = 1 / Math.cos(Math.PI / eight);
        imultipliers[four] = 1 / (Math.cos(Math.PI / eight) + cos);

    }


    public static AANdct getInstance() {
        return AANDCTINSTANCE;
    }

    @Override
    public double[] getScaleFactor() {
        return aanScaleFactor;
    }

    /**
     * Applies the 1D FDCT to each row of the 8x8 data block.
     *
     * @param data store the transformed coefficient.
     */
    private void fdctRow(final double[][] data) {

        for (int i = 0; i < scaleFactorCount; i++) {

            // --- Phase 1 ---
            // Calculate all 8 intermediate sums/differences from the input row
            final double tmp0 = data[i][0] + data[i][seven];
            final double tmp7 = data[i][0] - data[i][seven];
            final double tmp1 = data[i][1] + data[i][six];
            final double tmp6 = data[i][1] - data[i][six];
            final double tmp2 = data[i][2] + data[i][five];
            final double tmp5 = data[i][2] - data[i][five];
            final double tmp3 = data[i][three] + data[i][four];
            final double tmp4 = data[i][three] - data[i][four];

            // --- Phase 2 ---
            // Combine the sums/differences from phase 1
            double tmp10 = tmp0 + tmp3;
            double tmp11 = tmp1 + tmp2;
            double tmp12 = tmp1 - tmp2;
            final double tmp13 = tmp0 - tmp3;

            // --- Phase 3 (Even part) ---
            // Calculate and store Y(0), Y(4)
            data[i][0] = tmp10 + tmp11;
            data[i][four] = tmp10 - tmp11;

            // Calculate and store Y(2), Y(6)
            final double z1 = (tmp12 + tmp13) * multipliers[0];
            data[i][2] = z1 + tmp13;
            data[i][six] = tmp13 - z1;

            // --- Phase 3 (Odd part) ---
            // Reuse tmp10, tmp11, tmp12 for odd calculations
            tmp10 = -tmp4 - tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;

            final double z5 = (tmp10 + tmp12) * multipliers[four];
            final double z2 = -multipliers[1] * tmp10 - z5;
            final double z3 = multipliers[2] * tmp11;
            final double z4 = multipliers[three] * tmp12 - z5;

            final double z11 = tmp7 + z3;
            final double z13 = tmp7 - z3;

            // Calculate and store Y(1), Y(3), Y(5), Y(7)
            data[i][five] = z13 + z2;
            data[i][1] = z11 + z4;
            data[i][seven] = z11 - z4;
            data[i][three] = z13 - z2;
        }
    }

    /**
     * Applies the 1D FDCT to each column of the 8x8 data block.
     *
     * @param data The 8x8 block of data to be transformed.
     */
    private void fdctCol(final double[][] data) {


        for (int j = 0; j < scaleFactorCount; j++) {

            // --- Phase 1 ---
            // Calculate all 8 intermediate sums/differences from the input column
            final double tmp0 = data[0][j] + data[seven][j];
            final double tmp7 = data[0][j] - data[seven][j];
            final double tmp1 = data[1][j] + data[six][j];
            final double tmp6 = data[1][j] - data[six][j];
            final double tmp2 = data[2][j] + data[five][j];
            final double tmp5 = data[2][j] - data[five][j];
            final double tmp3 = data[three][j] + data[four][j];
            final double tmp4 = data[three][j] - data[four][j];

            // --- Phase 2 ---
            // Combine the sums/differences from phase 1
            double tmp10 = tmp0 + tmp3;
            double tmp11 = tmp1 + tmp2;
            double tmp12 = tmp1 - tmp2;
            final double tmp13 = tmp0 - tmp3;

            // --- Phase 3 (Even part) ---
            // Calculate and store Y(0), Y(4)
            data[0][j] = tmp10 + tmp11;
            data[four][j] = tmp10 - tmp11;

            // Calculate and store Y(2), Y(6)
            final double z1 = (tmp12 + tmp13) * multipliers[0];
            data[2][j] = z1 + tmp13;
            data[six][j] = tmp13 - z1;

            // --- Phase 3 (Odd part) ---
            // Reuse tmp10, tmp11, tmp12 for odd calculations
            tmp10 = -tmp4 - tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;

            final double z5 = (tmp10 + tmp12) * multipliers[four];
            final double z2 = -multipliers[1] * tmp10 - z5;
            final double z3 = multipliers[2] * tmp11;
            final double z4 = multipliers[three] * tmp12 - z5;

            final double z11 = tmp7 + z3;
            final double z13 = tmp7 - z3;

            // Calculate and store Y(1), Y(3), Y(5), Y(7)
            data[five][j] = z13 + z2;
            data[1][j] = z11 + z4;
            data[seven][j] = z11 - z4;
            data[three][j] = z13 - z2;
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
        final double[][] data = new double[scaleFactorCount][scaleFactorCount];

        // Copy 8x8 block into double array
        for (int i = 0; i < scaleFactorCount; i++) {
            for (int j = 0; j < scaleFactorCount; j++) {
                data[i][j] = matrix[row + i][col + j];
            }
        }

        // row wise transformation
        fdctRow(data);

        // column wise transformation
        fdctCol(data);

        final double normalizeFactor = 1;

        // ---- store back the transformed block ----
        for (int i = 0; i < scaleFactorCount; i++) {
            for (int j = 0; j < scaleFactorCount; j++) {
                matrix[row + i][col + j] = (short) Math.round(data[i][j] * normalizeFactor);
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
        final double[][] data = new double[scaleFactorCount][scaleFactorCount];
        for (int i = 0; i < scaleFactorCount; ++i) {
            for (int j = 0; j < scaleFactorCount; ++j) {
                data[i][j] = matrix[row + i][col + j];
            }
        }

        // 2. 1D IDCT on columns
        idctCol(data);

        // 3. 1D IDCT on rows
        idctRow(data);

        final double scaling = 8;
        
        // 4. Store back the reconstructed block
        for (int i = 0; i < scaleFactorCount; i++) {
            for (int j = 0; j < scaleFactorCount; j++) {
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
        for (int j = 0; j < scaleFactorCount; j++) {
            // --- Even part ---
            final double tmp10 = data[0][j] + data[four][j];
            double tmp11 = data[0][j] - data[four][j];
            final double tmp12 = (data[2][j] - data[six][j]) * imultipliers[1];
            final double tmp13 = data[2][j] + data[six][j];

            final double tmp0 = tmp10 + tmp13;
            final double tmp1 = tmp11 + tmp12;
            final double tmp2 = tmp11 - tmp12;
            final double tmp3 = tmp10 - tmp13;

            // --- Odd part ---
            double z2 = data[five][j] - data[three][j];
            final double z11 = data[1][j] + data[seven][j];
            final double z4 = data[1][j] - data[seven][j];
            final double z13 = data[five][j] + data[three][j];

            tmp11 = z11 - z13; // Reusing tmp11
            final double tmp7 = z11 + z13;
            final double z5 = (z2 + z4) * imultipliers[four];

            final double z1 = z2 * imultipliers[2] + z5;
            z2 = tmp11 * imultipliers[1]; // Reusing z2
            final double z3 = z4 * imultipliers[three] + z5;

            final double tmp6 = z3 - tmp7;
            final double tmp5 = z2 - tmp6;
            final double tmp4 = -z1 - tmp5;

            // --- Final values ---
            data[0][j] = tmp0 + tmp7;
            data[seven][j] = tmp0 - tmp7;
            data[1][j] = tmp1 + tmp6;
            data[six][j] = tmp1 - tmp6;
            data[2][j] = tmp2 + tmp5;
            data[five][j] = tmp2 - tmp5;
            data[three][j] = tmp3 + tmp4;
            data[four][j] = tmp3 - tmp4;
        }
    }

    /**
     * Performs a 1D IDCT on all rows of the 8x8 data block.
     *
     * @param data The 8x8 data block.
     */
    private void idctRow(final double[][] data) {
        for (int i = 0; i < scaleFactorCount; i++) {
            // --- Even part ---
            final double tmp10 = data[i][0] + data[i][four];
            double tmp11 = data[i][0] - data[i][four];
            final double tmp12 = (data[i][2] - data[i][six]) * imultipliers[1];
            final double tmp13 = data[i][2] + data[i][six];

            final double tmp0 = tmp10 + tmp13;
            final double tmp1 = tmp11 + tmp12;
            final double tmp2 = tmp11 - tmp12;
            final double tmp3 = tmp10 - tmp13;

            // --- Odd part ---
            double z2 = data[i][five] - data[i][three];
            final double z11 = data[i][1] + data[i][seven];
            final double z4 = data[i][1] - data[i][seven];
            final double z13 = data[i][five] + data[i][three];

            tmp11 = z11 - z13; // Reusing tmp11
            final double tmp7 = z11 + z13;
            final double z5 = (z2 + z4) * imultipliers[four];

            final double z1 = z2 * imultipliers[2] + z5;
            z2 = tmp11 * imultipliers[1]; // Reusing z2
            final double z3 = z4 * imultipliers[three] + z5;

            final double tmp6 = z3 - tmp7;
            final double tmp5 = z2 - tmp6;
            final double tmp4 = -z1 - tmp5;

            // --- Final values ---
            data[i][0] = tmp0 + tmp7;
            data[i][seven] = tmp0 - tmp7;
            data[i][1] = tmp1 + tmp6;
            data[i][six] = tmp1 - tmp6;
            data[i][2] = tmp2 + tmp5;
            data[i][five] = tmp2 - tmp5;
            data[i][three] = tmp3 + tmp4;
            data[i][four] = tmp3 - tmp4;
        }
    }
}


