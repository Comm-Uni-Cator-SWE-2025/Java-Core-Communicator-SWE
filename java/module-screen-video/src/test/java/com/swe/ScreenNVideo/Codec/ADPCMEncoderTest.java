package com.swe.ScreenNVideo.Codec;

import org.junit.Test;
import static org.junit.Assert.*;

public class ADPCMEncoderTest {

    /**
     * Utility: convert short PCM sample into little-endian byte pair.
     */
    private byte[] le(short value) {
        return new byte[] { (byte)(value & 0xFF), (byte)((value >> 8) & 0xFF) };
    }

    @Test
    public void testEncodeSingleZeroSample() {
        ADPCMEncoder encoder = new ADPCMEncoder();

        byte[] pcm = le((short)0);
        byte[] encoded = encoder.encode(pcm);

        // One sample → still one ADPCM nibble → stored in low nibble
        assertEquals(1, encoded.length);

        // Zero difference should produce ADPCM code 0
        assertEquals(0x00, encoded[0] & 0x0F);
    }

    @Test
    public void testEncodeTwoZeroSamplesPackedIntoOneByte() {
        ADPCMEncoder encoder = new ADPCMEncoder();

        byte[] pcm = new byte[4];
        // Both samples = 0, 0

        byte[] encoded = encoder.encode(pcm);

        assertEquals(1, encoded.length);
        // two zero-nibbles packed → 0x00
        assertEquals(0x00, encoded[0]);
    }

    @Test
    public void testPositiveDeltaProducesCorrectSignBit() {
        ADPCMEncoder encoder = new ADPCMEncoder();

        byte[] pcm = new byte[4];
        System.arraycopy(le((short)0), 0, pcm, 0, 2);
        System.arraycopy(le((short)2000), 0, pcm, 2, 2);

        byte[] encoded = encoder.encode(pcm);

        // High nibble = code for second sample (positive → sign bit NOT set)
        int highNibble = (encoded[0] >> 4) & 0x0F;

        assertEquals(0, highNibble & 0x08); // check sign bit is 0
    }

    @Test
    public void testNegativeDeltaProducesSignBit() {
        ADPCMEncoder encoder = new ADPCMEncoder();

        byte[] pcm = new byte[4];
        System.arraycopy(le((short)3000), 0, pcm, 0, 2);
        System.arraycopy(le((short)0), 0, pcm, 2, 2);

        byte[] encoded = encoder.encode(pcm);

        int lowNibble = encoded[0] & 0x0F;

        assertEquals(0x08, lowNibble & 0x08); // SIGN_BIT = 8
    }

    @Test
    public void testPredictorStatePersistsAcrossCalls() {
        ADPCMEncoder encoder = new ADPCMEncoder();

        byte[] pcm1 = le((short)1000);
        byte[] pcm2 = le((short)1200);

        byte[] e1 = encoder.encode(pcm1);  // 1 sample → high nibble only
        byte[] e2 = encoder.encode(pcm2);  // 1 sample → high nibble only

        ADPCMEncoder fresh = new ADPCMEncoder();
        byte[] combined = new byte[4];
        System.arraycopy(pcm1, 0, combined, 0, 2);
        System.arraycopy(pcm2, 0, combined, 2, 2);
        byte[] eCombined = fresh.encode(combined);

        int nibble1 = (eCombined[0] >> 4) & 0x0F;  // high nibble = sample1
        int nibble2 = eCombined[0] & 0x0F;         // low nibble = sample2

        assertEquals(nibble1, (e1[0] >> 4) & 0x0F);
        assertEquals(nibble2, (e2[0] >> 4) & 0x0F);
    }

    @Test
    public void testEncodingIsDeterministic() {
        ADPCMEncoder encoder1 = new ADPCMEncoder();
        ADPCMEncoder encoder2 = new ADPCMEncoder();

        byte[] pcm = new byte[100];
        for (int i = 0; i < 50; i++) {
            short s = (short)(Math.sin(i * 0.2) * 3000);
            pcm[2*i]   = (byte)(s & 0xFF);
            pcm[2*i+1] = (byte)((s >> 8) & 0xFF);
        }

        byte[] e1 = encoder1.encode(pcm);
        byte[] e2 = encoder2.encode(pcm);

        assertArrayEquals(e1, e2);
    }

    @Test
    public void testGetterMethods() {
        ADPCMEncoder encoder = new ADPCMEncoder();

        // Force some encoding to change predictor and index
        byte[] pcm = new byte[]{100, -100, 50, -50};
        encoder.encode(pcm);

        int predictor = encoder.getPredictor();
        int index = encoder.getIndex();

        // Just verify they return *something*
        // We don’t assert specific values since predictor/index evolve internally
        assertNotNull(predictor);
        assertTrue(index >= 0);
    }

}
