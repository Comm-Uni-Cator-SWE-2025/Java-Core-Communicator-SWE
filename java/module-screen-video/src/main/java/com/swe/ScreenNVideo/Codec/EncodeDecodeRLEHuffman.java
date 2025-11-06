/**
 * Contributed by Devansh Manoj Kesan.
 * Enhanced with Huffman encoding/decoding following JPEG standard.
 */

package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Provides methods for ZigZag scanning, Run-Length Encoding (RLE),
 * and Huffman encoding/decoding for 8x8 short matrices following JPEG standards.
 */
public class EncodeDecodeRLEHuffman implements IRLE {

    /** Singleton instance. */
    public static final EncodeDecodeRLEHuffman ENCDECINSTANCE = new EncodeDecodeRLEHuffman();

    // Pre-computed ZigZag indices for 8x8 blocks (row * 8 + col)
    private static final byte[] ZIGZAG_INDEX = new byte[64];

    // Reverse lookup: zigzag position -> linear index
    private static final byte[] REVERSE_ZIGZAG_INDEX = new byte[64];

    // Standard JPEG Huffman tables - using arrays for faster lookup
    private static final String[] DC_HUFFMAN_CODES = new String[12];
    private static final String[] AC_HUFFMAN_CODES = new String[256];

    // Trie-based decode structures for O(code_length) decoding
    private static final HuffmanDecodeNode DC_DECODE_ROOT = new HuffmanDecodeNode();
    private static final HuffmanDecodeNode AC_DECODE_ROOT = new HuffmanDecodeNode();

    // Pre-computed category lookup table for values -2047 to 2047
    private static final byte[] CATEGORY_LOOKUP = new byte[4096];

    // Standard JPEG luminance AC table specification
    private static final byte[] STD_AC_LUMINANCE_BITS = {
        0x00, 0x02, 0x01, 0x03, 0x03, 0x02, 0x04, 0x03,
        0x05, 0x05, 0x04, 0x04, 0x00, 0x00, 0x01, 0x7d
    };

    private static final short[] STD_AC_LUMINANCE_VALUES = {
        0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31,
        0x41, 0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14, 0x32,
        0x81, 0x91, 0xA1, 0x08, 0x23, 0x42, 0xB1, 0xC1, 0x15, 0x52,
        0xD1, 0xF0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0A, 0x16,
        0x17, 0x18, 0x19, 0x1A, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A,
        0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x43, 0x44, 0x45,
        0x46, 0x47, 0x48, 0x49, 0x4A, 0x53, 0x54, 0x55, 0x56, 0x57,
        0x58, 0x59, 0x5A, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69,
        0x6A, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x83,
        0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x92, 0x93, 0x94,
        0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0xA2, 0xA3, 0xA4, 0xA5,
        0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6,
        0xB7, 0xB8, 0xB9, 0xBA, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7,
        0xC8, 0xC9, 0xCA, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8,
        0xD9, 0xDA, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8,
        0xE9, 0xEA, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8,
        0xF9, 0xFA
    };

    /**
     * Trie node for fast Huffman decoding without string allocations.
     */
    private static class HuffmanDecodeNode {
        HuffmanDecodeNode zero;
        HuffmanDecodeNode one;
        int value = -1; // -1 means non-leaf

        void insert(String code, int val) {
            HuffmanDecodeNode curr = this;
            for (int i = 0; i < code.length(); i++) {
                if (code.charAt(i) == '0') {
                    if (curr.zero == null) curr.zero = new HuffmanDecodeNode();
                    curr = curr.zero;
                } else {
                    if (curr.one == null) curr.one = new HuffmanDecodeNode();
                    curr = curr.one;
                }
            }
            curr.value = val;
        }
    }

    static {
        initializeAll();
    }

    /**
     * Initialize all lookup tables and structures.
     */
    private static void initializeAll() {
        initializeZigZagIndices();
        initializeCategoryLookup();
        initializeHuffmanTables();
    }

    /**
     * Pre-compute zigzag traversal indices for 8x8 blocks.
     */
    private static void initializeZigZagIndices() {
        int idx = 0;
        for (int diag = 0; diag < 15; diag++) {
            int rowStart = Math.max(0, diag - 7);
            int rowEnd = Math.min(7, diag);

            if ((diag & 1) == 1) {
                for (int i = rowStart; i <= rowEnd; i++) {
                    int r = i;
                    int c = diag - i;
                    ZIGZAG_INDEX[idx] = (byte)(r * 8 + c);
                    REVERSE_ZIGZAG_INDEX[r * 8 + c] = (byte)idx;
                    idx++;
                }
            } else {
                for (int i = rowEnd; i >= rowStart; i--) {
                    int r = i;
                    int c = diag - i;
                    ZIGZAG_INDEX[idx] = (byte)(r * 8 + c);
                    REVERSE_ZIGZAG_INDEX[r * 8 + c] = (byte)idx;
                    idx++;
                }
            }
        }
    }

