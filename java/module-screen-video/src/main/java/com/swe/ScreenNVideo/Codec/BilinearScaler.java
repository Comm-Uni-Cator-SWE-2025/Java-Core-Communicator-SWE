package com.swe.ScreenNVideo.Codec;

public class BilinearScaler implements ImageScaler {
  
  public int[][] Scale(int[][] matrix, int targetHeight, int targetWidth) {

    int inputHeight = matrix.length;
    int inputWidth = matrix[0].length;

    double scaleY = (double)inputHeight/targetHeight;
    double scaleX = (double)inputWidth/targetWidth;

    int[][] reqMatrix = new int[targetHeight][targetWidth];

    for (int y_out = 0; y_out < targetHeight; y_out++) {
      for (int x_out = 0; x_out < targetWidth; x_out++) {
       
        // Map it to input coordinates
        double x_in = (x_out + 0.5) * scaleX - 0.5;
        double y_in = (y_out + 0.5) * scaleY - 0.5;

        // Locate 4 surrounding neighbours
        int x0 = (int)Math.floor(x_in);
        int x1 = Math.min(x0+1, inputWidth-1);
        int y0 = (int)Math.floor(y_in);
        int y1 = Math.min(y0+1, inputHeight-1);

        double dx = x_in - x0;
        double dy = y_in - y0;

        // Extract channels
        int r00 = (matrix[y0][x0] >> 16) & 0xFF;
        int g00 = (matrix[y0][x0] >> 8) & 0xFF;
        int b00 = matrix[y0][x0] & 0xFF;

        int r01 = (matrix[y0][x1] >> 16) & 0xFF;
        int g01 = (matrix[y0][x1] >> 8) & 0xFF;
        int b01 = matrix[y0][x1] & 0xFF;

        int r10 = (matrix[y1][x0] >> 16) & 0xFF;
        int g10 = (matrix[y1][x0] >> 8) & 0xFF;
        int b10 = matrix[y1][x0] & 0xFF;

        int r11 = (matrix[y1][x1] >> 16) & 0xFF;
        int g11 = (matrix[y1][x1] >> 8) & 0xFF;
        int b11 = matrix[y1][x1] & 0xFF;
  
        // Interpolate each channel separately
        int r = (int)Math.round(
              (1-dx) * (1-dy) * r00 +
              dx * (1-dy) * r10 +
              (1-dx) * dy * r01 + 
              dx * dy * r11
            );

        int g = (int)Math.round(
              (1-dx) * (1-dy) * g00 +
              dx * (1-dy) * g10 +
              (1-dx) * dy * g01 + 
              dx * dy * g11
            );

        int b = (int)Math.round(
              (1-dx) * (1-dy) * b00 +
              dx * (1-dy) * b10 +
              (1-dx) * dy * b01 + 
              dx * dy * b11
            );

        // Clamp
        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));

        // Combine channels
        int rgb = (r << 16 ) | (g << 8) | b;
        reqMatrix[y_out][x_out] = rgb;
      }
    }

    return reqMatrix;

  }
}
