package com.swe.ScreenNVideo.Codec;

public class DeCompressor implements  IDeCompressor{

    IFIDCT _dctmodule;
    QuantisationUtil _quantmodule;

    DeCompressor(){
        _dctmodule = AANdct.getInstance();
        _quantmodule = QuantisationUtil.getInstance();
    }

    @Override
    public void DecompressChrome(short[][] Matrix,short height,short width){
        byte[] RLE = new byte[2*height*width];
        int start = 0;
        for(short i = 0;i<height;i+=8){
            for(short j = 0;j<width;j+=8){
                _quantmodule.DeQuantisationChrome(Matrix,i,j);
                _dctmodule.Idct(Matrix,i,j);
            }
        }
    }

    @Override
    public void DecompressLumin(short[][] Matrix,short height,short width){
        byte[] RLE = new byte[2*height*width];
        int start = 0;
        for(short i = 0;i<height;i+=8){
            for(short j = 0;j<width;j+=8){
                _quantmodule.DeQuantisationLumin(Matrix,i,j);
                _dctmodule.Idct(Matrix,i,j);
            }
        }
    }

}
