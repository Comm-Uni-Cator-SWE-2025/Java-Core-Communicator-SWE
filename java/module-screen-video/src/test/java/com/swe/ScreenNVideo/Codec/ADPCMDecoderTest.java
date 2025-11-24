package com.swe.ScreenNVideo.Codec;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

public class ADPCMDecoderTest {

    /* Utility: little-endian short → byte[2] */
    private static byte[] le(short s) {
        return new byte[] {
                (byte)(s & 0xFF),
                (byte)((s >> 8) & 0xFF)
        };
    }

    /* ----------------------- BASIC TESTS ---------------------------- */

    @Test
    public void testDecodeSimpleZero() {
        ADPCMDecoder dec = new ADPCMDecoder();

        // ADPCM nibble 0 = no change
        byte[] encoded = new byte[] { 0x00 };

        byte[] pcm = dec.decode(encoded);

        // 2 samples → both equal predictor (starts at 0)
        assertEquals(0, pcm[0] | (pcm[1] << 8));
        assertEquals(0, pcm[2] | (pcm[3] << 8));
    }

    @Test
    public void testSignBitNegativeMovement() throws Exception {
        ADPCMDecoder dec = new ADPCMDecoder();

        // With code 0x8 (1000): only sign bit set → subtract step/8
        byte[] encoded = new byte[] { (byte)0x80 }; // high nibble = 8, low nibble = 0

        byte[] pcm = dec.decode(encoded);

        // First sample comes from nibble 8
        int sample = pcm[0] | (pcm[1] << 8);

        // Get STEP_TABLE[0] via reflection
        Field stepTable = ADPCMDecoder.class.getDeclaredField("STEP_TABLE");
        stepTable.setAccessible(true);
        int[] step = (int[]) stepTable.get(null);

        int expected = - (step[0] >> 3);

        assertEquals(expected, sample);
    }

    @Test
    public void testDecodeNibbleF() {
        ADPCMDecoder dec = new ADPCMDecoder();

        byte[] encoded = new byte[]{ (byte)0xF0 };

        byte[] pcm = dec.decode(encoded);

        int sample = pcm[0] | (pcm[1] << 8);

        assertEquals(-11, sample, "nibble F should decode to -11 at initial step size");
    }


    /* ----------------------- INDEX CLAMP TEST ----------------------- */

    @Test
    public void testIndexClamped() throws Exception {
        ADPCMDecoder dec = new ADPCMDecoder();

        byte[] encoded = new byte[2000];
        for (int i = 0; i < 2000; i++) {
            encoded[i] = (byte)(i % 2 == 0 ? 0x7F : 0xFF);
        }

        dec.decode(encoded);

        Field f = ADPCMDecoder.class.getDeclaredField("index");
        f.setAccessible(true);
        int idx = (int) f.get(dec);

        assertTrue(idx >= 0, "index should not go below 0");
        assertTrue(idx <= 88, "index should not exceed MAX_INDEX");
    }

    /* ----------------------- STATE PERSISTENCE ----------------------- */

    @Test
    public void testPredictorPersistsAcrossCalls() {
        ADPCMDecoder dec = new ADPCMDecoder();

        byte[] a = new byte[]{ 0x12 };
        byte[] b = new byte[]{ 0x34 };

        byte[] out1 = dec.decode(a);
        byte[] out2 = dec.decode(b);

        ADPCMDecoder fresh = new ADPCMDecoder();
        byte[] combined = fresh.decode(new byte[]{ 0x12, 0x34 });

        // out1 should match combined[0..3]
        assertEquals(out1[0], combined[0]);
        assertEquals(out1[1], combined[1]);
        assertEquals(out1[2], combined[2]);
        assertEquals(out1[3], combined[3]);

        // out2 should match combined[4..7]
        assertEquals(out2[0], combined[4]);
        assertEquals(out2[1], combined[5]);
        assertEquals(out2[2], combined[6]);
        assertEquals(out2[3], combined[7]);
    }

    /* ---------------------- ROUND TRIP TEST -------------------------- */

    @Test
    public void testAdpcmRealisticAudio() {
        ADPCMEncoder enc = new ADPCMEncoder();
        ADPCMDecoder dec = new ADPCMDecoder();

        int samples = 2000; // 1/8th second at 16kHz
        byte[] pcm = new byte[samples * 2];

        // Generate 440 Hz tone at amplitude 8000
        for (int i = 0; i < samples; i++) {
            double t = i / 16000.0;
            short s = (short) (Math.sin(2 * Math.PI * 440 * t) * 30000);

            pcm[2*i] = (byte)(s & 0xFF);
            pcm[2*i+1] = (byte)((s >> 8) & 0xFF);
        }

        byte[] adpcm = enc.encode(pcm);
        byte[] decPcm = dec.decode(adpcm);

        long errSum = 0;
        long count = 0;

        for (int i = 0; i < samples; i++) {
            int orig = (pcm[2*i] & 0xff) | (pcm[2*i+1] << 8);
            int got  = (decPcm[2*i] & 0xff) | (decPcm[2*i+1] << 8);

            errSum += Math.abs(orig - got);
            count++;
        }

        double avg = errSum / (double)count;

        assertTrue(avg < 1500,
                "Average ADPCM error too high: " + avg);
    }

    @Test
    public void testReset() throws Exception {
        ADPCMDecoder dec = new ADPCMDecoder();

        dec.setState(1234, 55);
        dec.reset();

        Field pred = ADPCMDecoder.class.getDeclaredField("predictor");
        Field idx  = ADPCMDecoder.class.getDeclaredField("index");
        pred.setAccessible(true);
        idx.setAccessible(true);

        assertEquals(0, pred.get(dec));
        assertEquals(0, idx.get(dec));
    }

    @Test
    public void testSetState() throws Exception {
        ADPCMDecoder dec = new ADPCMDecoder();

        dec.setState(5000, 22);

        Field pred = ADPCMDecoder.class.getDeclaredField("predictor");
        Field idx  = ADPCMDecoder.class.getDeclaredField("index");
        pred.setAccessible(true);
        idx.setAccessible(true);

        assertEquals(5000, pred.get(dec));
        assertEquals(22, idx.get(dec));
    }

    @Test
    public void testClampToMaxSample() {
        ADPCMDecoder dec = new ADPCMDecoder();

        // Set predictor near upper bound and index high so step is large
        dec.setState(32000, 88);

        // Code nibble = 0x7 → strong positive increase
        byte[] adpcm = new byte[]{ (byte)0x70 };

        byte[] pcm = dec.decode(adpcm);

        int sample = (pcm[0] & 0xff) | (pcm[1] << 8);

        assertEquals(32767, sample, "Should clamp to MAX_SAMPLE");
    }

    @Test
    public void testClampToMinSample() {
        ADPCMDecoder dec = new ADPCMDecoder();

        dec.setState(-32000, 88); // near bottom with huge step size

        // Code nibble = 0xF → strong negative movement (sign bit + all bits)
        byte[] adpcm = new byte[]{ (byte)0xF0 };

        byte[] pcm = dec.decode(adpcm);

        int sample = (pcm[0] & 0xff) | (pcm[1] << 8);

        assertEquals(-32768, sample, "Should clamp to MIN_SAMPLE");
    }

}
