/**
 * Contributed by @aman112201041.
 */

package com.swe.ScreenNVideo.Codec;

/**
 * ADPCMEncoder encodes 16-bit PCM audio samples into
 * 4-bit IMA ADPCM compressed format.
 * It maintains internal state (predictor and index) to
 * allow continuous encoding of streaming PCM data.
 */
public class ADPCMEncoder {

    /** Sign bit used in a 4-bit ADPCM code to indicate negative values. */
    private static final int SIGN_BIT = 8;

    /** Most significant delta bit in a 4-bit ADPCM code. */
    private static final int DELTA_BIT_2 = 4;

    /** Middle delta bit in a 4-bit ADPCM code. */
    private static final int DELTA_BIT_1 = 2;

    /** Least significant delta bit in a 4-bit ADPCM code. */
    private static final int DELTA_BIT_0 = 1;

    /** Minimum possible PCM sample value (16-bit signed). */
    private static final int MIN_PCM = -32768;

    /** Maximum possible PCM sample value (16-bit signed). */
    private static final int MAX_PCM = 32767;

    /** Right shift value used for base contribution in ADPCM predictor calculation. */
    private static final int SHIFT_BASE = 3;

    /** Right shift value used for mid contribution in ADPCM predictor calculation. */
    private static final int SHIFT_MID = 1;

    /** Right shift value used for low contribution in ADPCM predictor calculation. */
    private static final int SHIFT_LOW = 2;

    /** Maximum index into the ADPCM step size table. */
    private static final int MAX_STEP_INDEX = 88;

    /** Mask for the low nibble (4 bits) of a byte. */
    private static final int LOW_NIBBLE_MASK = 0x0F;

    /** Number of bits in an ADPCM nibble (half a byte). */
    private static final int NIBBLE_BITS = 4;

    /**
     * IMA ADPCM step size table.
     * Each entry corresponds to a quantization step for a given index.
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
     * IMA ADPCM index adjustment table.
     * Used to update the step size index after decoding each 4-bit nibble.
     */
    private static final int[] INDEX_TABLE = {
        -1, -1, -1, -1, 2, 4, 6, 8,
        -1, -1, -1, -1, 2, 4, 6, 8,
    };

    /** Last predicted PCM sample. Maintains state between decode/encode calls. */
    private int predictor = 0;

    /** Current index into the STEP_TABLE. Maintains state between decode/encode calls. */
    private int index = 0;

    /**
     * Encodes 16-bit PCM bytes into 4-bit ADPCM data.
     * Each pair of ADPCM codes is packed into one byte.
     * @param pcmBytes the input PCM audio samples (16-bit, little endian)
     * @return a byte array containing the ADPCM-encoded data
     */
    public byte[] encode(final byte[] pcmBytes) {
        final int numSamples = pcmBytes.length / 2;
        final byte[] adpcmBytes = new byte[(numSamples + 1) / 2];

        int step = STEP_TABLE[index];
        int bufferIndex = 0;
        boolean highNibble = true;
        byte currentByte = 0;

        for (int i = 0; i < numSamples; i++) {
            final int sample = bytesToSample(pcmBytes, i);
            final int code = encodeSample(sample, step);
            step = STEP_TABLE[index]; // index updated in encodeSample()

            // Pack 2 nibbles per byte
            if (highNibble) {
                currentByte = (byte) (code << NIBBLE_BITS);
                highNibble = false;
            } else {
                currentByte |= (byte) (code & LOW_NIBBLE_MASK);
                adpcmBytes[bufferIndex++] = currentByte;
                highNibble = true;
            }
        }

        if (!highNibble) {
            adpcmBytes[bufferIndex] = currentByte;
        }

        return adpcmBytes;
    }

    /**
     * Converts two PCM bytes to a 16-bit signed sample.
     *
     * @param pcmBytes the input PCM byte array (little-endian, 16-bit samples)
     * @param sampleIndex the sample index in the PCM array
     * @return the decoded 16-bit signed PCM sample
     */
    private int bytesToSample(final byte[] pcmBytes, final int sampleIndex) {
        final int lo = pcmBytes[2 * sampleIndex] & 0xFF;
        final int hi = pcmBytes[2 * sampleIndex + 1] << 8;
        return (short) (hi | lo);
    }

    /**
     * Encodes a single PCM sample into a 4-bit ADPCM code.
     *
     * @param sample the 16-bit PCM sample to encode
     * @param step the current step size from the STEP_TABLE
     * @return the 4-bit ADPCM code representing the sample
     */
    private int encodeSample(final int sample, final int step) {
        int diff = sample - predictor;

        final int sign;
        if (diff < 0) {
            sign = SIGN_BIT;
            diff = -diff;
        } else {
            sign = 0;
        }

        final int delta = computeDelta(diff, step);
        final int code = delta | sign;

        predictor = updatePredictor(delta, sign, step);
        index = clamp(index + INDEX_TABLE[code], 0, MAX_STEP_INDEX);

        return code;
    }

    /**
     * Computes the 3-bit delta for ADPCM quantization.
     *
     * @param diff the absolute difference between sample and predictor
     * @param step the current step size
     * @return the 3-bit delta value
     */
    private int computeDelta(final int diff, final int step) {
        int delta = 0;
        int remainingDiff = diff; // local mutable copy
        int currentStep = step;   // local mutable copy

        if (remainingDiff >= currentStep) {
            delta = DELTA_BIT_2;
            remainingDiff -= currentStep;
        }
        currentStep >>= 1;
        if (remainingDiff >= currentStep) {
            delta |= DELTA_BIT_1;
            remainingDiff -= currentStep;
        }
        currentStep >>= 1;
        if (remainingDiff >= currentStep) {
            delta |= DELTA_BIT_0;
        }
        return delta;
    }

    /**
     * Updates the predictor based on delta and sign.
     *
     * @param delta the 3-bit delta value
     * @param sign the sign bit (8 if negative, 0 if positive)
     * @param step the current step size
     * @return the new predictor value (clamped to -32768..32767)
     */
    private int updatePredictor(final int delta, final int sign, final int step) {
        int diffq = step >> SHIFT_BASE;
        if ((delta & DELTA_BIT_2) != 0) {
            diffq += step;
        }
        if ((delta & DELTA_BIT_1) != 0) {
            diffq += step >> SHIFT_MID;
        }
        if ((delta & DELTA_BIT_0) != 0) {
            diffq += step >> SHIFT_LOW;
        }

        int newPred = predictor;
        if (sign != 0) {
            newPred -= diffq;
        } else {
            newPred += diffq;
        }
        return clamp(newPred, MIN_PCM, MAX_PCM);
    }

    /**
     * Clamps a value to the specified range.
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     */
    private int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    public int getPredictor() {
        return this.predictor;
    }

    public int getIndex() {
        return this.index;
    }
}
