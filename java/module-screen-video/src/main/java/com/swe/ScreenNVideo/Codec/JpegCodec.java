package com.swe.ScreenNVideo.Codec;

import java.nio.charset.StandardCharsets;

public class JpegCodec implements Codec {
    private int[][] screenshot;

    public JpegCodec(int[][] screenshot) {
        this.screenshot = screenshot;
    }

    public JpegCodec() {
    }

    public void setScreenshot(int[][] screenshot) {
        System.out.println("Dims : " + screenshot.length + " " + screenshot[0].length);
        this.screenshot = screenshot;
    }

    //convert screenshot into YCbCr format (4:2:0) chroma sampling
    @Override
    public byte[] encode(int x, int y, int height, int width) {

        System.out.println("x y W H = " + x + " " + y + ":" + width + " " + height);

        int[][] YMatrix = new int[height][width];
        int[][] CbMatrix = new int[height / 2][width / 2];
        int[][] CrMatrix = new int[height / 2][width / 2];


        for (int i = y; i - y < height; i += 2) {
            for (int j = x; j - x < width; j += 2) {
                double cb_pixel = 0;
                double cr_pixel = 0;
                for (int ii = i; ii - i < 2; ++ii) {
                    for (int jj = j; jj - j < 2; ++jj) {
                        int pixel = screenshot[ii][jj];

                        int r = (pixel >> 16) & 0xff;
                        int g = (pixel >> 8) & 0xff;
                        int b = pixel & 0xff;

                        // conversion to YCbCr
                        int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                        double Cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;
                        double Cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;

                        // Clamp to 0-255
                        Y = Math.min(255, Math.max(0, Y));
                        YMatrix[i + ii][j + jj] = Y;
                        cb_pixel += Cb;
                        cr_pixel += Cr;
                    }
                }

                CbMatrix[i / 2][j / 2] = Math.min(255, Math.max(0, (int) (cb_pixel / 4)));
                CrMatrix[i / 2][j / 2] = Math.min(255, Math.max(0, (int) (cr_pixel / 4)));

            }
        }

        String str = "H:" + height + ",W:" + width + ";" +
            "Y:" + zigZagScan(YMatrix) + "Cb:" + zigZagScan(CbMatrix) +
            "Cr:" + zigZagScan(CrMatrix);
        byte[] imagebytes = str.getBytes(StandardCharsets.UTF_8);
        return imagebytes;
    }

    @Override
    public int[][] decode(byte[] encodedImage) {
        String recoveredImage = new String(encodedImage, StandardCharsets.UTF_8);

        String[] parts = recoveredImage.split(";");
        String[] dims = parts[0].split(",");

        // Extracting height and width of the color matrix from string
        int height = Integer.parseInt(dims[0].split("H:")[1]);
        int width = Integer.parseInt(dims[1].split("W:")[1]);

        // Extracting Y,Cb,Cr from the string
        String Ystring = parts[1].split("Cb:")[0].split("Y:")[1];
        String Cbstring = parts[1].split("Cr:")[0].split("Cb")[1];
        String Crstring = parts[1].split("Cr:")[1];

        int[][] Y = reverseZigZagScan(height, width, Ystring);
        int[][] Cb = reverseZigZagScan(height / 2, width / 2, Cbstring);
        int[][] Cr = reverseZigZagScan(height / 2, width / 2, Crstring);

        int[][] RGB = convertYCbCrToRGB(Y, Cb, Cr);

        return RGB;
    }


    private int[][] convertYCbCrToRGB(int[][] Y, int[][] Cb, int[][] Cr) {
        int height = Y.length;
        int width = Y[0].length;

        int[][] RGB = new int[height][width]; // [row][col]

        for (int i = 0; i < height / 2; ++i) {
            for (int j = 0; j < width / 2; ++j) {
                int cb = Cb[i][j];
                int cr = Cr[i][j];

                for (int ii = 0; ii < 2; ++ii) {
                    for (int jj = 0; jj < 2; ++jj) {
                        int y = Y[2 * i + ii][2 * j + jj];

                        int r = (int) Math.round(y + 1.402 * cr);
                        int g = (int) Math.round(y - 0.344136 * cb - 0.714136 * cr);
                        int b = (int) Math.round(y + 1.772 * cb);

                        // clamp to [0,255]
                        r = Math.min(255, Math.max(0, r));
                        g = Math.min(255, Math.max(0, g));
                        b = Math.min(255, Math.max(0, b));

                        RGB[2 * i + ii][2 * j + jj] = (r << 16) | (g << 8) | b;
                    }
                }
            }
        }

        return RGB;
    }


    private String zigZagScan(int[][] Matrix) {
        int M = Matrix.length;
        int N = Matrix[0].length;

        StringBuilder sb = new StringBuilder();

        // number of diagonals = M+N-1;
        // rule 1 :
        //   if diagonal index is even then move bottom -> top
        // rule 2:
        //   if diagonal index is odd then move top -> bottom

        for (int diag = 0; diag < (M + N - 1); ++diag) {
            int rowStart = Math.max(0, diag - (N - 1));
            int rowEnd = Math.min(M - 1, diag);

            if ((diag & 1) == 1) {
                //odd diagonal index : top->bottom
                for (int i = rowStart; i <= rowEnd; ++i) {
                    int j = diag - i;
                    sb.append(Matrix[i][j]).append(" ");
                }
            } else {
                //even diagonal index : bottom->top
                for (int i = rowEnd; i >= rowStart; --i) {
                    int j = diag - i;
                    sb.append(Matrix[i][j]).append(" ");
                }
            }
        }

        return sb.toString().trim();
    }

    private int[][] reverseZigZagScan(int height, int width, String zigZagString) {
        String[] matrixCells = zigZagString.split(" ");
        int[][] reqMatrix = new int[height][width];

        int cellCounter = 0;

        for (int diag = 0; diag < (height + width - 1); ++diag) {
            int rowStart = Math.max(0, diag - (width - 1));
            int rowEnd = Math.min(height - 1, diag);

            if ((diag & 1) == 1) {
                //odd diagonal index : top->bottom
                for (int i = rowStart; i <= rowEnd; ++i) {
                    int j = diag - i;
                    reqMatrix[i][j] = Integer.parseInt(matrixCells[cellCounter++]);
                }
            } else {
                //even diagonal index : bottom->top
                for (int i = rowEnd; i >= rowStart; --i) {
                    int j = diag - i;
                    reqMatrix[i][j] = Integer.parseInt(matrixCells[cellCounter++]);
                }
            }
        }

        return reqMatrix;
    }

}
