package com.swe.ScreenNVideo.Codec;

import java.awt.image.BufferedImage;

public interface Codec {
    void setScreenshot(int[][] screenshot); 
    byte[] Encode(int x, int y, int height, int weight);
    int[][] Decode(byte[] encoded_image);
}
