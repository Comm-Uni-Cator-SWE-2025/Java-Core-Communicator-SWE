package com.swe.ScreenNVideo.Codec;

public interface IDeCompressor {
    void DecompressChrome(short[][] Matrix,short height,short width);
    void DecompressLumin(short[][] Matrix,short height,short width);
};
