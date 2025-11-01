/**
 * Contributed by Anup Kumar.
 */

package com.swe.ScreenNVideo.Codec;

/**
 * Forward and Inverse DCT interface.
 */
interface IFIDCT {

    /**
     * Main public function to perform the 2D Forward DCT.
     *
     * @param matrix The input/output matrix.
     * @param row    The starting row of the 8x8 block.
     * @param col    The starting col of the 8x8 block.
     */
    void Fdct(short[][] matrix, short row, short col);

    /**
     * Main public function to perform the 2D Inverse DCT.
     *
     * @param matrix The input/output matrix.
     * @param row    The starting row of the 8x8 block.
     * @param col    The starting col of the 8x8 block.
     */
    void Idct(short[][] matrix, short row, short col);

    /**
     * Scale factor will be used by Quantisation
     * @return : ScaleFactor Array
     */
    double[] getScaleFactor();
};