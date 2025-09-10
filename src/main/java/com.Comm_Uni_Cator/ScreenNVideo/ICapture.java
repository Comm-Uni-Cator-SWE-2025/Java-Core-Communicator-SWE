package com.Comm_Uni_Cator.ScreenNVideo;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface ICapture {
    BufferedImage capture() throws AWTException;
    int[][][] captureAsRGBMatrix() throws AWTException;
}
