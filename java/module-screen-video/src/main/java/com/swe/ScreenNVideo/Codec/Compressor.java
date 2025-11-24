/**
 * Contributed by @anup.
 */

package com.swe.ScreenNVideo.Codec;

/**
 * Implements the ICompressor interface to provide JPEG-like compression logic.
 * This class orchestrates the various steps:
 * 1. Forward Discrete Cosine Transform (FDCT)
 * 2. Quantization
 */
class Compressor implements ICompressor {
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
     * Time taken for ZigZag operations.
     */
    private long zigZagTime = 0;
    /**
     * Time taken for DCT operations.
     */
    private long dctTime = 0;
    /**
     * Time taken for quantization operations.
     */
    private long quantTime = 0;


    /**
     * Package-private constructor.
     * Initializes the compressor by obtaining singleton instances of the
     * DCT, Quantization .
     */
    Compressor() {
        dctmodule = AANdct.getInstance();
        quantmodule = QuantisationUtil.getInstance();
    }

    /**
     * Doing the Compression of Chrominance (Cb,Cr) Matrix of Dimension height X width
     * both height and width are divisible by 8
     * steps :
     * DCT transformation -> Quantisation.
     *
     * @param matrix    : input matrix to be compressed
     * @param height    : height of matrix
     * @param width     : width of matrix
     */
    @Override
    public void compressChrome(final short[][] matrix, final short height, final short width) {

        for (short i = 0; i < height; i += matrixDim) {
            for (short j = 0; j < width; j += matrixDim) {
                dctmodule.fdct(matrix, i, j);
//                dctTime += System.nanoTime() - curr;
//                curr = System.nanoTime();
                quantmodule.quantisationChrome(matrix, i, j);
//                quantTime += System.nanoTime() - curr;
            }
        }

    }

    /**
     * Doing the Compression of Luminance Matrix (Y) of Dimension height X width
     * both height and width are divisibel by 8
     * steps :
     * DCT transformation -> Quantisation.
     *
     * @param matrix    : input matrix to be compressed
     * @param height    : height of matrix
     * @param width     : width of matrix
     */
    @Override
    public void compressLumin(final short[][] matrix, final short height, final short width) {

        for (short i = 0; i < height; i += matrixDim) {
            for (short j = 0; j < width; j += matrixDim) {
                dctmodule.fdct(matrix, i, j);
//                dctTime += System.nanoTime() - curr;
//                curr = System.nanoTime();
                quantmodule.quantisationLumin(matrix, i, j);
//                quantTime += System.nanoTime() - curr;
            }
        }

    }
}