package com.swe.ScreenNVideo.Codec;

/**
 * This FDCT and IDCT transformation is implemented
 * by using AAN algorithm
 */
public class AANdct implements IFIDCT {
    public double[] aanScaleFactor;
    double[] multipliers;
    private static final AANdct _aandctInstance = new AANdct();

    private AANdct(){
        aanScaleFactor = new double[8];
        multipliers = new double[5];
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

        // 1D IDCT on cols
        for(int j = 0;j<8;++j){
            z13 = (data[5][j] + data[3][j])/2;
            z2 = (data[5][j] - data[3][j])/2;
            z11 = (data[1][j] + data[7][j])/2;
            z4 = (data[1][j] - data[7][j])/2;

            tmp7 = (z11+z13)/2;
            z3 = (z11-z13)/2;

            tmp11 = z3/multipliers[2];


        }
    }

}


