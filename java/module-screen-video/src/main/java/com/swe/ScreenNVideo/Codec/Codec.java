package com.swe.ScreenNVideo.Codec;

import java.awt.image.BufferedImage;

public interface Codec {
    byte[] Encode(int x, int y, int height, int weight);
    BufferedImage Decode(byte[] encoded_image);
}
