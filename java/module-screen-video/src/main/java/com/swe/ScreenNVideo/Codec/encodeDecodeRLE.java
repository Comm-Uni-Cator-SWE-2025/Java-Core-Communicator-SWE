package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

public class encodeDecodeRLE implements IRLE {

    public static final encodeDecodeRLE _encodeDecodeRLE = new encodeDecodeRLE();

    public static  encodeDecodeRLE getInstance(){
        return _encodeDecodeRLE;
    }

    @Override
    public void zigZagRLE(short[][] Matrix, short height, short width, ByteBuffer resRLEbuffer){

    }

    @Override
    public short[][] revZigZagRLE(ByteBuffer resRLEbuffer){
        return new short[8][8]; // demo
    }
}
