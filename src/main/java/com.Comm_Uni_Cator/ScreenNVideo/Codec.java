package com.Comm_Uni_Cator.ScreenNVideo;

import java.awt.image.BufferedImage;

interface Codec {
    String Encoder (int x, int y, int height, int weight);
    BufferedImage Decoder (String encoded_image);
}
