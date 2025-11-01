package com.swe.ScreenNVideo.Codec;

/**
 * Provides an implementation of the {@link ImageScaler} interface 
 * using Bilinear Interpolation.
 *
 * <p>
 * Bilinear Interpolation computes the color of each pixel in the scaled 
 * image as a weighted average of the four nearest pixels in the original
 * image. This produces smoother results than nearest-neighbour scaling,
 * especially when upscalling.
 * </p>
 *
 * @see ImageScaler
 */
public class BilinearScaler implements ImageScaler {

    /** Number of fractional bits used for bilinear weights. */
    private static final int FRAC_BITS = 8;

    /** 1.0 in fixed-point representation (1 << 8 = 256). */
    private static final int ONE_FP = 1 << FRAC_BITS;  // 256

    /** Used to convert from 16 fractional bits back to integer (8 + 8 = 16). */
    private static final int BILINEAR_SHIFT = FRAC_BITS * 2; // 16

    /** Used when converting a float to fixed-point to round correctly. */
    private static final double ROUNDING_EPSILON = 0.5;

    /** Offset for red color component in the AARRGGBB value.*/
    private static final int R_OFFSET = 16;
              
    /** Offset for green color component in the AARRGGBB value.*/
    private static final int G_OFFSET = 8;
                  
    /** Mask for extracting color component. */
    private static final int MASK = 0xFF;

    /** Scales the given image using bilinear interpolation to the specified
     * width and height.
     *
     * @param matrix the image to be scaled 
     * @param targetHeight the target height of the scaled image (must be positive)
     * @param targetWidth the target width of the scaled image (must be positive)
     * @return the scaled image
     * */
    public int[][] scale(final int[][] matrix, final int targetHeight, final int targetWidth) {

        final int inputHeight = matrix.length;
        final int inputWidth = matrix[0].length;

        final double scaleY = (double) inputHeight / targetHeight;
        final double scaleX = (double) inputWidth / targetWidth;

        final int[][] reqMatrix = new int[targetHeight][targetWidth];

        // Precompute x coordinates
        final int[] x0 = new int[targetWidth];
        final int[] x1 = new int[targetWidth];
        final int[] dx = new int[targetWidth];
        final int[] invDx = new int[targetWidth];

        for (int xOut = 0; xOut < targetWidth; xOut++) {
            final double xIn = (xOut + 0.5) * scaleX - 0.5;

            x0[xOut] = Math.max((int) Math.floor(xIn), 0);
            x1[xOut] = Math.min(x0[xOut] + 1, inputWidth - 1);

            final double dxTemp = xIn - x0[xOut];
            dx[xOut] = (int) (dxTemp * ONE_FP + ROUNDING_EPSILON);
            invDx[xOut] = ONE_FP - dx[xOut];
        }

        for (int yOut = 0; yOut < targetHeight; yOut++) {

            final double yIn = (yOut + 0.5) * scaleY - 0.5;

            final int y0 = Math.max(0, (int) Math.floor(yIn));
            final int y1 = Math.min(y0 + 1, inputHeight - 1);

            final double dyTemp = yIn - y0;
            final int dy = (int) (dyTemp * 256 + 0.5);
            final int invDy = 256 - dy;

            for (int xOut = 0; xOut < targetWidth; xOut++) {

                final int pixel00 = matrix[y0][x0[xOut]];
                final int pixel01 = matrix[y0][x1[xOut]];
                final int pixel10 = matrix[y1][x0[xOut]];
                final int pixel11 = matrix[y1][x1[xOut]];

                // Extract Channels
                final int r00 = (pixel00 >> R_OFFSET) & MASK;
                final int g00 = (pixel00 >> G_OFFSET) & MASK;
                final int b00 = pixel00 & MASK;

                final int r01 = (pixel01 >> R_OFFSET) & MASK;
                final int g01 = (pixel01 >> G_OFFSET) & MASK;
                final int b01 = pixel01 & MASK;

                final int r10 = (pixel10 >> R_OFFSET) & MASK;
                final int g10 = (pixel10 >> G_OFFSET) & MASK;
                final int b10 = pixel10 & MASK;

                final int r11 = (pixel11 >> R_OFFSET) & MASK;
                final int g11 = (pixel11 >> G_OFFSET) & MASK;
                final int b11 = pixel11 & MASK;

                // Bilinear weights
                final int w00 = invDx[xOut] * invDy;
                final int w01 = dx[xOut] * invDy;
                final int w10 = invDx[xOut] * dy;
                final int w11 = dx[xOut] * dy;

                // Interpolate each channel separately
                final int r = (w00 * r00 + w01 * r01 + w10 * r10 + w11 * r11) >> BILINEAR_SHIFT;
                final int g = (w00 * g00 + w01 * g01 + w10 * g10 + w11 * g11) >> BILINEAR_SHIFT;
                final int b = (w00 * b00 + w01 * b01 + w10 * b10 + w11 * b11) >> BILINEAR_SHIFT;

                // Combine channels and store it
                reqMatrix[yOut][xOut] = (r << R_OFFSET) | (g << G_OFFSET) | b;
            }
        }
        return reqMatrix;
    }
}
