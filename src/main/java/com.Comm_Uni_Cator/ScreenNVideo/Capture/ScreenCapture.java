package com.Comm_Uni_Cator.ScreenNVideo.Capture;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenCapture extends ICapture {

    /**
     * Captures the entire screen and returns it as a BufferedImage.
     * Works on Windows, macOS, and Linux.
     *
     * @return BufferedImage containing the screenshot
     * @throws AWTException if the platform configuration does not allow low-level input control
     */
    @Override
    public BufferedImage capture() throws AWTException {

        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);

        // Create Robot instance and capture screen
        Robot robot = new Robot();
        return robot.createScreenCapture(screenRect);
    }
}
