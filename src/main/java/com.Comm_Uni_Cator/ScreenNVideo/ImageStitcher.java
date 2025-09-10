package com.Comm_Uni_Cator.ScreenNVideo;

import java.util.List;

// Interface
interface Stichable {
    void applyOn(int[][][] canvas);
}


class Patch implements Stichable {
    private int[][][] pixels;
    private int x;
    private int y;

    public Patch(int[][][] pixels, int x, int y) {
        this.pixels = pixels;
        this.x = x;
        this.y = y;
    }

    @Override
    public void applyOn(int[][][] canvas) {
        for(int i = 0; i < pixels.length; i++) {
            for(int j = 0; j<pixels[0].length; j++) {
                int targetX = x + i;
                int targetY = y + j;


                if (targetX >= 0 && targetX < canvas.length &&
                    targetY >= 0 && targetY < canvas[0].length) {

                    for (int c = 0; c < pixels[i][j].length; c++) {
                        canvas[targetX][targetY][c] = pixels[i][j][c];
                    }
                }
            }
        }
    }
}


public class ImageStitcher {
    private int[][][] canvas;

    public ImageStitcher(int[][][] canvas) {
        this.canvas = canvas;
    }

    public void stitch(List<Stichable> patches) {
        for (Stichable patch : patches) {
            patch.applyOn(canvas);
        }
    }

    public int[][][] getCanvas() {
        return canvas;
    }
}
