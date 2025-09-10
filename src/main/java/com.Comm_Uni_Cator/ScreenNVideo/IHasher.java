package com.Comm_Uni_Cator.ScreenNVideo;

public interface IHasher {
    /**
     * Computes a hash for an image patch/tile
     * @param img->pixel data (int[][][]); (x, y)->location of the first corner; (w, h)->height and width
     * @return hash as a long
     */
    long hash(int[][][] img, int x, int y, int w, int h);
}
