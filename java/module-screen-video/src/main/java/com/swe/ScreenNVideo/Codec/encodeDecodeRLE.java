/**
 * Contributed by Devansh Manoj Kesan.
 */

package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

/**
 * Provides methods for ZigZag scanning and Run-Length Encoding (RLE)
 * compression and decompression for 8x8 short matrices.
 */
public class encodeDecodeRLE implements IRLE {

    /** Singleton instance. */
    public static final encodeDecodeRLE _encodeDecodeRLE = new encodeDecodeRLE();

    /**
     * Returns the singleton instance.
     *
     * @return encodeDecodeRLE instance
     */
    public static encodeDecodeRLE getInstance() {
        return _encodeDecodeRLE;
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
        for (short rowBlock = 0; rowBlock < height; rowBlock += 8) {
            for (short colBlock = 0; colBlock < width; colBlock += 8) {
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
        for (short rowBlock = 0; rowBlock < height; rowBlock += 8) {
            for (short colBlock = 0; colBlock < width; colBlock += 8) {
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
        short count = 1;

        // ZigZag traversal
        for (short diag = 0; diag < 15; ++diag) {
            final short rowStart = (short) Math.max(0, diag - 7);
            final short rowEnd = (short) Math.min(7, diag);

            if ((diag & 1) == 1) {
                for (short i = rowStart; i <= rowEnd; ++i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r >= matrix.length || c >= matrix[0].length) {
                        continue;
                    }
                    short curr = matrix[r][c];
                    if (curr == prevVal) {
                        count++;
                    } else {
                        buffer.putShort(prevVal);
                        buffer.putShort(count);
                        prevVal = curr;
                        count = 1;
                    }
                }
            } else {
                for (short i = rowEnd; i >= rowStart; --i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r >= matrix.length || c >= matrix[0].length) {
                        continue;
                    }
                    short curr = matrix[r][c];
                    if (curr == prevVal) {
                        count++;
                    } else {
                        buffer.putShort(prevVal);
                        buffer.putShort(count);
                        prevVal = curr;
                        count = 1;
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

        short pairIndex = 0;
        short currentVal = buffer.getShort();
        short remaining = buffer.getShort();

        // Rebuild using reverse zigzag
        for (short diag = 0; diag < 15; ++diag) {
            final short rowStart = (short) Math.max(0, diag - 7);
            final short rowEnd = (short) Math.min(7, diag);

            if ((diag & 1) == 1) {
                for (short i = rowStart; i <= rowEnd; ++i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r >= matrix.length || c >= matrix[0].length) {
                        continue;
                    }

                    matrix[r][c] = currentVal;
                    remaining--;

                    if (remaining == 0) {
                        currentVal = buffer.getShort();
                        remaining = buffer.getShort();
                    }
                }
            } else {
                for (int i = rowEnd; i >= rowStart; --i) {
                    final int r = startRow + i;
                    final int c = startCol + (diag - i);
                    if (r >= matrix.length || c >= matrix[0].length) {
                        continue;
                    }

                    matrix[r][c] = currentVal;
                    remaining--;

                    if (remaining == 0) {
                        currentVal = buffer.getShort();
                        remaining = buffer.getShort();
                    }
                }
            }
        }
    }
}
