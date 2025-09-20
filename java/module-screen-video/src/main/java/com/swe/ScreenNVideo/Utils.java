package com.swe.ScreenNVideo;

import java.io.ByteArrayOutputStream;

public class Utils {
    public static final int HASH_STRIDE = 2;
    public static final String START_VIDEO_CAPTURE = "startVideoCapture";
    public static final String STOP_VIDEO_CAPTURE = "stopVideoCapture";
    public static final String START_SCREEN_CAPTURE = "startScreenCapture";
    public static final String STOP_SCREEN_CAPTURE = "stopScreenCapture";
    public static final String UPDATE_UI = "updateUI";
    public static final String MODULE_REMOTE_KEY = "screenNVideo";
    public static final int BUFFER_SIZE = 1024 * 10; // 10 kb

    public  static  void writeInt(final ByteArrayOutputStream bufferOut, int data) {
        bufferOut.write(data & 0xFF);
        bufferOut.write((data >> 8) & 0xFF);
        bufferOut.write((data >> 16) & 0xFF);
        bufferOut.write((data >> 24) & 0xFF);
    }

}
