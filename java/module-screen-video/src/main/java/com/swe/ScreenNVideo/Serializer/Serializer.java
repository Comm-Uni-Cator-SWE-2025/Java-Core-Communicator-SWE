package com.swe.ScreenNVideo.Serializer;

import java.nio.ByteBuffer;

import com.swe.ScreenNVideo.Utils;

/**
 * Serializer class for serializing and deserializing images.
 */
public class Serializer {
    /**
     * Serializes the image.
     * @param image the image to be serialized
     * @return serialized byte array
     */
    public static byte[] serializeImage(final int[][] image) {
        final int height = image.length;
        final int width = image[0].length;
        final ByteBuffer buffer = ByteBuffer.allocate((height * width) * 3 + 8); // three for
        buffer.putInt(height);
        buffer.putInt(width);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final int pixel = image[i][j];
                final byte r = (byte) ((pixel >> Utils.INT_MASK_16) & Utils.BYTE_MASK);
                final byte g = (byte) ((pixel >> Utils.INT_MASK_8) & Utils.BYTE_MASK);
                final byte b = (byte) (pixel & Utils.BYTE_MASK);

                buffer.put(r);
                buffer.put(g);
                buffer.put(b);
            }
        }
        return buffer.array();
    }

    /**
     * Deserializes the image.
     * @param data the byte array to be deserialized
     * @return the image.
     */
    public static int[][] deserializeImage(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final int height = buffer.getInt();
        final int width = buffer.getInt();
        final int[][] image = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final int r = buffer.get() & Utils.BYTE_MASK;
                final int g = buffer.get() & Utils.BYTE_MASK;
                final int b = buffer.get() & Utils.BYTE_MASK;

                // ARGB pixel with full alpha (Utils.BYTE_MASK000000)
                final int pixel = (Utils.BYTE_MASK << Utils.INT_MASK_24) | (
                    r << Utils.INT_MASK_16) | (g << Utils.INT_MASK_8) | b;

                image[i][j] = pixel;
            }
        }
        return image;
    }
}
