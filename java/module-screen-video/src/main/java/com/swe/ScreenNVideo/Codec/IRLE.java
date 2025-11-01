package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

interface IRLE {

    void zigZagRLE(short[][] Matrix, short height, short width, ByteBuffer resRLEbuffer);

    short[][] revZigZagRLE(ByteBuffer resRLEbuffer);

}
