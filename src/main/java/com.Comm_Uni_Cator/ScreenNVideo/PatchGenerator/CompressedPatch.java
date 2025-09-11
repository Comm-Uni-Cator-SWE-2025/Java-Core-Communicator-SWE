package com.Comm_Uni_Cator.ScreenNVideo.PatchGenerator;

public class CompressedPatch {
    private int x;
    private int y;
    private int width;
    private int height;
    private String data; // compressed tile as a string

    public CompressedPatch(int x, int y, int width, int height, String data) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    // getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getData() { return data; }
}
