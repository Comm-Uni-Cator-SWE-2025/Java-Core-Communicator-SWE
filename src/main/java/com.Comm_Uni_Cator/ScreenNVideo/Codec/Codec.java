package com.Comm_Uni_Cator.ScreenNVideo.Codec;

import java.awt.image.BufferedImage;

public interface Codec {
    String Encode(int x, int y, int height, int weight);
    BufferedImage Decode(String encoded_image);
}
