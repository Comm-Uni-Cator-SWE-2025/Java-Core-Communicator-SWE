package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

interface  ICompressor{
    public void compressChrome(short[][] Matrix, short height, short width, ByteBuffer resBuffer);
    public void compressLumin(short[][] Matrix,short height,short width, ByteBuffer resBuffer);
};
