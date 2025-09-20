package com.swe.ScreenNVideo.Codec;

/**
 * ImageScaler
 */
public interface ImageScaler {
  int[][] Scale(int[][] matrix, int targetHeight, int targetWidth);
}
