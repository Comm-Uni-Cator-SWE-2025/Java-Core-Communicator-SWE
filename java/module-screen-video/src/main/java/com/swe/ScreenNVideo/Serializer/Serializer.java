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
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int height = buffer.getInt();
        int width = buffer.getInt();
        int[][] image = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int r = buffer.get() & 0xFF;
                int g = buffer.get() & 0xFF;
                int b = buffer.get() & 0xFF;

                // ARGB pixel with full alpha (0xFF000000)
                int pixel = (0xFF << 24) | (r << 16) | (g << 8) | b;

                image[i][j] = pixel;
            }
        }
        return image;
    }
}
