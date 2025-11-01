package com.swe.ScreenNVideo.Codec;

/**
 * This FDCT and IDCT transformation is implemented
 * by using AAN algorithm
 */
public class AANdct implements IFIDCT {
    public double[] aanScaleFactor;
    double[] multipliers;
    double[] imultipliers;
    private static final AANdct _aandctInstance = new AANdct();

    private AANdct(){
        aanScaleFactor = new double[8];
        multipliers = new double[5];
        imultipliers = new double[5];
        double[] C = new double[8];

        for(int i = 0;i<8;++i){
            C[i] = Math.cos(Math.PI * i / 16);
            aanScaleFactor[i] = 1/(C[i]*4);
        }

        aanScaleFactor[0] = 1/(2*Math.sqrt(2));

        multipliers[0] = C[4];
        multipliers[1] = C[2]-C[6];
        multipliers[2] = C[4];
        multipliers[3] = C[2]+C[6];
        multipliers[4] = C[6];

        imultipliers[1] = 1/Math.cos(Math.PI  / 4);
        imultipliers[2] = 1/Math.cos(Math.PI * 3 / 8);
        imultipliers[3] = 1/Math.cos(Math.PI / 8);
        imultipliers[4] = 1/(Math.cos(Math.PI / 8) + Math.cos(Math.PI * 3 / 8));

    }


    public static AANdct getInstance(){
        return _aandctInstance;
    }

