/**
 * contributed by Anup Kumar.
 */

package com.swe.ScreenNVideo.Codec;

/**
 * This Class Do Decompression of Encoded Data.
 */
public class DeCompressor implements  IDeCompressor {
    /**
     * module which have implemented fdct and idct.
     */
    private IFIDCT dctmodule;
    /**
     * module which have Qunatisation table and implemented quantisation and dequantisation.
     */
    private QuantisationUtil quantmodule;

    /**
     * Unit Matrix Dimension (8x8).
     */
    private final int matrixDim = 8;

    DeCompressor() {
        dctmodule = AANdct.getInstance();
        quantmodule = QuantisationUtil.getInstance();
    }

    /**
     *  Doing the DeCompression of Chrominance (Cb,Cr) Matrix of Dimension height X width
     *  both height and width are divisible by 8
     *  steps :
     *  IDCT transformation <- Quantisation <- Decoding (Get Matrix) .
     * @param matrix : input matrix to be compressed
     * @param height : height of matrix
     * @param width : width of matrix
     */
    @Override
    public void decompressChrome(final short[][] matrix, final short height, final short width) {

        for (short i = 0; i < height; i += matrixDim) {
            for (short j = 0; j < width; j += matrixDim) {
                quantmodule.DeQuantisationChrome(matrix, i, j);
                dctmodule.Idct(matrix, i, j);
            }
        }
    }

    /**
     *  Doing the DeCompression of Luminance Matrix (Y) of Dimension height X width
     *  both height and width are divisibel by 8
     *  steps :
     *  IDCT transformation <- Quantisation <- Decoding (Get Matrix) .
     * @param matrix : input matrix to be compressed
     * @param height : height of matrix
     * @param width : width of matrix
     */
    @Override
    public void decompressLumin(final short[][] matrix, final short height, final short width) {

        for (short i = 0; i < height; i += matrixDim) {
            for (short j = 0; j < width; j += matrixDim) {
                quantmodule.DeQuantisationLumin(matrix, i, j);
                dctmodule.Idct(matrix, i, j);
            }
        }
    }
}
