package com.swe.ScreenNVideo.Codec;

public interface Codec {
    void setScreenshot(int[][] screenshot); 

    byte[] encode(int x, int y, int height, int weight);

    int[][] decode(byte[] encodedImage);
}
