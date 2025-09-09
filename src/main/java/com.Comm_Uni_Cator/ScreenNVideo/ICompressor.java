package com.Comm_Uni_Cator.ScreenNVideo;

public interface ICompressor {
    /**
     * Compresses raw tile pixel data and returns byte array
     * @param img->pixel data (int[w][h][3]); (x, y)->location of the first corner; (w, h)->height and width
     * @return string
     */
    String compress(int[][][] img, int x, int y, int w, int h);
}
