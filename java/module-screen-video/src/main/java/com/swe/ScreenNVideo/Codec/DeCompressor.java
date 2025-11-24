// contributed by Anup kumar

package com.swe.ScreenNVideo.Codec;

/**
 * This Class Do Decompression of Encoded Data.
 * It coordinates dequantization and IDCT.
 */
public class DeCompressor implements IDeCompressor {
    /**
     * module which have implemented fdct and idct.
     */
    private final IFIDCT dctmodule;
    /**
     * module which have Qunatisation table and implemented quantisation and dequantisation.
     */
    private final QuantisationUtil quantmodule;

    /**
     * Unit Matrix Dimension (8x8).
     */
    private final int matrixDim = 8;

    /**
     * Initializes the decompressor by obtaining singleton instances of the
     * DCT and Quantization modules.
     */
    public DeCompressor() {
        dctmodule = AANdct.getInstance();
        quantmodule = QuantisationUtil.getInstance();
    }

    /**
     * Doing the DeCompression of Chrominance (Cb,Cr) Matrix of Dimension height X width
     * both height and width are divisible by 8
     * steps :
     * IDCT transformation <- DeQuantisation.
     *
     * @param matrix : input matrix to be compressed
     * @param height : height of matrix
     * @param width  : width of matrix
     */
    @Override
    public void decompressChrome(final short[][] matrix, final short height, final short width) {

        for (short i = 0; i < height; i += matrixDim) {
            for (short j = 0; j < width; j += matrixDim) {
                quantmodule.deQuantisationChrome(matrix, i, j);
                dctmodule.idct(matrix, i, j);
            }
        }
    }

    /**
     * Doing the DeCompression of Luminance Matrix (Y) of Dimension height X width
     * both height and width are divisibel by 8
     * steps :
     * IDCT transformation <- DeQuantisation .
     *
     * @param matrix : input matrix to be compressed
     * @param height : height of matrix
     * @param width  : width of matrix
     */
    @Override
    public void decompressLumin(final short[][] matrix, final short height, final short width) {

        for (short i = 0; i < height; i += matrixDim) {
            for (short j = 0; j < width; j += matrixDim) {
                quantmodule.deQuantisationLumin(matrix, i, j);
                dctmodule.idct(matrix, i, j);
            }
        }
    }
}