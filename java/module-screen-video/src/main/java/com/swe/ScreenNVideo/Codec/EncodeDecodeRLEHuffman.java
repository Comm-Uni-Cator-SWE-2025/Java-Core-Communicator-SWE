/**
 * Contributed by Priyanshu Pandey.
 * Enhanced with Huffman encoding/decoding following JPEG standard.
 */

package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

/**
 * Provides methods for ZigZag scanning, Run-Length Encoding (RLE),
 * and Huffman encoding/decoding for 8x8 short matrices following JPEG standards.
 */
public class EncodeDecodeRLEHuffman implements IRLE {

    /** Singleton instance. */
    public static final EncodeDecodeRLEHuffman ENCDECINSTANCE = new EncodeDecodeRLEHuffman();

    // MAGIC NUMERS

    /** Size of the ZigZag block. */
    private static final int ZIGZAG_BLOCK_SIZE = 64;

    /** Size of the Huffman table. */
    private static final int HUFFMAN_TABLE_SIZE = 256;

    /** Size of the category lookup table. */
    private static final int CATEGORY_LOOKUP_SIZE = 4096;

    /** Range of the category lookup table. */
    private static final int CATEGORY_LOOKUP_RANGE = 2048;

    /** Size of the Huffman decode root. */
    private static final int HUFFMAN_DECODE_ROOT_SIZE = 16;

    /** Size of the byte. */
    private static final int BYTE_SIZE = 8;

    /** HAlf Size of the byte. */
    private static final int HALF_BYTE_SIZE = 4;

    /** Size of word. */
    private static final int WORD_SIZE = 32;

    /** Size of the DC Huffman codes. */
    private static final int DC_HUFFMAN_CODES_SIZE = 12;

    /** Size of the AC Huffman codes. */
    private static final int AC_HUFFMAN_CODES_SIZE = 256;

    /** Max Diagonal Number. */
    private static final int MAX_DIAGONAL_NUMBER = 15;

    /** Half of the diagonal number. */
    private static final int HALF_DIAGONAL_NUMBER = MAX_DIAGONAL_NUMBER / 2;


    /** Zero. */
    private static final int ZERO = 0;
    /** One. */
    private static final int ONE = 1;
    /** Two. */
    private static final int TWO = 2;
    /** Three. */
    private static final int THREE = 3;
    /** Four. */
    private static final int FOUR = 4;
    /** Five. */
    private static final int FIVE = 5;
    /** Six. */
    private static final int SIX = 6;
    /** Seven. */
    private static final int SEVEN = 7;
    /** Eight. */
    private static final int EIGHT = 8;
    /** Nine. */
    private static final int NINE = 9;
    /** Ten. */
    private static final int TEN = 10;
    /** Eleven. */
    private static final int ELEVEN = 11;

    /** ZRL. */
    private static final int ZRL = 0xF0;

    /** ZLE. */
    private static final int ZLE = 0x0F;

    /** Byte mask. */
    private static final int BYTE_MASK = 0xFF;
    //// 


    /** Pre-computed ZigZag indices for 8x8 blocks (row * 8 + col). */
    private static final byte[] ZIGZAG_INDEX = new byte[ZIGZAG_BLOCK_SIZE];

    /** Reverse lookup: zigzag position -> linear index. */
    private static final byte[] REVERSE_ZIGZAG_INDEX = new byte[ZIGZAG_BLOCK_SIZE];

    /** Standard JPEG Huffman tables - using arrays for faster lookup. */
    private static final String[] DC_HUFFMAN_CODES = new String[DC_HUFFMAN_CODES_SIZE];
    /** Standard JPEG Huffman tables - using arrays for faster lookup. */
    private static final String[] AC_HUFFMAN_CODES = new String[AC_HUFFMAN_CODES_SIZE];

    /** Trie-based decode structures for O(code_length) decoding. */
    private static final HuffmanDecodeNode DC_DECODE_ROOT = new HuffmanDecodeNode();
    /** Trie-based decode structures for O(code_length) decoding. */
    private static final HuffmanDecodeNode AC_DECODE_ROOT = new HuffmanDecodeNode();

    /** Pre-computed category lookup table for values -2047 to 2047. */
    private static final byte[] CATEGORY_LOOKUP = new byte[CATEGORY_LOOKUP_SIZE];

