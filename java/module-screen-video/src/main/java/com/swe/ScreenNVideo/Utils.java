package com.swe.ScreenNVideo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class Utils {
    public static final int HASH_STRIDE = 2;
    public static final String START_VIDEO_CAPTURE = "startVideoCapture";
    public static final String STOP_VIDEO_CAPTURE = "stopVideoCapture";
    public static final String START_SCREEN_CAPTURE = "startScreenCapture";
    public static final String STOP_SCREEN_CAPTURE = "stopScreenCapture";
    public static final String SUBSCRIBE_AS_VIEWER = "subscribeAsViewer";
    public static final String UPDATE_UI = "updateUI";
    public static final String MODULE_REMOTE_KEY = "screenNVideo";
    public static final int BUFFER_SIZE = 1024 * 10; // 10 kb
    public static final int SCALE_X = 7;
    public static final int SCALE_Y = 5;
    public static final int VIDEO_PADDING_X = 20;
    public static final int VIDEO_PADDING_Y = 20;


    public  static  void writeInt(final ByteArrayOutputStream bufferOut, int data) {
        bufferOut.write((data >> 24) & 0xFF);
        bufferOut.write((data >> 16) & 0xFF);
        bufferOut.write((data >> 8) & 0xFF);
        bufferOut.write(data & 0xFF);
    }

    /**
     * Converts the given image to its rgb form.
     * @param feed the image
     * @return int[][] : RGB matrix 0xAARRGGBB / 0x00RRGGBB
     */
    public static int[][] convertToRGBMatrix(final BufferedImage feed) {
        final int[][] matrix = new int[feed.getHeight()][feed.getWidth()];
        for (int i = 0; i < feed.getHeight(); i++) {
            for (int j = 0; j < feed.getWidth(); j++) {
                matrix[i][j] = feed.getRGB(j, i);
            }
        }
        return matrix;
    }

}
