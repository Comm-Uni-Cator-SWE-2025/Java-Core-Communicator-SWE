package com.swe.ScreenNVideo.Codec;

/**
 * Interface for encoding and decoding an image.
 *
 */
public interface Codec {

    /**
     * Encode and Compress the image.
     *
     * @param screenShot image matrix to be encoded
     * @param x topLeft postition along x axis of image matrix
     * @param y topLeft position along y axis of image matrix
     * @param height block's height
     * @param width block's width
     * @return an array bytes
     */
    byte[] encode(final int[][] screenShot, final int x, final int y, final int height, final int width);

    /**
     * Decode and Decompress the image.
     *
     * @param encodedImage image to be decoded
     * @return decoded image matrix
     */
    int[][] decode(final byte[] encodedImage);

}
