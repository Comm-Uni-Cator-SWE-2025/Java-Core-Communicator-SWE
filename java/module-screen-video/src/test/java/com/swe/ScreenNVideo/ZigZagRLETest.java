/**
 * Contributed by @Devansh-Kesan
 */

package com.swe.ScreenNVideo;

import com.swe.ScreenNVideo.Codec.EncodeDecodeRLE;

import java.nio.ByteBuffer;

/**
 * Test class for ZigZag RLE encoding/decoding.
 */
public class ZigZagRLETest {

    /**
     * Buffer size for encoding test.
     */
    private static final int BUFFER_SIZE = 10000;

    /**
     * Main method for testing ZigZag RLE.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        // Create a sample 8x8 matrix
        final short[][] original = {
            {10, 0, 30, 40, 50, 60, 70, 80},
            {15, 0, 0, 0, 0, 0, 0, 0},
            {12, 0, 0, 0, 0, 0, 0, 0},
            {18, 0, 0, 0, 0, 0, 0, 0},
            {11, 0, 0, 0, 0, 0, 0, 0},
            {14, 0, 0, 0, 0, 0, 0, 0},
            {16, 0, 0, 0, 0, 0, 0, 0},
            {19, 0, 0, 0, 0, 0, 0, 0},
        };

        System.out.println("Original Matrix:");
        printMatrix(original);

        // Encode using ZigZag + RLE
        final ByteBuffer encoded = ByteBuffer.allocate(BUFFER_SIZE);
        final EncodeDecodeRLE encoder = EncodeDecodeRLE.getInstance();
        encoder.zigZagRLE(original, encoded);

        // Prepare buffer for decoding
        encoded.flip();

        System.out.println("\nEncoded buffer size: " + encoded.remaining() + " bytes");

        final ByteBuffer copied = encoded.duplicate();  // Don't affect original
        System.out.println("\nEncoded Data (shorts):" + copied.limit());
        while (copied.hasRemaining()) {
            System.out.print(copied.getShort() + " ");
        }
        System.out.println();

        // Decode back
        final short[][] decoded = encoder.revZigZagRLE(encoded);

        System.out.println("\nDecoded Matrix:");
        printMatrix(decoded);

        // Verify if original and decoded match
        final boolean match = matricesMatch(original, decoded);
        System.out.println("\nMatrices match: " + match);
    }

    /**
     * Print a matrix.
     *
     * @param matrix the matrix to print
     */
    private static void printMatrix(final short[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(String.format("%4d ", matrix[i][j]));
            }
            System.out.println();
        }
    }

    /**
     * Check if two matrices match.
     *
     * @param m1 first matrix
     * @param m2 second matrix
     * @return true if matrices match
     */
    private static boolean matricesMatch(final short[][] m1, final short[][] m2) {
        if (m1.length != m2.length) {
            return false;
        }
        for (int i = 0; i < m1.length; i++) {
            if (m1[i].length != m2[i].length) {
                return false;
            }
            for (int j = 0; j < m1[i].length; j++) {
                if (m1[i][j] != m2[i][j]) {
                    System.out.println("Mismatch at [" + i + "][" + j + "]: "
                            + m1[i][j] + " vs " + m2[i][j]);
                    return false;
                }
            }
        }
        return true;
    }
}