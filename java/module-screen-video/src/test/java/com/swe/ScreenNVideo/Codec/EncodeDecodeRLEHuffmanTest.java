/**
 * Contributed by @alonot
 */

package com.swe.ScreenNVideo.Codec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Test class for EncodeDecodeRLEHuffman encoding and decoding functionality.
 */
public class EncodeDecodeRLEHuffmanTest {

    private final EncodeDecodeRLEHuffman codec = EncodeDecodeRLEHuffman.getInstance();

    @Test
    @DisplayName("Test simple 8x8 matrix encode and decode")
    public void testSimple8x8MatrixEncodeAndDecode() {
        // Create a simple 8x8 matrix with test data
        short[][] originalMatrix = {
            {100, 10,  5,  3,  0,  0,  0,  0},
            { 20, 15,  8,  2,  0,  0,  0,  0},
            { 12,  9,  4,  1,  0,  0,  0,  0},
            {  7,  6,  3,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0}
        };

        // Encode the matrix
        ByteBuffer encodedBuffer = ByteBuffer.allocate(1024);
        codec.zigZagRLE(originalMatrix, encodedBuffer);

        // Prepare buffer for reading
        encodedBuffer.flip();

        // Decode the matrix
        short[][] decodedMatrix = codec.revZigZagRLE(encodedBuffer);

        // Verify dimensions
        assertEquals(originalMatrix.length, decodedMatrix.length,
            "Height should match");
        assertEquals(originalMatrix[0].length, decodedMatrix[0].length,
            "Width should match");

        // Verify all values match
        assertMatrixEquals(originalMatrix, decodedMatrix,
            "Decoded matrix should match original matrix");

        // Print results for verification
        System.out.println("Original Matrix:");
        printMatrix(originalMatrix);
        System.out.println("\nDecoded Matrix:");
        printMatrix(decodedMatrix);
        System.out.println("\nTest passed! Matrices are identical.");
    }

    @Test
    @DisplayName("Test 8x8 matrix with all zeros")
    public void testAllZerosMatrix() {
        short[][] originalMatrix = new short[8][8];

        ByteBuffer encodedBuffer = ByteBuffer.allocate(1024);
        codec.zigZagRLE(originalMatrix, encodedBuffer);
        encodedBuffer.flip();

        short[][] decodedMatrix = codec.revZigZagRLE(encodedBuffer);

        assertMatrixEquals(originalMatrix, decodedMatrix,
            "All-zeros matrix should encode and decode correctly");
    }

    @Test
    @DisplayName("Test 8x8 matrix with single DC coefficient")
    public void testSingleDCCoefficient() {
        short[][] originalMatrix = new short[8][8];
        originalMatrix[0][0] = 50; // Only DC coefficient

        ByteBuffer encodedBuffer = ByteBuffer.allocate(1024);
        codec.zigZagRLE(originalMatrix, encodedBuffer);
        encodedBuffer.flip();

        short[][] decodedMatrix = codec.revZigZagRLE(encodedBuffer);

        assertMatrixEquals(originalMatrix, decodedMatrix,
            "Single DC coefficient should encode and decode correctly");
    }

    @Test
    @DisplayName("Test 8x8 matrix with negative values")
    public void testNegativeValues() {
        short[][] originalMatrix = {
            {100, -10,   5, -3,  0,  0,  0,  0},
            {-20,  15,  -8,  2,  0,  0,  0,  0},
            { 12,  -9,   4, -1,  0,  0,  0,  0},
            { -7,   6,  -3,  0,  0,  0,  0,  0},
            {  0,   0,   0,  0,  0,  0,  0,  0},
            {  0,   0,   0,  0,  0,  0,  0,  0},
            {  0,   0,   0,  0,  0,  0,  0,  0},
            {  0,   0,   0,  0,  0,  0,  0,  0}
        };

        ByteBuffer encodedBuffer = ByteBuffer.allocate(1024);
        codec.zigZagRLE(originalMatrix, encodedBuffer);
        encodedBuffer.flip();

        short[][] decodedMatrix = codec.revZigZagRLE(encodedBuffer);

        assertMatrixEquals(originalMatrix, decodedMatrix,
            "Negative values should encode and decode correctly");
    }

    @Test
    @DisplayName("Test 8x8 matrix with scattered non-zero values")
    public void testScatteredValues() {
        short[][] originalMatrix = {
            {50,  0,  3,  0,  0,  0,  0,  0},
            { 0,  5,  0,  0,  0,  0,  0,  0},
            { 2,  0,  0,  0,  0,  0,  0,  0},
            { 0,  0,  0,  1,  0,  0,  0,  0},
            { 0,  0,  0,  0,  0,  0,  0,  0},
            { 0,  0,  0,  0,  0,  0,  0,  0},
            { 0,  0,  0,  0,  0,  0,  2,  0},
            { 0,  0,  0,  0,  0,  0,  0,  0}
        };

        ByteBuffer encodedBuffer = ByteBuffer.allocate(1024);
        codec.zigZagRLE(originalMatrix, encodedBuffer);
        encodedBuffer.flip();

        short[][] decodedMatrix = codec.revZigZagRLE(encodedBuffer);

        assertMatrixEquals(originalMatrix, decodedMatrix,
            "Scattered values should encode and decode correctly");
    }

