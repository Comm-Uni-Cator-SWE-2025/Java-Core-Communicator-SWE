package com.Comm_Uni_Cator.ScreenNVideo;
import java.awt.image.BufferedImage;


public class YCbCr implements Compressor {
    private BufferedImage screenshot;

    public YCbCr(BufferedImage screenshot){
        this.screenshot = screenshot;
    }

    public YCbCr(){}

    public void setScreenshot(BufferedImage screenshot){
        this.screenshot = screenshot;
    }

    //convert screenshot into YCbCr format (4:2:0) chroma sampling
    @Override
    public String Encoder(int x,int y,int height,int width){

        int[][] YMatrix  = new int[height][width];
        int[][] CbMatrix = new int[height/2][width/2];
        int[][] CrMatrix = new int[height/2][width/2];

        for(int i = y;i-y<height;i+=2){
            for(int j = 0;j-x<width;j+=2){
                double cb_pixel = 0;
                double cr_pixel = 0;
                for(int ii = i;ii-i<2;++ii){
                    for(int jj = j;jj-j<2;++jj){
                        int pixel = screenshot.getRGB(jj,ii);

                        int r = (pixel>>16) & 0xff;
                        int g = (pixel>>8) & 0xff;
                        int b = pixel & 0xff;

                        // conversion to YCbCr
                        int Y  = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                        double Cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;
                        double Cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;

                        // Clamp to 0-255
                        Y = Math.min(255, Math.max(0, Y));
                        YMatrix[i+ii][j+jj] = Y;
                        cb_pixel += Cb;
                        cr_pixel += Cr;


                    }
                }

                CbMatrix[i/2][j/2]  = Math.min(255, Math.max(0, (int)(cb_pixel/4)));
                CrMatrix[i/2][j/2]  = Math.min(255, Math.max(0, (int)(cr_pixel/4)));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Y:").append(zigZagScan(YMatrix)).append("Cb:").append(zigZagScan(CbMatrix)).
                append("Cr:").append(zigZagScan(CrMatrix));
        return sb.toString();
    }

    String zigZagScan(int[][] Matrix){
        int M = Matrix.length;
        int N = Matrix[0].length;
        //
        StringBuilder sb = new StringBuilder();

        // number of diagonals = M+N-1;
        // rule 1 :
        //   if diagonal index is even then move bottom -> top
        // rule 2:
        //   if diagonal index is odd then move top -> bottom

        for(int diag = 0;diag<(M+N-1);++diag){
            int rowStart = Math.max(0,diag-(N-1));
            int rowEnd   = Math.min(M-1,diag);


            if((diag & 1) == 1 ){
                //odd diagonal index : top->bottom
                for(int i = rowStart;i<=rowEnd;++i){
                    int j = diag-i;
                    sb.append(Matrix[i][j]).append(" ");
                }
            } else{
                //even diagonal index : bottom->top
                for(int i = rowEnd;i>=rowStart;--i){
                    int j = diag-i;
                    sb.append(Matrix[i][j]).append(" ");
                }
            }
        }

        return sb.toString().trim();
    }
}
