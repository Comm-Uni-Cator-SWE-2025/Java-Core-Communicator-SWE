package com.swe.ScreenNVideo.PatchGenerator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HasherTest {

    /**
     * Creates a simple WxH test image where each pixel is ARGB = 0xAARRGGBB.
     */
    private int[][] createTestImage(int width, int height) {
        int[][] img = new int[height][width];
        int value = 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = (value & 0xFF);
                int g = (value & 0xFF);
                int b = (value & 0xFF);
                img[y][x] = (r << 16) | (g << 8) | b;  // AARRGGBB without alpha
                value++;
            }
        }
        return img;
    }

    @Test
    void testHashNoStrideFullPatch() {
        int[][] img = createTestImage(3, 3);
        Hasher hasher = new Hasher(1);

        long hash = hasher.hash(img, 0, 0, 3, 3);

        // manual calculation for 9 pixels
        long expected = 0;
        for (int v = 1; v <= 9; v++) {
            int r = v, g = v, b = v;
            expected += r;
            expected += ((long) g) << 20;
            expected += ((long) b) << 40;
        }

        assertEquals(expected, hash);
    }

    @Test
    void testHashWithStride2() {
        int[][] img = createTestImage(5, 5);
        Hasher hasher = new Hasher(2);

        long hash = hasher.hash(img, 0, 0, 5, 5);

        // should sample pixels at (0,0), (2,0), (4,0), (0,2), (2,2), (4,2), (0,4), (2,4), (4,4)
        int[] sampleValues = {
                1, 3, 5,
                11, 13, 15,
                21, 23, 25
        };

        long expected = 0;
        for (int v : sampleValues) {
            expected += v;
            expected += ((long) v) << 20;
            expected += ((long) v) << 40;
        }

        assertEquals(expected, hash);
    }

    @Test
    void testStrideLessThanOneDefaultsToOne() {
        Hasher h = new Hasher(0);  // should become stride=1
        int[][] img = createTestImage(2, 2);

        long hash1 = h.hash(img, 0, 0, 2, 2);
        long hash2 = new Hasher(1).hash(img, 0, 0, 2, 2);

        assertEquals(hash2, hash1);
    }

    @Test
    void testHashSubRegion() {
        int[][] img = createTestImage(5, 5);
        Hasher hasher = new Hasher(1);

        // Subregion: top-left 2x2 -> values: 1,2,6,7
        long hash = hasher.hash(img, 0, 0, 2, 2);

        int[] vals = {1, 2, 6, 7};
        long expected = 0;
        for (int v : vals) {
            expected += v;
            expected += ((long) v) << 20;
            expected += ((long) v) << 40;
        }

        assertEquals(expected, hash);
    }

    @Test
    void testDeterministicOutput() {
        int[][] img = createTestImage(4, 4);
        Hasher hasher = new Hasher(2);

        long h1 = hasher.hash(img, 0, 0, 4, 4);
        long h2 = hasher.hash(img, 0, 0, 4, 4);

        assertEquals(h1, h2);
    }
}
