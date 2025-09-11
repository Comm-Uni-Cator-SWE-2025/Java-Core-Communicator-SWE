package com.swe.ScreenNVideo.PatchGenerator;

public class Hasher implements IHasher {
    private final int stride;

    /**
     * @param stride sampling step (e.g. 2 = every 2nd pixel, 4 = every 4th pixel)
     */
    public Hasher(int stride) {
        this.stride = Math.max(1, stride); // avoid zero or negative
    }

    @Override
    public long hash(int[][][] img, int x, int y, int w, int h) {
        long hash = 0;
        for (int i = 0; i < w; i += stride) {
            for (int j = 0; j < h; j += stride) {
                int[] pixel = img[x + i][y + j];
                hash += pixel[0];
                hash += ((long) pixel[1] << 20);
                hash += ((long) pixel[2] << 40);
            }
        }
        return hash;
    }
}
