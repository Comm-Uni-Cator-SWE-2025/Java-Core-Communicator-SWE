package com.swe.ScreenNVideo.PatchGenerator;

import java.util.List;


public class ImageStitcher {
    private final int[][][] canvas;

    public ImageStitcher(int[][][] canvas) {
        this.canvas = canvas;
    }

    public void stitch(Stitchable patch) {
        patch.applyOn(canvas);
    }

    public int[][][] getCanvas() {
        return canvas;
    }
}
