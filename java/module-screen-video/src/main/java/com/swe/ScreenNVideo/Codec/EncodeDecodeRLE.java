/**
 * Contributed by Devansh Manoj Kesan.
 */

package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

/**
 * Provides methods for ZigZag scanning and Run-Length Encoding (RLE)
 * compression and decompression for 8x8 short matrices.
 */
public class EncodeDecodeRLE implements IRLE {

    /**
     * Block size for processing matrices (8x8 blocks).
     */
    private static final short BLOCK_SIZE = 8;

    /**
     * Number of diagonals in an 8x8 block for zigzag scanning.
     */
    private static final short NUM_DIAGONALS = 15;

    /**
     * Maximum index within a block (BLOCK_SIZE - 1).
     */
    private static final short MAX_BLOCK_INDEX = 7;

    /**
     * Initial count value for RLE encoding.
     */
    private static final short INITIAL_COUNT = 1;

    /**
     * Bit mask to check if a number is odd.
     */
    private static final short ODD_MASK = 1;

    /**
     * Zero value constant.
     */
    private static final short ZERO = 0;

    /** Singleton instance. */
    public static final EncodeDecodeRLE ENCODE_DECODE_RLE = new EncodeDecodeRLE();

    /**
     * Returns the singleton instance.
     *
     * @return encodeDecodeRLE instance
     */
    public static EncodeDecodeRLE getInstance() {
        return ENCODE_DECODE_RLE;
    }

    /**
     * Applies ZigZag scanning followed by RLE encoding for the given matrix.
     *
     * @param matrix        the input short matrix
     * @param resRLEbuffer  the output buffer where the result is written
     */
    @Override
    public void zigZagRLE(final short[][] matrix, final ByteBuffer resRLEbuffer) {
        final short height = (short) matrix.length;
        final short width = (short) matrix[0].length;

        // Write matrix dimensions at the beginning.
        resRLEbuffer.putShort(height);
        resRLEbuffer.putShort(width);

        // Process the matrix in 8x8 blocks.
        for (short rowBlock = ZERO; rowBlock < height; rowBlock += BLOCK_SIZE) {
            for (short colBlock = ZERO; colBlock < width; colBlock += BLOCK_SIZE) {
                // Perform zigzag scan + RLE on each 8x8 block.
                encodeBlock(matrix, rowBlock, colBlock, resRLEbuffer);
            }
        }
    }

    /**
     * Decodes data from the given buffer by reversing the RLE and ZigZag scan.
     *
     * @param resRLEbuffer  the input buffer containing encoded data
     * @return the reconstructed short matrix
     */
    @Override
    public short[][] revZigZagRLE(final ByteBuffer resRLEbuffer) {
        // Extract height and width from the start of the buffer.
        final short height = (short) resRLEbuffer.getShort();
        final short width = (short) resRLEbuffer.getShort();

        final short[][] matrix = new short[height][width];

        // Process the matrix block by block.
        for (short rowBlock = ZERO; rowBlock < height; rowBlock += BLOCK_SIZE) {
            for (short colBlock = ZERO; colBlock < width; colBlock += BLOCK_SIZE) {
                decodeBlock(matrix, rowBlock, colBlock, resRLEbuffer);
            }
        }

        return matrix;
    }

    /**
     * Encodes a 8x8 block using ZigZag scanning and RLE encoding.
     *
     * @param matrix        the input matrix
     * @param startRow      starting row index for the block
     * @param startCol      starting column index for the block
     * @param buffer        buffer to write encoded data
     */
    private void encodeBlock(final short[][] matrix,
                             final short startRow,
                             final short startCol,
                             final ByteBuffer buffer) {
        short prevVal = matrix[startRow][startCol];
        short count = INITIAL_COUNT;

        // ZigZag traversal
        for (short diag = ZERO; diag < NUM_DIAGONALS; ++diag) {
            final short rowStart = (short) Math.max(ZERO, diag - MAX_BLOCK_INDEX);
            final short rowEnd = (short) Math.min(MAX_BLOCK_INDEX, diag);

            if ((diag & ODD_MASK) == ODD_MASK) {
                for (short i = rowStart; i <= rowEnd; ++i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r >= matrix.length || c >= matrix[0].length) {
                        continue;
                    }
                    final short curr = matrix[r][c];
                    if (curr == prevVal) {
                        count++;
                    } else {
                        buffer.putShort(prevVal);
                        buffer.putShort(count);
                        prevVal = curr;
                        count = INITIAL_COUNT;
                    }
                }
            } else {
                for (short i = rowEnd; i >= rowStart; --i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r >= matrix.length || c >= matrix[0].length) {
                        continue;
                    }
                    final short curr = matrix[r][c];
                    if (curr == prevVal) {
                        count++;
                    } else {
                        buffer.putShort(prevVal);
                        buffer.putShort(count);
                        prevVal = curr;
                        count = INITIAL_COUNT;
                    }
                }
            }
        }

        buffer.putShort(prevVal);
        buffer.putShort(count);
    }

    /**
     * Decodes an 8x8 block using RLE decoding and reverse ZigZag scanning.
     *
     * @param matrix        the output matrix to fill
     * @param startRow      starting row index for the block
     * @param startCol      starting column index for the block
     * @param buffer        buffer to read encoded data
     */
    private void decodeBlock(final short[][] matrix,
                             final short startRow,
                             final short startCol,
                             final ByteBuffer buffer) {
        short currentVal = buffer.getShort();
        short remaining = buffer.getShort();

        // Rebuild using reverse zigzag
        for (short diag = ZERO; diag < NUM_DIAGONALS; ++diag) {
            final short rowStart = (short) Math.max(ZERO, diag - MAX_BLOCK_INDEX);
            final short rowEnd = (short) Math.min(MAX_BLOCK_INDEX, diag);

            if ((diag & ODD_MASK) == ODD_MASK) {
                for (short i = rowStart; i <= rowEnd; ++i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r >= matrix.length || c >= matrix[0].length) {
                        continue;
                    }

                    matrix[r][c] = currentVal;
                    remaining--;

                    if (remaining == ZERO) {
                        currentVal = buffer.getShort();
                        remaining = buffer.getShort();
                    }
                }
            } else {
                for (short i = rowEnd; i >= rowStart; --i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r >= matrix.length || c >= matrix[0].length) {
                        continue;
                    }

                    matrix[r][c] = currentVal;
                    remaining--;

                    if (remaining == ZERO) {
                        currentVal = buffer.getShort();
                        remaining = buffer.getShort();
                    }
                }
            }
        }
    }
}
