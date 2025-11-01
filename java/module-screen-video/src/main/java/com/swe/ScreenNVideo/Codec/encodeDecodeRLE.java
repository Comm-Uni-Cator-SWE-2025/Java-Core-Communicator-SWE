/**
 * Contributed by Devansh Manoj Kesan.
 */

package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;

/**
 * Provides methods for ZigZag scanning and Run-Length Encoding (RLE)
 * compression and decompression for 8x8 short matrices.
 */
public class encodeDecodeRLE implements IRLE {

    public static int extraCount = 0;
    public static int tcount = 0;

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
        extraCount = 0;
        tcount = 0;

        // Process the matrix in 8x8 blocks.
        for (short rowBlock = 0; rowBlock < height; rowBlock += 8) {
            for (short colBlock = 0; colBlock < width; colBlock += 8) {
                // Perform zigzag scan + RLE on each 8x8 block.
                encodeBlock(matrix, rowBlock, colBlock, resRLEbuffer);
            }
        }

//        System.err.println("RLE Extra Count: " + extraCount + " Total Count: " + tcount);
        int actualNeeded = matrix.length * matrix[0].length * 2;
//        System.err.println("RLE Buffer Size: " + resRLEbuffer.position() + " Actual Needed: " + actualNeeded);
        double ratio = (resRLEbuffer.position() * 1.0) / actualNeeded;
        if (ratio > 1.0) {
//            System.err.println("RLE Compression Ratio > 1.0 : " + ratio);
//            System.err.println("Height: " + height + " Width: " + width);
//            System.err.println("resRLEbuffer position: " + resRLEbuffer.position());
//            System.err.println("Actual Needed: " + actualNeeded);
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
        short count = 0;
        int prevPos = buffer.position();

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
                    if (curr != 0) {
                        if (count > 0) {
                            buffer.putShort((short)0);
                            buffer.putShort(count);
                            count = 0;
                        }
                        // directly store non-zero values
                        buffer.putShort(curr);
                    } else {
                        count ++;
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
                    if (curr != 0) {
                        if (count > 0) {
                            buffer.putShort((short)0);
                            buffer.putShort(count);
                            count = 0;
                        }
                        // directly store non-zero values
                        buffer.putShort(curr);
                    } else {
                        count ++;
                    }
                }
            }
        }

        if (count > 0) {
            buffer.putShort((short)0);
            buffer.putShort(count);
        }

        tcount ++;
        if (buffer.position() - prevPos > 128) {
            extraCount ++;
        }
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
        short remaining = 1;
        if (currentVal == 0) {
            remaining = buffer.getShort();
        }

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

                    if (remaining == 0 && buffer.hasRemaining()) {
                        currentVal = buffer.getShort();
                        if (currentVal == 0) {
                            remaining = buffer.getShort();
                        } else {
                            remaining = 1;
                        }
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

                    if (remaining == 0 && buffer.hasRemaining()) {
                        currentVal = buffer.getShort();
                        if (currentVal == 0) {
                            remaining = buffer.getShort();
                        } else {
                            remaining = 1;
                        }
                    }
                }
            }
        }
    }
}
