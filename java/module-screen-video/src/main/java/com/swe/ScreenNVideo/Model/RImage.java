/**
 * Contributed by @BhupathiVarun.
 */

package com.swe.ScreenNVideo.Model;

import com.swe.ScreenNVideo.Utils;

import java.nio.ByteBuffer;

/**
 * Image to be sent via RPC.
 */
public class RImage {
    /**
     * Image to be sent to UI.
     */
    private final int[][] image;

    /**
     * ip of the user whose image is this.
     */
    private final String ip;

    /**
     * Data rate.
     */
    private final long dataRate;

    /**
     * Constructor for the RImage class.
     * @param imageArgs The image.
     * @param ipArgs The IP address.
     * @param dataRateArgs The data rate.
     */
    public RImage(final int[][] imageArgs, final String ipArgs, final long dataRateArgs) {
        ip = ipArgs;
        image = imageArgs;
        dataRate = dataRateArgs;
    }

    /**
     * Serializes the image.
     * @return serialized byte array
     */
    public byte[] serialize() {
        final int height = image.length;
        final int width = image[0].length;
        final byte[] ipBytes = ip.getBytes();
        final int lenReq = ipBytes.length + (height * width) * 3 + 12 + Long.BYTES;
        final ByteBuffer buffer = ByteBuffer.allocate(lenReq); // three for rgb
        // put the ip
        buffer.putInt(ipBytes.length);
        buffer.put(ipBytes);

        // put the dataRate
        buffer.putLong(dataRate);

        // put the image
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
     * Get the image.
     * @return the image
     */
    public int[][] getImage() {
        return image;
    }

    /**
     * Get the IP address.
     * @return the IP address
     */
    public String getIp() {
        return ip;
    }

    /**
     * Get the data rate.
     * @return the data rate
     */
    public long getDataRate() {
        return dataRate;
    }
}