    /**
     * Pre-compute category lookup for fast category determination.
     */
    private static void initializeCategoryLookup() {
        for (int i = 0; i < 4096; i++) {
            int value = i - 2048; // Range: -2048 to 2047
            int absValue = Math.abs(value);
            if (absValue == 0) {
                CATEGORY_LOOKUP[i] = 0;
            } else {
                CATEGORY_LOOKUP[i] = (byte)(32 - Integer.numberOfLeadingZeros(absValue));
            }
        }
    }

    /**
     * Initialize standard JPEG Huffman tables.
     */
    private static void initializeHuffmanTables() {
        // DC Luminance Huffman codes
        DC_HUFFMAN_CODES[0] = "00";
        DC_HUFFMAN_CODES[1] = "010";
        DC_HUFFMAN_CODES[2] = "011";
        DC_HUFFMAN_CODES[3] = "100";
        DC_HUFFMAN_CODES[4] = "101";
        DC_HUFFMAN_CODES[5] = "110";
        DC_HUFFMAN_CODES[6] = "1110";
        DC_HUFFMAN_CODES[7] = "11110";
        DC_HUFFMAN_CODES[8] = "111110";
        DC_HUFFMAN_CODES[9] = "1111110";
        DC_HUFFMAN_CODES[10] = "11111110";
        DC_HUFFMAN_CODES[11] = "111111110";

        buildAcHuffmanTable();
        buildDecodeTries();
    }

    private static void buildAcHuffmanTable() {
        int code = 0;
        int valueIndex = 0;

        for (int bitLength = 1; bitLength <= 16; bitLength++) {
            int codesForLength = STD_AC_LUMINANCE_BITS[bitLength - 1];

            for (int i = 0; i < codesForLength; i++) {
                int symbol = STD_AC_LUMINANCE_VALUES[valueIndex++];
                AC_HUFFMAN_CODES[symbol] = toBinaryString(code, bitLength);
                code++;
            }
            code <<= 1;
        }
    }

    private static void buildDecodeTries() {
        // Build DC trie
        for (int i = 0; i < DC_HUFFMAN_CODES.length; i++) {
            if (DC_HUFFMAN_CODES[i] != null) {
                DC_DECODE_ROOT.insert(DC_HUFFMAN_CODES[i], i);
            }
        }

        // Build AC trie
        for (int i = 0; i < AC_HUFFMAN_CODES.length; i++) {
            if (AC_HUFFMAN_CODES[i] != null) {
                AC_DECODE_ROOT.insert(AC_HUFFMAN_CODES[i], i);
            }
        }
    }

    /**
     * Fast binary string conversion without allocations.
     */
    private static String toBinaryString(int value, int length) {
        char[] chars = new char[length];
        for (int i = length - 1; i >= 0; i--) {
            chars[i] = (char)('0' + (value & 1));
            value >>= 1;
        }
        return new String(chars);
    }

    /**
     * Bit writer with minimal branching.
     */
    private static class BitWriter {
        private final ByteBuffer buffer;
        private int currentByte;
        private int bitsInByte;

        public BitWriter(ByteBuffer bufferArgs) {
            this.buffer = bufferArgs;
            this.currentByte = 0;
            this.bitsInByte = 0;
        }

        public void writeBits(String bits) {
            for (int i = 0, len = bits.length(); i < len; i++) {
                currentByte = (currentByte << 1) | (bits.charAt(i) - '0');
                if (++bitsInByte == 8) {
                    buffer.put((byte)currentByte);
                    currentByte = 0;
                    bitsInByte = 0;
                }
            }
        }

        public void writeBits(int value, int numBits) {
            // Process bits in chunks for better performance
            while (numBits > 0) {
                int bitsToWrite = Math.min(numBits, 8 - bitsInByte);
                int shift = numBits - bitsToWrite;
                int mask = (1 << bitsToWrite) - 1;
                int bits = (value >> shift) & mask;

                currentByte = (currentByte << bitsToWrite) | bits;
                bitsInByte += bitsToWrite;
                numBits -= bitsToWrite;

                if (bitsInByte == 8) {
                    buffer.put((byte)currentByte);
                    currentByte = 0;
                    bitsInByte = 0;
                }
            }
        }

