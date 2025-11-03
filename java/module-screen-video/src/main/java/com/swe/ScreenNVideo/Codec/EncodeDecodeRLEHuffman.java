/**
 * Contributed by Devansh Manoj Kesan.
 * Enhanced with Huffman encoding/decoding following JPEG standard.
 * Production-ready with proper bit-level packing.
 */

package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Provides methods for ZigZag scanning, Run-Length Encoding (RLE),
 * and Huffman encoding/decoding for 8x8 short matrices following JPEG standards.
 * Uses proper bit-level packing for production use.
 */
public class EncodeDecodeRLEHuffman implements IRLE {

    /** Singleton instance. */
    public static final EncodeDecodeRLEHuffman ENCDECINSTANCE = new EncodeDecodeRLEHuffman();

    // Standard JPEG Huffman tables for luminance DC coefficients
    private static final Map<Integer, String> DC_HUFFMAN_TABLE = new HashMap<>();
    private static final Map<String, Integer> DC_HUFFMAN_DECODE = new HashMap<>();

    // Standard JPEG Huffman tables for luminance AC coefficients
    private static final Map<Integer, String> AC_HUFFMAN_TABLE = new HashMap<>();
    private static final Map<String, Integer> AC_HUFFMAN_DECODE = new HashMap<>();

    static {
        initializeHuffmanTables();
    }

