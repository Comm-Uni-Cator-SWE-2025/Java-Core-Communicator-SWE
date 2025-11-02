/**
 * Contributed by Devansh Manoj Kesan.
 */

package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

interface IRLE {

    void zigZagRLE(short[][] matrix, ByteBuffer resRLEbuffer);

    short[][] revZigZagRLE(ByteBuffer resRLEbuffer);

}
