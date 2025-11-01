package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;

class Compressor implements  ICompressor{

    IFIDCT _dctmodule;
    QuantisationUtil _quantmodule;
    IRLE _enDeRLE;

    Compressor(){
        _dctmodule = AANdct.getInstance();
        _quantmodule = QuantisationUtil.getInstance();
        _quantmodule.scaleQuantTable(_dctmodule.getScaleFactor());
        _enDeRLE = encodeDecodeRLE.getInstance();
    }

    @Override
    public void compressChrome(short[][] Matrix,short height,short width,ByteBuffer resBuffer){

        resBuffer.putShort((short) (height / 8));
        resBuffer.putShort((short) (width / 8));

        for(short i = 0;i<height;i+=8){
            for(short j = 0;j<width;j+=8){
                _dctmodule.Fdct(Matrix,i,j);
                _quantmodule.QuantisationChrome(Matrix,i,j);
            }
        }

        _enDeRLE.zigZagRLE(Matrix,height,width,resBuffer);
    }

    @Override
    public void compressLumin(short[][] Matrix,short height,short width,ByteBuffer resBuffer){

        resBuffer.putShort((short) (height / 8));
        resBuffer.putShort((short) (width / 8));

        for(short i = 0;i<height;i+=8){
            for(short j = 0;j<width;j+=8){
                _dctmodule.Fdct(Matrix,i,j);
                _quantmodule.QuantisationLumin(Matrix,i,j);
            }
        }

        _enDeRLE.zigZagRLE(Matrix,height,width,resBuffer);
    }
}
