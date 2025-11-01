/**
 * contributed by Anup Kumar.
 */

package com.swe.ScreenNVideo.Codec;

/**
 * This Interface will be Used By Decompressors.
 */
public interface IDeCompressor {

    /**
     *  Doing the DeCompression of Chrominance (Cb,Cr) Matrix of Dimension height X width
     *  both height and width are divisible by 8
     *  steps :
     *  IDCT transformation <- Quantisation <- Decoding (Get Matrix) .
     * @param matrix : input matrix to be compressed
     * @param height : height of matrix
     * @param width : width of matrix
     */
    void decompressChrome(short[][] matrix, short height, short width);

    /**
     *  Doing the DeCompression of Luminance Matrix (Y) of Dimension height X width
     *  both height and width are divisibel by 8
     *  steps :
     *  IDCT transformation <- Quantisation <- Decoding (Get Matrix) .
     * @param matrix : input matrix to be compressed
     * @param height : height of matrix
     * @param width : width of matrix
     */
    void decompressLumin(short[][] matrix, short height, short width);
};
