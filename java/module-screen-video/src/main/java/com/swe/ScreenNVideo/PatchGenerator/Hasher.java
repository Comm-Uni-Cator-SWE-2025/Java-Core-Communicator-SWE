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
    public long hash(final int[][] img, int x, int y, int w, int h) {
        long hash = 0;
        for (int i = 0; i < w; i += stride) {
            for (int j = 0; j < h; j += stride) {
                int pixel = img[y + j][x + i];
                int r = (pixel>>16) & 0xff;
                int g = (pixel>>8) & 0xff;
                int b = pixel & 0xff;
                hash += r;
                hash += (long) g << 20;
                hash += (long) b << 40;
            }
        }
        return hash;
    }
}
