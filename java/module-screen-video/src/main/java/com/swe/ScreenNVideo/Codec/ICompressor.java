/**
 * Contributed by Anup Kumar.
 */

package com.swe.ScreenNVideo.Codec;

/**
 * This Interface will be used by Compressors.
 */
interface  ICompressor {
    /**
     *  Doing the Compression of Chrominance (Cb,Cr) Matrix of Dimension height X width
     *  both height and width are divisible by 8
     *  steps :
     *  DCT transformation -> Quantisation .
     * @param matrix : input matrix to be compressed
     * @param height : height of matrix
     * @param width : width of matrix
     */
    void compressChrome(short[][] matrix, short height, short width);

    /**
     * Doing the Compression of Luminance Matrix (Y) of Dimension height X width
     * both height and width are divisibel by 8
     * steps :
     * DCT transformation -> Quantisation .
     *
     * @param matrix    : input matrix to be compressed
     * @param height    : height of matrix
     * @param width     : width of matrix
     */
    void compressLumin(short[][] matrix, short height, short width);
}