    /** Standard JPEG luminance AC table specification. */
    private static final byte[] STD_AC_LUMINANCE_BITS = {
        0x00, 0x02, 0x01, 0x03, 0x03, 0x02, 0x04, 0x03,
        0x05, 0x05, 0x04, 0x04, 0x00, 0x00, 0x01, 0x7d,
    };

    /** Standard JPEG luminance AC table specification. */
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
        0xF9, 0xFA,
    };

    /**
     * Trie node for fast Huffman decoding without string allocations.
     */
    private static class HuffmanDecodeNode {
        /** The one node. */
        private HuffmanDecodeNode one;
        /** The zero node. */
        private HuffmanDecodeNode zero;
        /** The value. */
        private int value = -1; // -1 means non-leaf

        void insert(final String code, final int val) {
            HuffmanDecodeNode curr = this;
            for (int i = 0; i < code.length(); i++) {
                if (code.charAt(i) == '0') {
                    if (curr.zero == null) {
                        curr.zero = new HuffmanDecodeNode();
                    }
                    curr = curr.zero;
                } else {
                    if (curr.one == null) {
                        curr.one = new HuffmanDecodeNode();
                    }
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
        for (int diag = 0; diag < MAX_DIAGONAL_NUMBER; diag++) {
            final int rowStart = Math.max(0, diag - HALF_DIAGONAL_NUMBER);
            final int rowEnd = Math.min(HALF_DIAGONAL_NUMBER, diag);

            if ((diag & 1) == 1) {
                for (int i = rowStart; i <= rowEnd; i++) {
                    final int r = i;
                    final int c = diag - i;
                    ZIGZAG_INDEX[idx] = (byte) (r * BYTE_SIZE + c);
                    REVERSE_ZIGZAG_INDEX[r * BYTE_SIZE + c] = (byte) idx;
                    idx++;
                }
            } else {
                for (int i = rowEnd; i >= rowStart; i--) {
                    final int r = i;
                    final int c = diag - i;
                    ZIGZAG_INDEX[idx] = (byte) (r * BYTE_SIZE + c);
                    REVERSE_ZIGZAG_INDEX[r * BYTE_SIZE + c] = (byte) idx;
                    idx++;
                }
            }
        }
    }

    /**
     * Pre-compute category lookup for fast category determination.
     */
    private static void initializeCategoryLookup() {
        for (int i = 0; i < CATEGORY_LOOKUP_SIZE; i++) {
            final int value = i - CATEGORY_LOOKUP_RANGE; // Range: -2048 to 2047
            final int absValue = Math.abs(value);
            if (absValue == 0) {
                CATEGORY_LOOKUP[i] = 0;
            } else {
                CATEGORY_LOOKUP[i] = (byte) (WORD_SIZE - Integer.numberOfLeadingZeros(absValue));
            }
        }
    }

    /**
     * Initialize standard JPEG Huffman tables.
     */
    private static void initializeHuffmanTables() {
        // DC Luminance Huffman codes
        DC_HUFFMAN_CODES[ZERO] = "00";
        DC_HUFFMAN_CODES[ONE] = "010";
        DC_HUFFMAN_CODES[TWO] = "011";
        DC_HUFFMAN_CODES[THREE] = "100";
        DC_HUFFMAN_CODES[FOUR] = "101";
        DC_HUFFMAN_CODES[FIVE] = "110";
        DC_HUFFMAN_CODES[SIX] = "1110";
        DC_HUFFMAN_CODES[SEVEN] = "11110";
        DC_HUFFMAN_CODES[EIGHT] = "111110";
        DC_HUFFMAN_CODES[NINE] = "1111110";
        DC_HUFFMAN_CODES[TEN] = "11111110";
        DC_HUFFMAN_CODES[ELEVEN] = "111111110";

        buildAcHuffmanTable();
        buildDecodeTries();
    }

    /**
     * Build the AC Huffman table.
     */
    private static void buildAcHuffmanTable() {
        int code = 0;
        int valueIndex = 0;

        for (int bitLength = 1; bitLength <= HUFFMAN_DECODE_ROOT_SIZE; bitLength++) {
            final int codesForLength = STD_AC_LUMINANCE_BITS[bitLength - 1];

            for (int i = 0; i < codesForLength; i++) {
                final int symbol = STD_AC_LUMINANCE_VALUES[valueIndex++];
                AC_HUFFMAN_CODES[symbol] = toBinaryString(code, bitLength);
                code++;
            }
            code <<= 1;
        }
    }

    /**
     * Build the decode tries.
     */
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
     * @param val The value to convert.
     * @param length The length of the binary string.
     * @return the binary string
     */
    private static String toBinaryString(final int val, final int length) {
        int value = val;
        final char[] chars = new char[length];
        for (int i = length - 1; i >= 0; i--) {
            chars[i] = (char) ('0' + (value & 1));
            value >>= 1;
        }
        return new String(chars);
    }

    /**
     * Bit writer with minimal branching.
     */
    private static class BitWriter {
        /** The buffer. */
        private final ByteBuffer buffer;
        /** The current byte. */
        private int currentByte;
        /** The bits in byte. */
        private int bitsInByte;

        /**
         * Constructor for the BitWriter class.
         * @param bufferArgs The buffer.
         */
        BitWriter(final ByteBuffer bufferArgs) {
            this.buffer = bufferArgs;
            this.currentByte = 0;
            this.bitsInByte = 0;
        }

        /**
         * Write the bits.
         * @param bits The bits to write.
         */
        public void writeBits(final String bits) {
            for (int i = 0, len = bits.length(); i < len; i++) {
                currentByte = (currentByte << 1) | (bits.charAt(i) - '0');
                if (++bitsInByte == BYTE_SIZE) {
                    buffer.put((byte) currentByte);
                    currentByte = 0;
                    bitsInByte = 0;
                }
            }
        }

        /**
         * Write the bits.
         * @param value The value to write.
         * @param nBits The number of bits to write.
         */
        public void writeBits(final int value, final int nBits) {
            int numBits = nBits;
            // Process bits in chunks for better performance
            while (numBits > 0) {
                final int bitsToWrite = Math.min(numBits, BYTE_SIZE - bitsInByte);
                final int shift = numBits - bitsToWrite;
                final int mask = (1 << bitsToWrite) - 1;
                final int bits = (value >> shift) & mask;

                currentByte = (currentByte << bitsToWrite) | bits;
                bitsInByte += bitsToWrite;
                numBits -= bitsToWrite;

                if (bitsInByte == BYTE_SIZE) {
                    buffer.put((byte) currentByte);
                    currentByte = 0;
                    bitsInByte = 0;
                }
            }
        }

        /**
         * Flush the bits.
         */
        public void flush() {
            if (bitsInByte > 0) {
                currentByte <<= BYTE_SIZE - bitsInByte;
                currentByte |= (1 << (BYTE_SIZE - bitsInByte)) - 1; // Pad with 1s
                buffer.put((byte) currentByte);
                currentByte = 0;
                bitsInByte = 0;
            }
        }
    }

    /**
     * Bit reader with trie-based decoding.
     */
    private static class BitReader {
        /** The buffer. */
        private final ByteBuffer buffer;
        /** The current byte. */
        private int currentByte;
        /** The bits remaining. */
        private int bitsRemaining;
        /** The max position. */
        private final int maxPosition;

        /**
         * Constructor for the BitReader class.
         * @param bufferArgs The buffer.
         * @param bitStreamLength The length of the bit stream.
         */
        BitReader(final ByteBuffer bufferArgs, final int bitStreamLength) {
            this.buffer = bufferArgs;
            this.currentByte = 0;
            this.bitsRemaining = 0;
            this.maxPosition = buffer.position() + bitStreamLength;
        }

        /**
         * Read the bit.
         * @return the bit
         */
        private int readBit() {
            if (bitsRemaining == 0) {
                if (!buffer.hasRemaining() || buffer.position() >= maxPosition) {
                    return -1;
                }
                currentByte = buffer.get() & BYTE_MASK;
                bitsRemaining = BYTE_SIZE;
            }
            return (currentByte >> --bitsRemaining) & 1;
        }

        /**
         * Read the bits.
         * @param nBits The number of bits to read.
         * @return the bits
         */
        public int readBits(final int nBits) {
            int numBits = nBits;
            int value = 0;
            while (numBits > 0) {
                if (bitsRemaining == 0) {
                    if (!buffer.hasRemaining() || buffer.position() >= maxPosition) {
                        return -1;
                    }
                    currentByte = buffer.get() & BYTE_MASK;
                    bitsRemaining = BYTE_SIZE;
                }

                final int bitsToRead = Math.min(numBits, bitsRemaining);
                final int shift = bitsRemaining - bitsToRead;
                final int mask = (1 << bitsToRead) - 1;
                value = (value << bitsToRead) | ((currentByte >> shift) & mask);
                bitsRemaining -= bitsToRead;
                numBits -= bitsToRead;
            }
            return value;
        }

        /**
         * Decode the Huffman.
         * @param root The root.
         * @return the decoded Huffman
         */
        public int decodeHuffman(final HuffmanDecodeNode root) {
            HuffmanDecodeNode curr = root;
            for (int i = 0; i < HUFFMAN_DECODE_ROOT_SIZE; i++) { // Max code length
                final int bit = readBit();
                if (bit == -1) {
                    return -1;
                }

                if (bit == 0) {
                    curr = curr.zero;
                } else {
                    curr = curr.one;
                }
                if (curr == null) {
                    return -1;
                }
                if (curr.value != -1) {
                    return curr.value;
                }
            }
            return -1;
        }

        /**
         * Align to the next byte.
         */
        public void alignToNextByte() {
            buffer.position(maxPosition);
        }
    }

    /**
     * Get the instance.
     * @return the instance
     */
    public static EncodeDecodeRLEHuffman getInstance() {
        return ENCDECINSTANCE;
    }

    /**
     * Zigzag RLE.
     * @param matrix The matrix.
     * @param resRLEbuffer The result buffer.
     */
    @Override
    public void zigZagRLE(final short[][] matrix, final ByteBuffer resRLEbuffer) {
        final int height = matrix.length;
        final int width = matrix[0].length;

        resRLEbuffer.putShort((short) height);
        resRLEbuffer.putShort((short) width);

        final int bitStreamStart = resRLEbuffer.position();
        resRLEbuffer.putInt(0); // Placeholder

        final BitWriter writer = new BitWriter(resRLEbuffer);
        short prevDC = (short) ZERO;

        // Process blocks
        for (int rowBlock = 0; rowBlock < height; rowBlock += BYTE_SIZE) {
            for (int colBlock = 0; colBlock < width; colBlock += BYTE_SIZE) {
                prevDC = encodeBlockHuffman(matrix, rowBlock, colBlock, writer, prevDC);
            }
        }

        writer.flush();

        final int bitStreamEnd = resRLEbuffer.position();
        resRLEbuffer.putInt(bitStreamStart, bitStreamEnd - bitStreamStart - HALF_BYTE_SIZE);
    }

    @Override
    public short[][] revZigZagRLE(final ByteBuffer resRLEbuffer) {
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

        final BitReader reader = new BitReader(resRLEbuffer, bitStreamLength);
        short prevDC = (short) ZERO;

        for (int rowBlock = 0; rowBlock < height; rowBlock += BYTE_SIZE) {
            for (int colBlock = 0; colBlock < width; colBlock += BYTE_SIZE) {
                prevDC = decodeBlockHuffman(matrix, rowBlock, colBlock, reader, prevDC);
            }
        }

        reader.alignToNextByte();
        return matrix;
    }

    /**
     * Encodes an 8x8 block using ZigZag, RLE, and Huffman encoding (JPEG standard).
     * @param matrix The matrix.
     * @param startRow The start row.
     * @param startCol The start column.
     * @param writer The writer.
     * @param prevDC The previous DC.
     * @return the encoded block
     */
    private short encodeBlockHuffman(final short[][] matrix, final int startRow, final int startCol,
                                     final BitWriter writer, final short prevDC) {
        // Extract in zigzag order using pre-computed indices
        final short dcCoeff = matrix[startRow][startCol];
        encodeDC((short) (dcCoeff - prevDC), writer);

        // Encode AC coefficients
        int i = ONE;
        while (i < ZIGZAG_BLOCK_SIZE) {
            int zeroRun = 0;
            while (i < ZIGZAG_BLOCK_SIZE) {
                final int idx = ZIGZAG_INDEX[i] & BYTE_MASK;
                final int r = startRow + (idx >> THREE);
                final int c = startCol + (idx & HALF_DIAGONAL_NUMBER);
                if (r < matrix.length && c < matrix[0].length && matrix[r][c] == 0) {
                    zeroRun++;
                    i++;
                } else {
                    break;
                }
            }

            if (i == ZIGZAG_BLOCK_SIZE) {
                writer.writeBits(AC_HUFFMAN_CODES[ZERO]); // EOB
                break;
            }

            while (zeroRun >= HUFFMAN_DECODE_ROOT_SIZE) {
                writer.writeBits(AC_HUFFMAN_CODES[ZRL]); // ZRL
                zeroRun -= HUFFMAN_DECODE_ROOT_SIZE;
            }

            final int idx = ZIGZAG_INDEX[i] & BYTE_MASK;
            final int r = startRow + (idx >> THREE);
            final int c = startCol + (idx & HALF_DIAGONAL_NUMBER);
            final short value;
            if (r < matrix.length && c < matrix[0].length)  {
                value = matrix[r][c];
            } else {
                value = 0;
            }

            final int category = getCategoryFast(value);
            final int symbol = (zeroRun << HALF_BYTE_SIZE) | category;

            final String huffCode = AC_HUFFMAN_CODES[symbol];
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
     * @param matrix The matrix.
     * @param startRow The start row.
     * @param startCol The start column.
     * @param reader The reader.
     * @param prevDC The previous DC.
     * @return the decoded block
     */
    private short decodeBlockHuffman(final short[][] matrix, final int startRow, final int startCol,
                                     final BitReader reader, final short prevDC) {
        // Decode DC
        final int dcCategory = reader.decodeHuffman(DC_DECODE_ROOT);
        final short dcDiff;
        if (dcCategory == 0) {
            dcDiff = 0;
        } else {
            dcDiff = decodeValue(reader.readBits(dcCategory), dcCategory);
        }
        final short dcCoeff = (short) (prevDC + dcDiff);
        matrix[startRow][startCol] = dcCoeff;

        // Decode AC
        int i = 1;
        while (i < ZIGZAG_BLOCK_SIZE) {
            final int symbol = reader.decodeHuffman(AC_DECODE_ROOT);
            if (symbol == -1 || symbol == 0x00) {
                break; // EOB or error
            }

            if (symbol == ZRL) { // ZRL
                i = Math.min(i + HUFFMAN_DECODE_ROOT_SIZE, ZIGZAG_BLOCK_SIZE);
                continue;
            }

            final int zeroRun = (symbol >> HALF_BYTE_SIZE) & ZLE;
            final int category = symbol & ZLE;

            i += zeroRun;
            if (i < ZIGZAG_BLOCK_SIZE && category > 0) {
                final int idx = ZIGZAG_INDEX[i] & BYTE_MASK;
                final int r = startRow + (idx >> THREE);
                final int c = startCol + (idx & HALF_DIAGONAL_NUMBER);
                if (r < matrix.length && c < matrix[0].length) {
                    matrix[r][c] = decodeValue(reader.readBits(category), category);
                }
                i++;
            }
        }

        return dcCoeff;
    }

    /**
     * Encode the DC.
     * @param dcDiff The DC difference.
     * @param writer The writer.
     */
    private void encodeDC(final short dcDiff, final BitWriter writer) {
        final int category = getCategoryFast(dcDiff);
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
    private int getCategoryFast(final short value) {
        final int index = value + CATEGORY_LOOKUP_RANGE;
        if (index >= 0 && index < CATEGORY_LOOKUP_SIZE) {
            return CATEGORY_LOOKUP[index];
        }
        // Fallback for out-of-range values
        final int absValue = Math.abs(value);
        if (absValue == 0) {
            return 0;
        }
        return WORD_SIZE - Integer.numberOfLeadingZeros(absValue);
    }

    /**
     * Get the additional bits value.
     * @param value The value.
     * @param category The category.
     * @return the additional bits value
     */
    private int getAdditionalBitsValue(final short value, final int category) {
        if (value >= 0) {
            return value;
        }
        return value + (1 << category) - 1;
    }

    /**
     * Decode value from additional bits.
     *
     * @param bits the bits to decode
     * @param category the category of the value
     * @return decoded value
     */
    private short decodeValue(final int bits, final int category) {
        final int threshold = 1 << (category - 1);
        if (bits < threshold) {
            return (short) (bits - (1 << category) + 1);
        }
        return (short) bits;
    }
}