        public void flush() {
            if (bitsInByte > 0) {
                currentByte <<= (8 - bitsInByte);
                currentByte |= (1 << (8 - bitsInByte)) - 1; // Pad with 1s
                buffer.put((byte)currentByte);
                currentByte = 0;
                bitsInByte = 0;
            }
        }
    }

    /**
     * Bit reader with trie-based decoding.
     */
    private static class BitReader {
        private final ByteBuffer buffer;
        private int currentByte;
        private int bitsRemaining;
        private final int maxPosition;

        public BitReader(ByteBuffer buffer, int bitStreamLength) {
            this.buffer = buffer;
            this.currentByte = 0;
            this.bitsRemaining = 0;
            this.maxPosition = buffer.position() + bitStreamLength;
        }

        private int readBit() {
            if (bitsRemaining == 0) {
                if (!buffer.hasRemaining() || buffer.position() >= maxPosition) {
                    return -1;
                }
                currentByte = buffer.get() & 0xFF;
                bitsRemaining = 8;
            }
            return (currentByte >> --bitsRemaining) & 1;
        }

        public int readBits(int numBits) {
            int value = 0;
            while (numBits > 0) {
                if (bitsRemaining == 0) {
                    if (!buffer.hasRemaining() || buffer.position() >= maxPosition) {
                        return -1;
                    }
                    currentByte = buffer.get() & 0xFF;
                    bitsRemaining = 8;
                }

                int bitsToRead = Math.min(numBits, bitsRemaining);
                int shift = bitsRemaining - bitsToRead;
                int mask = (1 << bitsToRead) - 1;
                value = (value << bitsToRead) | ((currentByte >> shift) & mask);
                bitsRemaining -= bitsToRead;
                numBits -= bitsToRead;
            }
            return value;
        }

        public int decodeHuffman(HuffmanDecodeNode root) {
            HuffmanDecodeNode curr = root;
            for (int i = 0; i < 16; i++) { // Max code length
                int bit = readBit();
                if (bit == -1) return -1;

                curr = (bit == 0) ? curr.zero : curr.one;
                if (curr == null) return -1;
                if (curr.value != -1) return curr.value;
            }
            return -1;
        }

        public void alignToNextByte() {
            buffer.position(maxPosition);
        }
    }

    public static EncodeDecodeRLEHuffman getInstance() {
        return ENCDECINSTANCE;
    }

    @Override
    public void zigZagRLE(short[][] matrix, ByteBuffer resRLEbuffer) {
        final int height = matrix.length;
        final int width = matrix[0].length;

        resRLEbuffer.putShort((short)height);
        resRLEbuffer.putShort((short)width);

        final int bitStreamStart = resRLEbuffer.position();
        resRLEbuffer.putInt(0); // Placeholder

        BitWriter writer = new BitWriter(resRLEbuffer);
        short prevDC = 0;

        // Process blocks
        for (int rowBlock = 0; rowBlock < height; rowBlock += 8) {
            for (int colBlock = 0; colBlock < width; colBlock += 8) {
                prevDC = encodeBlockHuffman(matrix, rowBlock, colBlock, writer, prevDC);
            }
        }

        writer.flush();

        final int bitStreamEnd = resRLEbuffer.position();
        resRLEbuffer.putInt(bitStreamStart, bitStreamEnd - bitStreamStart - 4);
    }

    @Override
    public short[][] revZigZagRLE(ByteBuffer resRLEbuffer) {
        final short height = resRLEbuffer.getShort();
        final short width = resRLEbuffer.getShort();

        if (height <= 0 || width <= 0) {
            throw new RuntimeException("Invalid dimensions: " + height + "x" + width);
        }

        final short[][] matrix = new short[height][width];
        final int bitStreamLength = resRLEbuffer.getInt();

        if (bitStreamLength < 0 || bitStreamLength > resRLEbuffer.remaining()) {
            throw new RuntimeException("Invalid bit stream length: " + bitStreamLength);
        }

        BitReader reader = new BitReader(resRLEbuffer, bitStreamLength);
        short prevDC = 0;

        for (int rowBlock = 0; rowBlock < height; rowBlock += 8) {
            for (int colBlock = 0; colBlock < width; colBlock += 8) {
                prevDC = decodeBlockHuffman(matrix, rowBlock, colBlock, reader, prevDC);
            }
        }

        reader.alignToNextByte();
        return matrix;
    }

