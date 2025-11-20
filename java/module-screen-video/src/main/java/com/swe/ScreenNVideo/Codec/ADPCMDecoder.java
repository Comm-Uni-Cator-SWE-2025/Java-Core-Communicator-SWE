/**
 * Contributed by @aman112201041
 */

package com.swe.ScreenNVideo.Codec;

/**
 * ADPCMDecoder decodes IMA ADPCM compressed audio data into
 * 16-bit PCM samples (little-endian format).
 * It maintains internal state (predictor and index) between calls
 * to allow continuous decoding of streaming ADPCM data.
 */
public class ADPCMDecoder {

    /** Bit mask for extracting a byte (0xFF). */
    private static final int BYTE_MASK = 0xFF;

    /** Number of bits in a nibble. */
    private static final int NIBBLE_BITS = 4;

    /** Size of a Byte. */
    private static final int BYTE_SIZE = 8;

    /** Number of bits to shift when dividing step by 8. */
    private static final int STEP_DIVISOR_SHIFT = 3;

    /** Bit mask for sign bit (ADPCM sign flag). */
    private static final int SIGN_MASK = 0b1000;

    /** Bit mask for the 4th bit of the ADPCM code. */
    private static final int BIT_3_MASK = 0b0100;

    /** Bit mask for the 3rd bit of the ADPCM code. */
    private static final int BIT_2_MASK = 0b0010;

    /** Bit mask for the 2nd bit of the ADPCM code. */
    private static final int BIT_1_MASK = 0b0001;

    /** Max index value for the step table. */
    private static final int MAX_INDEX = 88;

    /** Max sample amplitude for 16-bit PCM. */
    private static final int MAX_SAMPLE = 32767;

    /** Min sample amplitude for 16-bit PCM. */
    private static final int MIN_SAMPLE = -32768;

    /**
     * Step size lookup table for IMA ADPCM decoding.
     * Each entry defines the quantization step used for a given index.
     */
    private static final int[] STEP_TABLE = {
        7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
        19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
        50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
        130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
        337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
        876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
        2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
        5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
        15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767,
    };

    /**
     * Index adjustment table for IMA ADPCM decoding.
     * Used to update the step size index after decoding each 4-bit nibble.
     */
    private static final int[] INDEX_TABLE = {
        -1, -1, -1, -1, 2, 4, 6, 8,
        -1, -1, -1, -1, 2, 4, 6, 8,
    };

    /**
     * Most recent predicted PCM sample value.
     */
    private int predictor = 0;

    /**
     * Current step index into {@link #STEP_TABLE}.
     */
    private int index = 0;

    /**
     * Decodes 4-bit IMA ADPCM data into 16-bit PCM bytes (little endian).
     * @param adpcmBytes the input ADPCM-encoded byte array
     * @return a byte array containing the decoded 16-bit PCM samples
     */
    public byte[] decode(final byte[] adpcmBytes) {
        final int numSamples = adpcmBytes.length * 2; // each byte holds 2 samples
        final byte[] pcmBytes = new byte[numSamples * 2]; // 2 bytes per sample

        int pcmIndex = 0;

        for (byte adpcmByte : adpcmBytes) {
            final int highNibble = (adpcmByte >> NIBBLE_BITS) & 0x0F;
            final int lowNibble = adpcmByte & 0x0F;

            predictor = decodeNibble(highNibble, predictor);
            pcmBytes[pcmIndex++] = (byte) (predictor & BYTE_MASK);
            pcmBytes[pcmIndex++] = (byte) ((predictor >> BYTE_SIZE) & BYTE_MASK);

            predictor = decodeNibble(lowNibble, predictor);
            pcmBytes[pcmIndex++] = (byte) (predictor & BYTE_MASK);
            pcmBytes[pcmIndex++] = (byte) ((predictor >> BYTE_SIZE) & BYTE_MASK);
        }

        return pcmBytes;
    }

    /**
     * Decodes a single 4-bit ADPCM nibble into a new PCM sample value.
     *
     * @param code the 4-bit ADPCM code
     * @param prevSample the previous PCM sample
     * @return the decoded PCM sample
     */
    private int decodeNibble(final int code, final int prevSample) {
        final int step = STEP_TABLE[index];

        // Compute difference (diffq)
        int diffq = step >> STEP_DIVISOR_SHIFT;
        if ((code & BIT_3_MASK) != 0) {
            diffq += step;
        }
        if ((code & BIT_2_MASK) != 0) {
            diffq += step >> 1;
        }
        if ((code & BIT_1_MASK) != 0) {
            diffq += step >> 2;
        }

        int sample = prevSample;
        if ((code & SIGN_MASK) != 0) {
            sample -= diffq;
        } else {
            sample += diffq;
        }

        // Clamp
        if (sample > MAX_SAMPLE) {
            sample = MAX_SAMPLE;
        } else if (sample < MIN_SAMPLE) {
            sample = MIN_SAMPLE;
        }

        // Update index
        index += INDEX_TABLE[code];
        if (index < 0) {
            index = 0;
        } else if (index > MAX_INDEX) {
            index = MAX_INDEX;
        }

        return sample;
    }

    public void reset() {
        predictor = 0;
        index = 0;
    }

    public void setState(int predictor, int index) {
        this.predictor = predictor;
        this.index = index;
    }

}
