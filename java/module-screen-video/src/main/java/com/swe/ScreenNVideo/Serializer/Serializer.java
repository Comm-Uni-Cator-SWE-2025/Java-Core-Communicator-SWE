package com.swe.ScreenNVideo.Serializer;

import java.nio.ByteBuffer;

public class Serializer {
    public static byte[] serializeImage(int[][] image) {
        int height = image.length;
        int width = image[0].length;
        final ByteBuffer buffer = ByteBuffer.allocate((height * width) * 3 + 8); // three for
        buffer.putInt(height);
        buffer.putInt(width);
        for (int i = 0; i < height; i ++) {
            for (int j = 0; j < width; j ++) {
                int pixel = image[i][j];
                byte r = (byte) ((pixel>>16) & 0xff);
                byte g = (byte) ((pixel>>8) & 0xff);
                byte b = (byte) (pixel & 0xff);

                buffer.put(r);
                buffer.put(g);
                buffer.put(b);
            }
        }
        return buffer.array();
    }
    public static int[][] deserializeImage(byte[] data) {
        int height = data[0] << 24 | data[1] << 16 | data[2] << 8 | data[3];
        int width = data[4] << 24 | data[5] << 16 | data[6] << 8 | data[7];
        int[][] image = new int[height][width];
        for (int i = 0; i < height; i ++) {
            for (int j = 0; j < width; j ++) {
                int pixel = 0;
                pixel |= data[8 + (i * width + j) * 3] << 16;
                pixel |= data[8 + (i * width + j) * 3 + 1] << 8;
                pixel |= data[8 + (i * width + j) * 3 + 2];
                image[i][j] = pixel;
            }
        }
        return image;
    }
}