    /**
     * Encodes an 8x8 block using ZigZag, RLE, and Huffman encoding (JPEG standard).
     */
    private short encodeBlockHuffman(short[][] matrix, int startRow, int startCol,
                                     BitWriter writer, short prevDC) {
        // Extract in zigzag order using pre-computed indices
        short dcCoeff = matrix[startRow][startCol];
        encodeDC((short)(dcCoeff - prevDC), writer);

        // Encode AC coefficients
        int i = 1;
        while (i < 64) {
            int zeroRun = 0;
            while (i < 64) {
                int idx = ZIGZAG_INDEX[i] & 0xFF;
                int r = startRow + (idx >> 3);
                int c = startCol + (idx & 7);
                if (r < matrix.length && c < matrix[0].length && matrix[r][c] == 0) {
                    zeroRun++;
                    i++;
                } else {
                    break;
                }
            }

            if (i == 64) {
                writer.writeBits(AC_HUFFMAN_CODES[0x00]); // EOB
                break;
            }

            while (zeroRun >= 16) {
                writer.writeBits(AC_HUFFMAN_CODES[0xF0]); // ZRL
                zeroRun -= 16;
            }

            int idx = ZIGZAG_INDEX[i] & 0xFF;
            int r = startRow + (idx >> 3);
            int c = startCol + (idx & 7);
            short value = (r < matrix.length && c < matrix[0].length) ? matrix[r][c] : 0;

            int category = getCategoryFast(value);
            int symbol = (zeroRun << 4) | category;

            String huffCode = AC_HUFFMAN_CODES[symbol];
            if (huffCode != null) {
                writer.writeBits(huffCode);
                if (category > 0) {
                    writer.writeBits(getAdditionalBitsValue(value, category), category);
                }
            }
            i++;
        }

        return dcCoeff;
    }

    /**
     * Decode 8x8 block.
     */
    private short decodeBlockHuffman(short[][] matrix, int startRow, int startCol,
                                     BitReader reader, short prevDC) {
        // Decode DC
        int dcCategory = reader.decodeHuffman(DC_DECODE_ROOT);
        short dcDiff = (dcCategory == 0) ? 0 : decodeValue(reader.readBits(dcCategory), dcCategory);
        short dcCoeff = (short)(prevDC + dcDiff);
        matrix[startRow][startCol] = dcCoeff;

        // Decode AC
        int i = 1;
        while (i < 64) {
            int symbol = reader.decodeHuffman(AC_DECODE_ROOT);
            if (symbol == -1 || symbol == 0x00) break; // EOB or error

            if (symbol == 0xF0) { // ZRL
                i = Math.min(i + 16, 64);
                continue;
            }

            int zeroRun = (symbol >> 4) & 0x0F;
            int category = symbol & 0x0F;

            i += zeroRun;
            if (i < 64 && category > 0) {
                int idx = ZIGZAG_INDEX[i] & 0xFF;
                int r = startRow + (idx >> 3);
                int c = startCol + (idx & 7);
                if (r < matrix.length && c < matrix[0].length) {
                    matrix[r][c] = decodeValue(reader.readBits(category), category);
                }
                i++;
            }
        }

        return dcCoeff;
    }

    private void encodeDC(short dcDiff, BitWriter writer) {
        int category = getCategoryFast(dcDiff);
        writer.writeBits(DC_HUFFMAN_CODES[category]);
        if (category > 0) {
            writer.writeBits(getAdditionalBitsValue(dcDiff, category), category);
        }
    }

    /**
     * Get category (number of bits needed) for a value.
     *
     * @param value the value to get category for
     * @return category number
     */
    private int getCategoryFast(short value) {
        int index = value + 2048;
        if (index >= 0 && index < 4096) {
            return CATEGORY_LOOKUP[index];
        }
        // Fallback for out-of-range values
        int absValue = Math.abs(value);
        return (absValue == 0) ? 0 : (32 - Integer.numberOfLeadingZeros(absValue));
    }

    private int getAdditionalBitsValue(short value, int category) {
        return (value >= 0) ? value : (value + (1 << category) - 1);
    }

    /**
     * Decode value from additional bits.
     *
     * @param bits the bits to decode
     * @param category the category of the value
     * @return decoded value
     */
    private short decodeValue(int bits, int category) {
        int threshold = 1 << (category - 1);
        return (short)((bits < threshold) ? (bits - (1 << category) + 1) : bits);
    }
}