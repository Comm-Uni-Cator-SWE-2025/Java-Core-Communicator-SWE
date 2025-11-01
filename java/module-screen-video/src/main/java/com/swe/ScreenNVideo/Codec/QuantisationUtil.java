package com.swe.ScreenNVideo.Codec;

/**
 * It includes Quantisation Table
 * It includes Method to scale it
 * required for compressing and decompressing
 */
public class QuantisationUtil {
    // Annex K, Table K.1 of the JPEG Standard (ITU-T T.81 / ISO 10918-1, 1992)
    // Microsoft Word - T081E.DOC
    int[][] quantChrome = {
            {17, 18, 24, 47, 99, 99, 99, 99},
            {18, 21, 26, 66, 99, 99, 99, 99},
            {24, 26, 56, 99, 99, 99, 99, 99},
            {47, 66, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99}
    };
    int[][] quantLumin = {
            {16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68,109,103, 77},
            {24, 35, 55, 64, 81,104,113, 92},
            {49, 64, 78, 87,103,121,120,101},
            {72, 92, 95, 98,112,100,103, 99}
    };

    private static final QuantisationUtil _quantisationUtil = new QuantisationUtil();

    private QuantisationUtil(){}

    public static QuantisationUtil getInstance(){
        return _quantisationUtil;
    }

    public void setCompressonResulation(int q){
        int scaleFactor = 1;
        if(q>=1 && q<50){
            scaleFactor = (5000)/q;
        }else if(q>=50 && q<100){
            scaleFactor = 200-2*q;
        }

        for(int i = 0;i<8;++i){
            for(int j = 0;j<8;++j){
                int scaledCVal = (quantChrome[i][j]*scaleFactor + 50)/100;
                int scaledLVal = (quantLumin[i][j]*scaleFactor + 50)/100;

                if(scaledCVal<1) scaledCVal = 1;
                else if(scaledCVal>255) scaledCVal = 255;

                if(scaledLVal<1) scaledLVal = 1;
                else if(scaledLVal>255) scaledLVal = 255;

                quantChrome[i][j] = scaledCVal;
                quantLumin[i][j] = scaledLVal;
            }
        }
    }

    public void scaleQuantTable(double[] scalingFactors){
        for(int i = 0;i<8;++i){
            for(int j = 0;j<8;++j){

                int scaledCVal = (int)((quantChrome[i][j]/(scalingFactors[i]*scalingFactors[j]))+0.5);
                int scaledLVal = (int)((quantLumin[i][j]/(scalingFactors[i]*scalingFactors[j]))+0.5);

                if(scaledCVal<1) scaledCVal = 1;
                else if(scaledCVal>255) scaledCVal = 255;

                if(scaledLVal<1) scaledLVal = 1;
                else if(scaledLVal>255) scaledLVal = 255;

                quantChrome[i][j] = scaledCVal;
                quantLumin[i][j] = scaledLVal;
            }
        }
    }

    public void QuantisationChrome(short[][] DMatrix,short row, short col){
        for(int i = 0;i<8;++i){
            for(int j = 0;j<8;++j){
                DMatrix[i+row][j+col] = (short)Math.round((float) DMatrix[i+row][j+col] /quantChrome[i][j]);
            }
        }
    }

    public void QuantisationLumin(short[][] DMatrix,short row, short col){
        for(int i = 0;i<8;++i){
            for(int j = 0;j<8;++j){
                DMatrix[i+row][j+col] = (short)Math.round((float) DMatrix[i+row][j+col] /quantLumin[i][j]);
            }
        }
    }

    public void DeQuantisationChrome(short[][] DMatrix,short row, short col){
        for(int i = 0;i<8;++i){
            for(int j = 0;j<8;++j){
                DMatrix[i+row][j+col] = (short) (DMatrix[i+row][j+col] * quantChrome[i][j]);
            }
        }
    }

    public void DeQuantisationLumin(short[][] DMatrix,short row, short col){
        for(int i = 0;i<8;++i){
            for(int j = 0;j<8;++j){
                DMatrix[i+row][j+col] = (short) (DMatrix[i+row][j+col]  * quantLumin[i][j]);
            }
        }
    }
}
