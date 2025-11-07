package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

// Contributed by Anup Kumar
/**
 * Implements the ICompressor interface to provide JPEG-like compression logic.
 * This class orchestrates the various steps:
 * 1. Forward Discrete Cosine Transform (FDCT)
 * 2. Quantization
 * 3. Run-Length Encoding (RLE) with ZigZag scan
 * <p>
 * It is package-private and intended to be used within the Codec package.
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
     * this is the module which will be used for encoding and decoding.
     */
    private final IRLE enDeRLE;

    /**
     * Unit Matrix Dimension (8x8).
     */
    private final int MATRIX_DIM = 8;

    /**
     * Time taken for ZigZag operations.
     */
    public long zigZagTime = 0;
    /**
     * Time taken for DCT operations.
     */
    public long dctTime = 0;
    /**
     * Time taken for quantization operations.
     */
    public long quantTime = 0;


    /**
     * Package-private constructor.
     * Initializes the compressor by obtaining singleton instances of the
     * DCT, Quantization, and RLE modules.
     */
    Compressor() {
        dctmodule = AANdct.getInstance();
        quantmodule = QuantisationUtil.getInstance();
//        quantmodule.scaleQuantTable(dctmodule.getScaleFactor());
        enDeRLE = EncodeDecodeRLE.getInstance();
    }

    /**
     * Doing the Compression of Chrominance (Cb,Cr) Matrix of Dimension height X width
     * both height and width are divisible by 8
     * steps :
     * DCT transformation -> Quantisation -> ZigzagScan -> Run Length Encoding .
     *
     * @param matrix    : input matrix to be compressed
     * @param height    : height of matrix
     * @param width     : width of matrix
     * @param resBuffer : The Resultant buffer where encoded matrix will be stored
     */
    @Override
    public void compressChrome(final short[][] matrix, final short height, final short width, final ByteBuffer resBuffer) {

        for (short i = 0; i < height; i += MATRIX_DIM) {
            for (short j = 0; j < width; j += MATRIX_DIM) {
                dctmodule.fdct(matrix, i, j);
//                dctTime += System.nanoTime() - curr;
//                curr = System.nanoTime();
                quantmodule.quantisationChrome(matrix, i, j);
//                quantTime += System.nanoTime() - curr;
            }
        }

        long curr = System.nanoTime();
        enDeRLE.zigZagRLE(matrix, resBuffer);
        zigZagTime += System.nanoTime() - curr;
    }

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
    @Override
    public void compressLumin(final short[][] matrix, final short height, final short width, final ByteBuffer resBuffer) {

        for (short i = 0; i < height; i += MATRIX_DIM) {
            for (short j = 0; j < width; j += MATRIX_DIM) {
                dctmodule.fdct(matrix, i, j);
//                dctTime += System.nanoTime() - curr;
//                curr = System.nanoTime();
                quantmodule.quantisationLumin(matrix, i, j);
//                quantTime += System.nanoTime() - curr;
            }
        }

        long curr = System.nanoTime();
        enDeRLE.zigZagRLE(matrix, resBuffer);
        zigZagTime += System.nanoTime() - curr;
    }
}