package com.swe.ScreenNVideo.Capture;

import java.awt.*;
import java.awt.image.BufferedImage;

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
