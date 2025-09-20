package com.swe.ScreenNVideo.PatchGenerator;

public class Patch implements Stitchable {
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

                    System.arraycopy(pixels[i][j], 0, canvas[targetX][targetY], 0, pixels[i][j].length);
                }
            }
        }
    }
}
