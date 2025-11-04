package com.swe.ScreenNVideo;

import com.swe.ScreenNVideo.Codec.EncodeDecodeRLE;

import java.nio.ByteBuffer;

public class ZigZagRLETest {

    public static void main(String[] args) {
        // Create a sample 8x8 matrix
        short[][] original = {
            {10, 0, 30, 40, 50, 60, 70, 80},
            {15, 0, 0, 0,0,0,0,0},
            {12, 0, 0, 0,0,0,0,0},
            {18, 0, 0, 0,0,0,0,0},
            {11, 0, 0, 0,0,0,0,0},
            {14, 0, 0, 0,0,0,0,0},
            {16, 0, 0, 0,0,0,0,0},
            {19, 0, 0, 0,0,0,0,0}
        };

        System.out.println("Original Matrix:");
        printMatrix(original);

        // Encode using ZigZag + RLE
        ByteBuffer encoded = ByteBuffer.allocate(10000);
        EncodeDecodeRLE encoder = EncodeDecodeRLE.getInstance();
        encoder.zigZagRLE(original, encoded);

        // Prepare buffer for decoding
        encoded.flip();

        System.out.println("\nEncoded buffer size: " + encoded.remaining() + " bytes");

        ByteBuffer copied = encoded.duplicate();  // Don't affect original
        System.out.println("\nEncoded Data (shorts):" + copied.limit());
        while (copied.hasRemaining()) {
            System.out.print(copied.getShort() + " ");
        }
        System.out.println();

        // Decode back
        short[][] decoded = encoder.revZigZagRLE(encoded);

        System.out.println("\nDecoded Matrix:");
        printMatrix(decoded);

        // Verify if original and decoded match
        boolean match = matricesMatch(original, decoded);
        System.out.println("\nMatrices match: " + match);
    }

    private static void printMatrix(short[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(String.format("%4d ", matrix[i][j]));
            }
            System.out.println();
        }
    }

    private static boolean matricesMatch(short[][] m1, short[][] m2) {
        if (m1.length != m2.length) return false;
        for (int i = 0; i < m1.length; i++) {
            if (m1[i].length != m2[i].length) return false;
            for (int j = 0; j < m1[i].length; j++) {
                if (m1[i][j] != m2[i][j]) {
                    System.out.println("Mismatch at [" + i + "][" + j + "]: " +
                        m1[i][j] + " vs " + m2[i][j]);
                    return false;
                }
            }
        }
        return true;
    }
}