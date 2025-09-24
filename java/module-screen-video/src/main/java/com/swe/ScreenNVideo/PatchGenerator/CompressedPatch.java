package com.swe.ScreenNVideo.PatchGenerator;

/**
 * Represents a compressed patch of an image or frame.
 * Contains coordinates (x, y), dimensions, and the compressed tile data.
 */
public class CompressedPatch {
    /** X coordinate of the patch. */
    private int x;

    /** Y coordinate of the patch. */
    private int y;

    /** Width of the patch. */
    private int width;

    /** Height of the patch. */
    private int height;

    /** Compressed tile data as a string. */
    private String data;

    public CompressedPatch(final int xArg,
                           final int yArg,
                           final int widthArg,
                           final int heightArg,
                           final String dataArg) {
        this.x = xArg;
        this.y = yArg;
        this.width = widthArg;
        this.height = heightArg;
        this.data = dataArg;
    }

    // getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getData() {
        return data;
    }
}
