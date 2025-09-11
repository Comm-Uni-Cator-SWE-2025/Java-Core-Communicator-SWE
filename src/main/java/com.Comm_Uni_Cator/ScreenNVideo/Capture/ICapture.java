package com.Comm_Uni_Cator.ScreenNVideo.Capture;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class ICapture {
    public abstract BufferedImage capture() throws AWTException;


    /**
     * Captures the screen and returns a 3D RGB matrix [height][width][3].
     * Each pixel has {R, G, B} values (0–255).
     *
     * @return int[][][] RGB matrix of the screenshot
     * @throws AWTException if screen capture is not supported
     */
    public int[][][] captureAsRGBMatrix() throws AWTException {
        BufferedImage image = capture();
        int width = image.getWidth();
        int height = image.getHeight();

        int[][][] rgbMatrix = new int[height][width][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                rgbMatrix[y][x][0] = (pixel >> 16) & 0xFF; // Red
                rgbMatrix[y][x][1] = (pixel >> 8) & 0xFF;  // Green
                rgbMatrix[y][x][2] = pixel & 0xFF;         // Blue
            }
        }

        return rgbMatrix;
    }
}
