package com.swe.ScreenNVideo.Capture;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

/**
 * The {@code ScreenCapture} class is responsible for capturing
 * the entire screen as a {@link BufferedImage}.
 *
 * <p>It uses the {@link Robot} class to create a screenshot of the
 * current display. Works on Windows, macOS, and Linux.
 */
public class ScreenCapture extends ICapture {

    /**
     * Captures the entire screen and returns it as a BufferedImage.
     * Works on Windows, macOS, and Linux.t
     *
     * @return BufferedImage containing the screenshot
     * @throws AWTException if the platform configuration does not allow low-level input control
     */
    @Override
    public BufferedImage capture() throws AWTException {

        // Get screen size
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Rectangle screenRect = new Rectangle(screenSize);

        // Create Robot instance and capture screen
        final Robot robot = new Robot();

        return robot.createScreenCapture(screenRect);
    }
}