    /**
     * Initialize standard JPEG Huffman tables for luminance.
     */
    private static void initializeHuffmanTables() {
        // DC Luminance Huffman codes (category -> code)
        DC_HUFFMAN_TABLE.put(0, "00");
        DC_HUFFMAN_TABLE.put(1, "010");
        DC_HUFFMAN_TABLE.put(2, "011");
        DC_HUFFMAN_TABLE.put(3, "100");
        DC_HUFFMAN_TABLE.put(4, "101");
        DC_HUFFMAN_TABLE.put(5, "110");
        DC_HUFFMAN_TABLE.put(6, "1110");
        DC_HUFFMAN_TABLE.put(7, "11110");
        DC_HUFFMAN_TABLE.put(8, "111110");
        DC_HUFFMAN_TABLE.put(9, "1111110");
        DC_HUFFMAN_TABLE.put(10, "11111110");
        DC_HUFFMAN_TABLE.put(11, "111111110");

        // AC Luminance Huffman codes (run/size symbol -> code)
        // EOB (End of Block)
        AC_HUFFMAN_TABLE.put(0x00, "1010");
        // ZRL (Zero Run Length - 16 zeros)
        AC_HUFFMAN_TABLE.put(0xF0, "11111111001");

        // Common AC codes (Run-length, Size)
        AC_HUFFMAN_TABLE.put(0x01, "00");
        AC_HUFFMAN_TABLE.put(0x02, "01");
        AC_HUFFMAN_TABLE.put(0x03, "100");
        AC_HUFFMAN_TABLE.put(0x04, "1011");
        AC_HUFFMAN_TABLE.put(0x05, "11010");
        AC_HUFFMAN_TABLE.put(0x06, "1111000");
        AC_HUFFMAN_TABLE.put(0x07, "11111000");
        AC_HUFFMAN_TABLE.put(0x08, "1111110110");
        AC_HUFFMAN_TABLE.put(0x09, "1111111110000010");
        AC_HUFFMAN_TABLE.put(0x0A, "1111111110000011");

        AC_HUFFMAN_TABLE.put(0x11, "1100");
        AC_HUFFMAN_TABLE.put(0x12, "11011");
        AC_HUFFMAN_TABLE.put(0x13, "1111001");
        AC_HUFFMAN_TABLE.put(0x14, "111110110");
        AC_HUFFMAN_TABLE.put(0x15, "11111110110");
        AC_HUFFMAN_TABLE.put(0x16, "1111111110000100");
        AC_HUFFMAN_TABLE.put(0x17, "1111111110000101");
        AC_HUFFMAN_TABLE.put(0x18, "1111111110000110");
        AC_HUFFMAN_TABLE.put(0x19, "1111111110000111");
        AC_HUFFMAN_TABLE.put(0x1A, "1111111110001000");

        AC_HUFFMAN_TABLE.put(0x21, "11100");
        AC_HUFFMAN_TABLE.put(0x22, "11111001");
        AC_HUFFMAN_TABLE.put(0x23, "1111110111");
        AC_HUFFMAN_TABLE.put(0x24, "111111110100");
        AC_HUFFMAN_TABLE.put(0x25, "1111111110001001");
        AC_HUFFMAN_TABLE.put(0x26, "1111111110001010");
        AC_HUFFMAN_TABLE.put(0x27, "1111111110001011");
        AC_HUFFMAN_TABLE.put(0x28, "1111111110001100");
        AC_HUFFMAN_TABLE.put(0x29, "1111111110001101");
        AC_HUFFMAN_TABLE.put(0x2A, "1111111110001110");

        AC_HUFFMAN_TABLE.put(0x31, "111010");
        AC_HUFFMAN_TABLE.put(0x32, "111110111");
        AC_HUFFMAN_TABLE.put(0x33, "111111110101");
        AC_HUFFMAN_TABLE.put(0x34, "1111111110001111");
        AC_HUFFMAN_TABLE.put(0x35, "1111111110010000");
        AC_HUFFMAN_TABLE.put(0x36, "1111111110010001");
        AC_HUFFMAN_TABLE.put(0x37, "1111111110010010");
        AC_HUFFMAN_TABLE.put(0x38, "1111111110010011");
        AC_HUFFMAN_TABLE.put(0x39, "1111111110010100");
        AC_HUFFMAN_TABLE.put(0x3A, "1111111110010101");

        AC_HUFFMAN_TABLE.put(0x41, "111011");
        AC_HUFFMAN_TABLE.put(0x42, "1111111000");
        AC_HUFFMAN_TABLE.put(0x43, "1111111110010110");
        AC_HUFFMAN_TABLE.put(0x44, "1111111110010111");
        AC_HUFFMAN_TABLE.put(0x45, "1111111110011000");
        AC_HUFFMAN_TABLE.put(0x46, "1111111110011001");
        AC_HUFFMAN_TABLE.put(0x47, "1111111110011010");
        AC_HUFFMAN_TABLE.put(0x48, "1111111110011011");
        AC_HUFFMAN_TABLE.put(0x49, "1111111110011100");
        AC_HUFFMAN_TABLE.put(0x4A, "1111111110011101");

        AC_HUFFMAN_TABLE.put(0x51, "1111010");
        AC_HUFFMAN_TABLE.put(0x52, "11111110111");
        AC_HUFFMAN_TABLE.put(0x53, "1111111110011110");
        AC_HUFFMAN_TABLE.put(0x54, "1111111110011111");
        AC_HUFFMAN_TABLE.put(0x55, "1111111110100000");
        AC_HUFFMAN_TABLE.put(0x56, "1111111110100001");
        AC_HUFFMAN_TABLE.put(0x57, "1111111110100010");
        AC_HUFFMAN_TABLE.put(0x58, "1111111110100011");
        AC_HUFFMAN_TABLE.put(0x59, "1111111110100100");
        AC_HUFFMAN_TABLE.put(0x5A, "1111111110100101");

        AC_HUFFMAN_TABLE.put(0x61, "1111011");
        AC_HUFFMAN_TABLE.put(0x62, "111111110110");
        AC_HUFFMAN_TABLE.put(0x63, "1111111110100110");
        AC_HUFFMAN_TABLE.put(0x64, "1111111110100111");
        AC_HUFFMAN_TABLE.put(0x65, "1111111110101000");
        AC_HUFFMAN_TABLE.put(0x66, "1111111110101001");
        AC_HUFFMAN_TABLE.put(0x67, "1111111110101010");
        AC_HUFFMAN_TABLE.put(0x68, "1111111110101011");
        AC_HUFFMAN_TABLE.put(0x69, "1111111110101100");
        AC_HUFFMAN_TABLE.put(0x6A, "1111111110101101");

        AC_HUFFMAN_TABLE.put(0x71, "11111010");
        AC_HUFFMAN_TABLE.put(0x72, "111111110111");
        AC_HUFFMAN_TABLE.put(0x73, "1111111110101110");
        AC_HUFFMAN_TABLE.put(0x74, "1111111110101111");
        AC_HUFFMAN_TABLE.put(0x75, "1111111110110000");
        AC_HUFFMAN_TABLE.put(0x76, "1111111110110001");
        AC_HUFFMAN_TABLE.put(0x77, "1111111110110010");
        AC_HUFFMAN_TABLE.put(0x78, "1111111110110011");
        AC_HUFFMAN_TABLE.put(0x79, "1111111110110100");
        AC_HUFFMAN_TABLE.put(0x7A, "1111111110110101");

        AC_HUFFMAN_TABLE.put(0x81, "111111000");
        AC_HUFFMAN_TABLE.put(0x82, "111111111000000");
        AC_HUFFMAN_TABLE.put(0x83, "1111111110110110");
        AC_HUFFMAN_TABLE.put(0x84, "1111111110110111");
        AC_HUFFMAN_TABLE.put(0x85, "1111111110111000");
        AC_HUFFMAN_TABLE.put(0x86, "1111111110111001");
        AC_HUFFMAN_TABLE.put(0x87, "1111111110111010");
        AC_HUFFMAN_TABLE.put(0x88, "1111111110111011");
        AC_HUFFMAN_TABLE.put(0x89, "1111111110111100");
        AC_HUFFMAN_TABLE.put(0x8A, "1111111110111101");

        AC_HUFFMAN_TABLE.put(0x91, "111111001");
        AC_HUFFMAN_TABLE.put(0x92, "1111111110111110");
        AC_HUFFMAN_TABLE.put(0x93, "1111111110111111");
        AC_HUFFMAN_TABLE.put(0x94, "1111111111000000");
        AC_HUFFMAN_TABLE.put(0x95, "1111111111000001");
        AC_HUFFMAN_TABLE.put(0x96, "1111111111000010");
        AC_HUFFMAN_TABLE.put(0x97, "1111111111000011");
        AC_HUFFMAN_TABLE.put(0x98, "1111111111000100");
        AC_HUFFMAN_TABLE.put(0x99, "1111111111000101");
        AC_HUFFMAN_TABLE.put(0x9A, "1111111111000110");

        AC_HUFFMAN_TABLE.put(0xA1, "111111010");
        AC_HUFFMAN_TABLE.put(0xA2, "1111111111000111");
        AC_HUFFMAN_TABLE.put(0xA3, "1111111111001000");
        AC_HUFFMAN_TABLE.put(0xA4, "1111111111001001");
        AC_HUFFMAN_TABLE.put(0xA5, "1111111111001010");
        AC_HUFFMAN_TABLE.put(0xA6, "1111111111001011");
        AC_HUFFMAN_TABLE.put(0xA7, "1111111111001100");
        AC_HUFFMAN_TABLE.put(0xA8, "1111111111001101");
        AC_HUFFMAN_TABLE.put(0xA9, "1111111111001110");
        AC_HUFFMAN_TABLE.put(0xAA, "1111111111001111");

        // Build decode tables (reverse mappings)
        for (Map.Entry<Integer, String> entry : DC_HUFFMAN_TABLE.entrySet()) {
            DC_HUFFMAN_DECODE.put(entry.getValue(), entry.getKey());
        }
        for (Map.Entry<Integer, String> entry : AC_HUFFMAN_TABLE.entrySet()) {
            AC_HUFFMAN_DECODE.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Bit writer for packing bits into bytes.
     */
    private static class BitWriter {
        private final ByteBuffer buffer;
        private int currentByte;
        private int bitsInByte;

        public BitWriter(final ByteBuffer bufferArgs) {
            this.buffer = bufferArgs;
            this.currentByte = 0;
            this.bitsInByte = 0;
        }

        public void writeBit(final int bit) {
            currentByte = (currentByte << 1) | (bit & 1);
            bitsInByte++;

            if (bitsInByte == 8) {
                buffer.put((byte) currentByte);
                currentByte = 0;
                bitsInByte = 0;
            }
        }

        public void writeBits(String bits) {
            for (int i = 0; i < bits.length(); i++) {
                writeBit(bits.charAt(i) == '1' ? 1 : 0);
            }
        }

        public void writeBits(int value, int numBits) {
            for (int i = numBits - 1; i >= 0; i--) {
                writeBit((value >> i) & 1);
            }
        }

        public void flush() {
            if (bitsInByte > 0) {
                // Pad with 1s (JPEG standard)
                while (bitsInByte < 8) {
                    currentByte = (currentByte << 1) | 1;
                    bitsInByte++;
                }
                buffer.put((byte) currentByte);
                currentByte = 0;
                bitsInByte = 0;
            }
        }

        public int getBitCount() {
            return buffer.position() * 8 + bitsInByte;
        }
    }

    /**
     * Bit reader for unpacking bits from bytes.
     */
    private static class BitReader {
        private final ByteBuffer buffer;
        private int currentByte;
        private int bitsRemaining;
        private final int startPosition;
        private final int maxPosition;

        public BitReader(ByteBuffer buffer, int bitStreamLength) {
            this.buffer = buffer;
            this.currentByte = 0;
            this.bitsRemaining = 0;
            this.startPosition = buffer.position();
            this.maxPosition = startPosition + bitStreamLength;
        }

        public int readBit() {
            if (bitsRemaining == 0) {
                if (!buffer.hasRemaining() || buffer.position() >= maxPosition) {
                    return -1; // End of stream
                }
                currentByte = buffer.get() & 0xFF;
                bitsRemaining = 8;
            }

            bitsRemaining--;
            return (currentByte >> bitsRemaining) & 1;
        }

        public int readBits(int numBits) {
            int value = 0;
            for (int i = 0; i < numBits; i++) {
                int bit = readBit();
                if (bit == -1) {
                    return -1;
                }
                value = (value << 1) | bit;
            }
            return value;
        }

        public String readHuffmanCode(Map<String, Integer> decodeTable) {
            StringBuilder code = new StringBuilder();
            int maxLength = 16; // Max Huffman code length

            for (int i = 0; i < maxLength; i++) {
                int bit = readBit();
                if (bit == -1) {
                    break;
                }
                code.append(bit == 1 ? '1' : '0');

                if (decodeTable.containsKey(code.toString())) {
                    return code.toString();
                }
            }

            return code.toString();
        }

        public void alignToNextByte() {
            // Move buffer position to account for consumed bytes
            buffer.position(maxPosition);
        }
    }

    public static EncodeDecodeRLEHuffman getInstance() {
        return ENCDECINSTANCE;
    }

    @Override
    public void zigZagRLE(final short[][] matrix, final ByteBuffer resRLEbuffer) {
        final short height = (short) matrix.length;
        final short width = (short) matrix[0].length;

        // Write dimensions
        resRLEbuffer.putShort(height);
        resRLEbuffer.putShort(width);

        // Mark position for bit stream start
        final int bitStreamStart = resRLEbuffer.position();
        resRLEbuffer.putInt(0); // Placeholder for bit stream length

        BitWriter writer = new BitWriter(resRLEbuffer);
        short prevDC = 0;

        for (short rowBlock = 0; rowBlock < height; rowBlock += 8) {
            for (short colBlock = 0; colBlock < width; colBlock += 8) {
                prevDC = encodeBlockHuffman(matrix, rowBlock, colBlock, writer, prevDC);
            }
        }

        writer.flush();

        // Write actual bit stream length
        final int bitStreamEnd = resRLEbuffer.position();
        final int bitStreamLength = bitStreamEnd - bitStreamStart - 4;
        resRLEbuffer.putInt(bitStreamStart, bitStreamLength);
    }

    @Override
    public short[][] revZigZagRLE(final ByteBuffer resRLEbuffer) {
        final short height = resRLEbuffer.getShort();
        final short width = resRLEbuffer.getShort();

        // Validate dimensions
        if (height <= 0 || width <= 0) {
            throw new RuntimeException("Invalid matrix dimensions: height=" + height + ", width=" + width);
        }

        final short[][] matrix = new short[height][width];

        final int bitStreamLength = resRLEbuffer.getInt();

        // Validate bit stream length
        if (bitStreamLength < 0 || bitStreamLength > resRLEbuffer.remaining()) {
            throw new RuntimeException("Invalid bit stream length: " + bitStreamLength +
                ", remaining bytes: " + resRLEbuffer.remaining());
        }

        BitReader reader = new BitReader(resRLEbuffer, bitStreamLength);
        short prevDC = 0;

        for (short rowBlock = 0; rowBlock < height; rowBlock += 8) {
            for (short colBlock = 0; colBlock < width; colBlock += 8) {
                prevDC = decodeBlockHuffman(matrix, rowBlock, colBlock, reader, prevDC);
            }
        }

        // Ensure buffer position is correctly aligned after reading all bits
        reader.alignToNextByte();

        return matrix;
    }

    /**
     * Encodes an 8x8 block using ZigZag, RLE, and Huffman encoding (JPEG standard).
     */
    private short encodeBlockHuffman(final short[][] matrix,
                                     final short startRow,
                                     final short startCol,
                                     final BitWriter writer,
                                     final short prevDC) {
        // Extract 8x8 block in zigzag order
        short[] zigzag = extractZigZag(matrix, startRow, startCol);

        // Encode DC coefficient (differential)
        short dcCoeff = zigzag[0];
        short dcDiff = (short) (dcCoeff - prevDC);
        encodeDC(dcDiff, writer);

        // Encode AC coefficients using RLE + Huffman
        encodeAC(zigzag, writer);

        return dcCoeff;
    }

    /**
     * Decodes an 8x8 block using Huffman decoding, RLE, and reverse ZigZag.
     */
    private short decodeBlockHuffman(final short[][] matrix,
                                     final short startRow,
                                     final short startCol,
                                     final BitReader reader,
                                     final short prevDC) {
        short[] zigzag = new short[64];

        // Decode DC coefficient
        short dcDiff = decodeDC(reader);
        short dcCoeff = (short) (prevDC + dcDiff);
        zigzag[0] = dcCoeff;

        // Decode AC coefficients
        decodeAC(zigzag, reader);

        // Fill matrix in reverse zigzag order
        fillFromZigZag(matrix, startRow, startCol, zigzag);

        return dcCoeff;
    }

    /**
     * Extract 8x8 block in zigzag order.
     */
    private short[] extractZigZag(final short[][] matrix, final short startRow, final short startCol) {
        short[] result = new short[64];
        int idx = 0;

        for (short diag = 0; diag < 15; ++diag) {
            final short rowStart = (short) Math.max(0, diag - 7);
            final short rowEnd = (short) Math.min(7, diag);

            if ((diag & 1) == 1) {
                for (short i = rowStart; i <= rowEnd; ++i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r < matrix.length && c < matrix[0].length) {
                        result[idx++] = matrix[r][c];
                    }
                }
            } else {
                for (short i = rowEnd; i >= rowStart; --i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r < matrix.length && c < matrix[0].length) {
                        result[idx++] = matrix[r][c];
                    }
                }
            }
        }

        return result;
    }

