package com.swe.ScreenNVideo.Capture;

import org.junit.jupiter.api.Test;
import java.awt.AWTException;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the ICapture abstract class.
 * Verifies the default RGB matrix conversion logic.
 */
class ICaptureTest {

    @Test
    void testCaptureAsRGBMatrix() throws AWTException {
        // Create a 2x2 test image with specific colors
        final int width = 2;
        final int height = 2;
        final BufferedImage testImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        testImage.setRGB(0, 0, 0xFFFF0000); // Red
        testImage.setRGB(1, 0, 0xFF00FF00); // Green
        testImage.setRGB(0, 1, 0xFF0000FF); // Blue
        testImage.setRGB(1, 1, 0xFFFFFFFF); // White

        // Create a simple anonymous subclass for testing
        final ICapture captureImpl = new ICapture() {
            @Override
            public BufferedImage capture() {
                return testImage;
            }

            @Override
            public void reInit() {
            }

            @Override
            public void stop() {
            }
        };

        final int[][][] result = captureImpl.captureAsRGBMatrix();

        // Verify dimensions
        assertEquals(height, result.length);
        assertEquals(width, result[0].length);
        assertEquals(3, result[0][0].length);

        // Verify Red pixel (255, 0, 0)
        assertEquals(255, result[0][0][0]);
        assertEquals(0, result[0][0][1]);
        assertEquals(0, result[0][0][2]);

        // Verify Green pixel (0, 255, 0)
        assertEquals(0, result[0][1][0]);
        assertEquals(255, result[0][1][1]);
        assertEquals(0, result[0][1][2]);

        // Verify Blue pixel (0, 0, 255)
        assertEquals(0, result[1][0][0]);
        assertEquals(0, result[1][0][1]);
        assertEquals(255, result[1][0][2]);

        // Verify White pixel (255, 255, 255)
        assertEquals(255, result[1][1][0]);
        assertEquals(255, result[1][1][1]);
        assertEquals(255, result[1][1][2]);
    }
}
