package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

class Compressor implements  ICompressor {
    /**
     * module which have implemented fdct and idct.
     */
    private IFIDCT dctmodule;
    /**
     * module which have Qunatisation table and implemented quantisation and dequantisation.
     */
    private QuantisationUtil quantmodule;
    /**
     * this is the module which will be used for encoding and decoding.
     */
    private IRLE enDeRLE;

    /**
     * Unit Matrix Dimension (8x8).
     */

    private final int matrixDim = 8;

    public long zigZagTime = 0;
    public long dctTime = 0;
    public long quantTime = 0;


    Compressor() {
        dctmodule = AANdct.getInstance();
        quantmodule = QuantisationUtil.getInstance();
        quantmodule.scaleQuantTable(dctmodule.getScaleFactor());
        enDeRLE = EncodeDecodeRLEHuffman.getInstance();
    }

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
    @Override
    public void compressChrome(final short[][] matrix, final short height, final short width, final ByteBuffer resBuffer) {

        resBuffer.putShort((short) (height / matrixDim));
        resBuffer.putShort((short) (width / matrixDim));

        for (short i = 0; i < height; i += matrixDim) {
            for (short j = 0; j < width; j += matrixDim) {
//                long curr = System.nanoTime();
                dctmodule.fdct(matrix, i, j);
//                dctTime += System.nanoTime() - curr;
//                curr = System.nanoTime();
                quantmodule.quantisationChrome(matrix, i, j);
//                quantTime += System.nanoTime() - curr;
            }
        }

//        long curr = System.nanoTime();
        enDeRLE.zigZagRLE(matrix, resBuffer);
//        zigZagTime += System.nanoTime() - curr;
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

        resBuffer.putShort((short) (height / matrixDim));
        resBuffer.putShort((short) (width / matrixDim));

        for (short i = 0; i < height; i += matrixDim) {
            for (short j = 0; j < width; j += matrixDim) {
//                long curr = System.nanoTime();
                dctmodule.fdct(matrix, i, j);
//                dctTime += System.nanoTime() - curr;
//                curr = System.nanoTime();
                quantmodule.quantisationLumin(matrix, i, j);
//                quantTime += System.nanoTime() - curr;
            }
        }

//        long curr = System.nanoTime();
        enDeRLE.zigZagRLE(matrix, resBuffer);
//        zigZagTime += System.nanoTime() - curr;
    }
}
