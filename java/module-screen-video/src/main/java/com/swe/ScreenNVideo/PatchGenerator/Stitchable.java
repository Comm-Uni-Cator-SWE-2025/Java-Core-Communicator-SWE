package com.swe.ScreenNVideo.PatchGenerator;

// Interface
public interface Stitchable {
    void applyOn(int[][] canvas);
    int getHeight();
    int getWidth();
    int getX();
    int getY();
}