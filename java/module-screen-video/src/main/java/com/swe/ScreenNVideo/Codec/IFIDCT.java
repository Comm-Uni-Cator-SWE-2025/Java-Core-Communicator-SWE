package com.swe.ScreenNVideo.Codec;

/**
 * Forward and Inverse DCT interface
 */
interface IFIDCT{

    void Fdct(short[][] matrix, short row, short col);

    void Idct(short[][] matrix, short row, short col);

    double[] getScaleFactor();
};