    @Test
    @DisplayName("Test typical DCT-like matrix")
    public void testDCTLikeMatrix() {
        // Simulates a typical DCT output with DC coefficient and decreasing AC coefficients
        short[][] originalMatrix = {
            {200, 50, 25, 12,  5,  2,  1,  0},
            { 45, 30, 15,  8,  3,  1,  0,  0},
            { 20, 12,  8,  4,  2,  0,  0,  0},
            { 10,  6,  3,  2,  1,  0,  0,  0},
            {  5,  3,  2,  1,  0,  0,  0,  0},
            {  2,  1,  1,  0,  0,  0,  0,  0},
            {  1,  0,  0,  0,  0,  0,  0,  0},
            {  0,  0,  0,  0,  0,  0,  0,  0}
        };

        ByteBuffer encodedBuffer = ByteBuffer.allocate(1024);
        codec.zigZagRLE(originalMatrix, encodedBuffer);
        encodedBuffer.flip();

        System.out.println("\nDCT-like matrix compression:");
        System.out.println("Original size: " + (8 * 8 * 2) + " bytes");
        System.out.println("Encoded size: " + encodedBuffer.limit() + " bytes");

        short[][] decodedMatrix = codec.revZigZagRLE(encodedBuffer);

        assertMatrixEquals(originalMatrix, decodedMatrix,
            "DCT-like matrix should encode and decode correctly");
    }

    @Test
    @DisplayName("Test 16x8 matrix with 2 blocks - detects buffer alignment issues")
    public void testMultipleBlocks16x8() {
        // Create a 16x8 matrix that will result in 2 horizontal 8x8 blocks
        // This tests if sequential block encoding/decoding maintains buffer alignment
        short[][] originalMatrix = new short[16][8];
        
        // First block (top half): different values
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                originalMatrix[i][j] = (short) ((i * 8 + j) % 50);
            }
        }
        
        // Second block (bottom half): different values to ensure blocks are distinct
        for (int i = 8; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                originalMatrix[i][j] = (short) ((i * 8 + j) % 30 + 20);
            }
        }

        // Encode the matrix
        ByteBuffer encodedBuffer = ByteBuffer.allocate(2048);
        int posBeforeEncode = encodedBuffer.position();
        codec.zigZagRLE(originalMatrix, encodedBuffer);
        int posAfterEncode = encodedBuffer.position();
        int encodedSize = posAfterEncode - posBeforeEncode;

        // Prepare buffer for reading
        encodedBuffer.flip();

        // Decode the matrix
        int posBeforeDecode = encodedBuffer.position();
        short[][] decodedMatrix = codec.revZigZagRLE(encodedBuffer);
        int posAfterDecode = encodedBuffer.position();
        int decodedSize = posAfterDecode - posBeforeDecode;

        // Verify dimensions
        assertEquals(originalMatrix.length, decodedMatrix.length,
            "Height should match");
        assertEquals(originalMatrix[0].length, decodedMatrix[0].length,
            "Width should match");

        // Verify buffer positions match (encoded and decoded should consume same bytes)
        assertEquals(encodedSize, decodedSize,
            "Encoded and decoded sizes should match. Encoded: " + encodedSize + ", Decoded: " + decodedSize);

        // Verify all values match
        assertMatrixEquals(originalMatrix, decodedMatrix,
            "Decoded matrix should match original matrix across 2 blocks");
    }

    @Test
    @DisplayName("Test 8x16 matrix with 2 blocks - detects buffer alignment issues")
    public void testMultipleBlocks8x16() {
        // Create an 8x16 matrix that will result in 2 vertical 8x8 blocks
        // This tests if sequential block encoding/decoding maintains buffer alignment
        short[][] originalMatrix = new short[8][16];
        
        // First block (left half): different values
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                originalMatrix[i][j] = (short) ((i * 8 + j) % 40 + 10);
            }
        }
        
        // Second block (right half): different values to ensure blocks are distinct
        for (int i = 0; i < 8; i++) {
            for (int j = 8; j < 16; j++) {
                originalMatrix[i][j] = (short) ((i * 8 + (j - 8)) % 35 + 25);
            }
        }

        // Encode the matrix
        ByteBuffer encodedBuffer = ByteBuffer.allocate(2048);
        int posBeforeEncode = encodedBuffer.position();
        codec.zigZagRLE(originalMatrix, encodedBuffer);
        int posAfterEncode = encodedBuffer.position();
        int encodedSize = posAfterEncode - posBeforeEncode;

        // Prepare buffer for reading
        encodedBuffer.flip();

        // Decode the matrix
        int posBeforeDecode = encodedBuffer.position();
        short[][] decodedMatrix = codec.revZigZagRLE(encodedBuffer);
        int posAfterDecode = encodedBuffer.position();
        int decodedSize = posAfterDecode - posBeforeDecode;

        // Verify dimensions
        assertEquals(originalMatrix.length, decodedMatrix.length,
            "Height should match");
        assertEquals(originalMatrix[0].length, decodedMatrix[0].length,
            "Width should match");

        // Verify buffer positions match (encoded and decoded should consume same bytes)
        assertEquals(encodedSize, decodedSize,
            "Encoded and decoded sizes should match. Encoded: " + encodedSize + ", Decoded: " + decodedSize);

        // Verify all values match
        assertMatrixEquals(originalMatrix, decodedMatrix,
            "Decoded matrix should match original matrix across 2 blocks");
    }

    /**
     * Helper method to print a matrix for visual verification
     */
    private void printMatrix(short[][] matrix) {
        for (short[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    /**
     * Helper method to compare two matrices element by element
     */
    private void assertMatrixEquals(short[][] expected, short[][] actual, String message) {
        assertEquals(expected.length, actual.length, message + " - row count mismatch");

        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i],
                message + " - mismatch at row " + i);
        }
    }
}