    @Override
    public double[] getScaleFactor(){
        return aanScaleFactor;
    }
    @Override
    public void Fdct(short[][] matrix, short row, short col) {

        double[][] data = new double[8][8];

        // Copy 8x8 block into double array
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                data[i][j] = matrix[row + i][col + j];
            }
        }

        double tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
        double tmp10, tmp11, tmp12, tmp13;
        double z1, z2, z3, z4, z5, z11, z13;

        // 1D DCT on rows
        for (int i = 0; i < 8; i++) {

            // phase 1
            tmp0 = data[i][0] + data[i][7];
            tmp1 = data[i][1] + data[i][6];
            tmp2 = data[i][2] + data[i][5];
            tmp3 = data[i][3] + data[i][4];
            tmp4 = data[i][3] - data[i][4];
            tmp5 = data[i][2] - data[i][5];
            tmp6 = data[i][1] - data[i][6];
            tmp7 = data[i][0] - data[i][7];


            // phase 2
            tmp10 = tmp0 + tmp3;
            tmp11 = tmp1 + tmp2;
            tmp12 = tmp1 - tmp2;
            tmp13 = tmp0 - tmp3;

            // phase 3 (even part )
            data[i][0] = tmp10 + tmp11;
            data[i][4] = tmp10 - tmp11;

            z1 = (tmp12 + tmp13) * multipliers[0];

            data[i][2] = z1 + tmp13;
            data[i][6] = tmp13 - z1;

            // phase 3 (Odd part)
            tmp10 = -tmp4 - tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;

            z5 = (tmp10 + tmp12) * multipliers[4];
            z2 = -multipliers[1] * tmp10 - z5;
            z3 = multipliers[2] * tmp11;
            z4 = multipliers[3] * tmp12 - z5;

            z11 = tmp7 + z3;
            z13 = tmp7 - z3;

            data[i][5] = z13 + z2;
            data[i][1] = z11 + z4;
            data[i][7] = z11 - z4;
            data[i][3] = z13 - z2;

        }

        // 1D DCT on columns
        for (int j = 0; j < 8; j++) {
            // phase 1
            tmp0 = data[0][j] + data[7][j];
            tmp1 = data[1][j] + data[6][j];
            tmp2 = data[2][j] + data[5][j];
            tmp3 = data[3][j] + data[4][j];
            tmp4 = data[3][j] - data[4][j];
            tmp5 = data[2][j] - data[5][j];
            tmp6 = data[1][j] - data[6][j];
            tmp7 = data[0][j] - data[7][j];

            // pase 2
            tmp10 = tmp0 + tmp3;
            tmp11 = tmp1 + tmp2;
            tmp12 = tmp1 - tmp2;
            tmp13 = tmp0 - tmp3;

            // phase 3 (even part)
            data[0][j] = tmp10 + tmp11;
            data[4][j] = tmp10 - tmp11;

            z1 = (tmp12 + tmp13) * multipliers[0];
            data[2][j] = tmp13 + z1;
            data[6][j] = tmp13 - z1;

            // Odd part
            tmp10 = -tmp4 - tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;

            z5 = (tmp10 + tmp12) * multipliers[4];
            z2 = -multipliers[1] * tmp10 - z5;
            z3 = multipliers[2] * tmp11;
            z4 = multipliers[3] * tmp12 + z5;

            z11 = tmp7 + z3;
            z13 = tmp7 - z3;

            data[5][j] = z13 + z2;
            data[1][j] = z11 + z4;
            data[7][j] = z11 - z4;
            data[3][j] = z13 - z2;
        }

        // ---- store back the transformed block ----
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                matrix[row + i][col + j] = (short)Math.round(data[i][j]);
            }
        }
    }

    @Override
    public void Idct(short[][] matrix, short row, short col) {
        double[][] data = new double[8][8];

        // Copy 8x8 block into double array
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                data[i][j] = matrix[row + i][col + j];
            }
        }

        double tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
        double tmp10, tmp11, tmp12, tmp13;
        double z1, z2, z3, z4, z5, z11, z13;

        //1D IDCT on columns
        for (int j = 0; j < 8; j++) {

            //(even part)
            tmp10 = data[0][j] + data[4][j];
            tmp11 = data[0][j] - data[4][j];

            tmp12 = (data[2][j] - data[6][j]) * imultipliers[1];
            tmp13 = (data[2][j] + data[6][j]);

            tmp0 = tmp10 + tmp13;
            tmp1 = tmp11 + tmp12;
            tmp2 = tmp11 - tmp12;
            tmp3 = tmp10 - tmp13;

            // (odd part)
            z2 = data[5][j] - data[3][j];
            z11 = data[1][j] + data[7][j];
            z4 = data[1][j] - data[7][j];
            z13 = data[5][j] + data[3][j];

            tmp11 = z11 - z13;
            tmp7 = z11 + z13;
            z5 = (z2 + z4) * imultipliers[4];

            z1 = z2 * imultipliers[2] + z5;
            z2 = tmp11 * imultipliers[1];
            z3 = z4 * imultipliers[3] + z5;

            tmp6 = z3 - tmp7;
            tmp5 = z2 - tmp6;
            tmp4 = -z1 -tmp5;

            data[0][j] = tmp0 + tmp7;
            data[7][j] = tmp0 - tmp7;
            data[1][j] = tmp1 + tmp6;
            data[6][j] = tmp1 - tmp6;
            data[2][j] = tmp2 + tmp5;
            data[5][j] = tmp2 - tmp5;
            data[3][j] = tmp3 + tmp4;
            data[4][j] = tmp3 - tmp4;
        }

        // ---- 1D IDCT on rows ----
        for (int i = 0; i < 8; i++) {

            //(even part)
            tmp10 = data[i][0] + data[i][4];
            tmp11 = data[i][0] - data[i][4];

            tmp12 = (data[i][2] - data[i][6]) * imultipliers[1];
            tmp13 = (data[i][2] + data[i][6]);

            tmp0 = tmp10 + tmp13;
            tmp1 = tmp11 + tmp12;
            tmp2 = tmp11 - tmp12;
            tmp3 = tmp10 - tmp13;

            // (odd part)
            z2 = data[i][5] - data[i][3];
            z11 = data[i][1] + data[i][7];
            z4 = data[i][1] - data[i][7];
            z13 = data[i][5] + data[i][3];

            tmp11 = z11 - z13;
            tmp7 = z11 + z13;
            z5 = (z2 + z4) * imultipliers[4];

            z1 = z2 * imultipliers[2] + z5;
            z2 = tmp11 * imultipliers[1];
            z3 = z4 * imultipliers[3] + z5;



            tmp6 = z3 - tmp7;
            tmp5 = z2 - tmp6;
            tmp4 = -z1 -tmp5;

            data[i][0] = tmp0 + tmp7;
            data[i][7] = tmp0 - tmp7;
            data[i][1] = tmp1 + tmp6;
            data[i][6] = tmp1 - tmp6;
            data[i][2] = tmp2 + tmp5;
            data[i][5] = tmp2 - tmp5;
            data[i][3] = tmp3 + tmp4;
            data[i][4] = tmp3 - tmp4;
        }

        // ---- store back the reconstructed block ----
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                matrix[row + i][col + j] = (short)Math.round(data[i][j] / 8.0);
            }
        }
    }

}


