/**
 * Contributed by Devansh Manoj Kesan.
 */

package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

/**
 * Interface for Run-Length Encoding (RLE) and ZigZag scanning operations.
 *
 * <p>Defines the contract for compressing a matrix into a buffer
 * and decompressing a buffer back into a matrix.
 */
interface IRLE {

    /**
     * Applies ZigZag scanning followed by RLE encoding to a matrix.
     *
     * @param matrix        the input short matrix
     * @param resRLEbuffer  the output buffer where the result is written
     */
    void zigZagRLE(short[][] matrix, ByteBuffer resRLEbuffer);

    /**
     * Decodes data from a buffer by reversing RLE and ZigZag scanning.
     *
     * @param resRLEbuffer  the input buffer containing encoded data
     * @return the reconstructed short matrix
     */
    short[][] revZigZagRLE(ByteBuffer resRLEbuffer);

}