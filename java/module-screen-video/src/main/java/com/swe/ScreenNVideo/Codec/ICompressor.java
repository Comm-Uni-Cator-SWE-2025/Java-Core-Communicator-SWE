/**
 * Contributed by Anup Kumar.
 */

package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

/**
 * This Interface will be used by Compressors.
 */
interface  ICompressor {
    /**
     *  Doing the Compression of Chrominance (Cb,Cr) Matrix of Dimension height X width
     *  both height and width are divisible by 8
     *  steps :
     *  DCT transformation -> Quantisation -> ZigzagScan -> Run Length Encoding .
     * @param matrix : input matrix to be compressed
     * @param height : height of matrix
     * @param width : width of matrix
     * @param resBuffer : The Resultant buffer where encoded matrix will be stored
     */
    void compressChrome(short[][] matrix, short height, short width, ByteBuffer resBuffer);

    /**
     * Doing the Compression of Luminance Matrix (Y) of Dimension height X width
     * both height and width are divisibel by 8
     * steps :
     * DCT transformation -> Quantisation -> ZigzagScan -> Run Length Encoding .
     *
     * @param matrix    : input matrix to be compressed
     * @param height    : height of matrix
     * @param width     : width of matrix
     * @param resBuffer : The Resultant buffer where encoded matrix will be stored
     */
    void compressLumin(short[][] matrix, short height, short width, ByteBuffer resBuffer);
};