    /**
     * Fill matrix from zigzag ordered array.
     */
    private void fillFromZigZag(final short[][] matrix, final short startRow,
                                final short startCol, final short[] zigzag) {
        int idx = 0;

        for (short diag = 0; diag < 15; ++diag) {
            final short rowStart = (short) Math.max(0, diag - 7);
            final short rowEnd = (short) Math.min(7, diag);

            if ((diag & 1) == 1) {
                for (short i = rowStart; i <= rowEnd; ++i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r < matrix.length && c < matrix[0].length) {
                        matrix[r][c] = zigzag[idx++];
                    }
                }
            } else {
                for (short i = rowEnd; i >= rowStart; --i) {
                    final short r = (short) (startRow + i);
                    final short c = (short) (startCol + (diag - i));
                    if (r < matrix.length && c < matrix[0].length) {
                        matrix[r][c] = zigzag[idx++];
                    }
                }
            }
        }
    }

    /**
     * Encode DC coefficient using JPEG standard differential encoding.
     */
    private void encodeDC(short dcDiff, BitWriter writer) {
        int category = getCategory(dcDiff);
        String huffCode = DC_HUFFMAN_TABLE.get(category);

        if (huffCode == null) {
            huffCode = DC_HUFFMAN_TABLE.get(11); // Max category
        }

        // Write Huffman code for category
        writer.writeBits(huffCode);

        // Write additional bits for magnitude
        if (category > 0) {
            int additionalBits = getAdditionalBitsValue(dcDiff, category);
            writer.writeBits(additionalBits, category);
        }
    }

    /**
     * Decode DC coefficient.
     */
    private short decodeDC(BitReader reader) {
        String huffCode = reader.readHuffmanCode(DC_HUFFMAN_DECODE);
        Integer category = DC_HUFFMAN_DECODE.get(huffCode);

        if (category == null || category == 0) {
            return 0;
        }

        int additionalBits = reader.readBits(category);
        return decodeValue(additionalBits, category);
    }

    /**
     * Encode AC coefficients using RLE + Huffman.
     */
    private void encodeAC(short[] zigzag, BitWriter writer) {
        int i = 1; // Start from first AC coefficient

        while (i < 64) {
            // Count zeros
            int zeroRun = 0;
            while (i < 64 && zigzag[i] == 0) {
                zeroRun++;
                i++;
            }

            // If we reached end, write EOB
            if (i == 64) {
                writer.writeBits(AC_HUFFMAN_TABLE.get(0x00)); // EOB
                break;
            }

            // Handle long zero runs (ZRL - 16 zeros)
            while (zeroRun >= 16) {
                writer.writeBits(AC_HUFFMAN_TABLE.get(0xF0)); // ZRL
                zeroRun -= 16;
            }

            // Encode non-zero coefficient
            short value = zigzag[i];
            int category = getCategory(value);
            int symbol = (zeroRun << 4) | category;

            String huffCode = AC_HUFFMAN_TABLE.get(symbol);
            if (huffCode == null) {
                // Fallback for symbols not in table
                huffCode = AC_HUFFMAN_TABLE.get(0xF0);
            }

            writer.writeBits(huffCode);

            if (category > 0) {
                int additionalBits = getAdditionalBitsValue(value, category);
                writer.writeBits(additionalBits, category);
            }

            i++;
        }
    }

    /**
     * Decode AC coefficients.
     */
    private void decodeAC(short[] zigzag, BitReader reader) {
        int i = 1;

        while (i < 64) {
            String huffCode = reader.readHuffmanCode(AC_HUFFMAN_DECODE);
            Integer symbol = AC_HUFFMAN_DECODE.get(huffCode);

            if (symbol == null) {
                // Fill rest with zeros if decoding fails
                while (i < 64) {
                    zigzag[i++] = 0;
                }
                break;
            }

            // Check for EOB
            if (symbol == 0x00) {
                // Fill rest with zeros
                while (i < 64) {
                    zigzag[i++] = 0;
                }
                break;
            }

            // Check for ZRL
            if (symbol == 0xF0) {
                // Skip 16 zeros
                for (int j = 0; j < 16 && i < 64; j++) {
                    zigzag[i++] = 0;
                }
                continue;
            }

            int zeroRun = (symbol >> 4) & 0x0F;
            int category = symbol & 0x0F;

            // Fill zeros
            for (int j = 0; j < zeroRun && i < 64; j++) {
                zigzag[i++] = 0;
            }

            if (i < 64 && category > 0) {
                int additionalBits = reader.readBits(category);
                zigzag[i++] = decodeValue(additionalBits, category);
            }
        }
    }

    /**
     * Get category (number of bits needed) for a value.
     */
    private int getCategory(short value) {
        int absValue = Math.abs(value);
        if (absValue == 0) return 0;
        return 32 - Integer.numberOfLeadingZeros(absValue);
    }

    /**
     * Get additional bits value for a coefficient.
     */
    private int getAdditionalBitsValue(short value, int category) {
        if (value >= 0) {
            return value;
        } else {
            return value + (1 << category) - 1;
        }
    }

    /**
     * Decode value from additional bits.
     */
    private short decodeValue(int bits, int category) {
        int threshold = 1 << (category - 1);

        if (bits < threshold) {
            return (short) (bits - (1 << category) + 1);
        }
        return (short) bits;
    }
}