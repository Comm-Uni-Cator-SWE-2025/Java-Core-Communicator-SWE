package com.swe.ScreenNVideo.Codec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive test suite for BilinearScaler class.
 * Tests bilinear interpolation for image scaling operations.
 */
public class BilinearScalerTest {

    /**
     * Small dimension for test images.
     */
    private static final int SMALL_DIM = 8;

    /**
     * Medium dimension for test images.
     */
    private static final int MEDIUM_DIM = 16;

    /**
     * Large dimension for test images.
     */
    private static final int LARGE_DIM = 32;

    /**
     * Color black in ARGB format.
     */
    private static final int COLOR_BLACK = 0xFF000000;

    /**
     * Color white in ARGB format.
     */
    private static final int COLOR_WHITE = 0xFFFFFFFF;

    /**
     * Color red in ARGB format.
     */
    private static final int COLOR_RED = 0xFFFF0000;

    /**
     * Color green in ARGB format.
     */
    private static final int COLOR_GREEN = 0xFF00FF00;

    /**
     * Color blue in ARGB format.
     */
    private static final int COLOR_BLUE = 0xFF0000FF;

    /**
     * Color gray in ARGB format.
     */
    private static final int COLOR_GRAY = 0xFF808080;

    /**
     * Alpha mask for extracting alpha channel.
     */
    private static final int ALPHA_MASK = 0xFF000000;

    /**
     * Red shift for extracting red channel.
     */
    private static final int RED_SHIFT = 16;

    /**
     * Green shift for extracting green channel.
     */
    private static final int GREEN_SHIFT = 8;

    /**
     * Color mask for extracting color channels.
     */
    private static final int COLOR_MASK = 0xFF;

    /**
     * Delta for color comparison (allows for interpolation rounding).
     */
    private static final int COLOR_DELTA = 2;

    /**
     * BilinearScaler instance for testing.
     */
    private BilinearScaler scaler;

    /**
     * Sets up test fixture before each test.
     */
    @BeforeEach
    public void setUp() {
        scaler = new BilinearScaler();
    }

    /**
     * Tests that constructor creates non-null instance.
     */
    @Test
    public void testConstructorCreatesInstance() {
        assertNotNull(scaler);
    }

    /**
     * Tests scale with same dimensions (no scaling).
     */
    @Test
    public void testScaleSameDimensions() {
        final int[][] image = createSolidColorImage(SMALL_DIM, SMALL_DIM, COLOR_BLACK);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        assertNotNull(scaled);
        assertEquals(SMALL_DIM, scaled.length);
        assertEquals(SMALL_DIM, scaled[0].length);
    }

    /**
     * Tests scale up from 8x8 to 16x16.
     */
    @Test
    public void testScaleUp8x8To16x16() {
        final int[][] image = createSolidColorImage(SMALL_DIM, SMALL_DIM, COLOR_WHITE);
        final int[][] scaled = scaler.scale(image, MEDIUM_DIM, MEDIUM_DIM);

        assertNotNull(scaled);
        assertEquals(MEDIUM_DIM, scaled.length);
        assertEquals(MEDIUM_DIM, scaled[0].length);
    }

    /**
     * Tests scale down from 16x16 to 8x8.
     */
    @Test
    public void testScaleDown16x16To8x8() {
        final int[][] image = createSolidColorImage(MEDIUM_DIM, MEDIUM_DIM, COLOR_WHITE);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        assertNotNull(scaled);
        assertEquals(SMALL_DIM, scaled.length);
        assertEquals(SMALL_DIM, scaled[0].length);
    }

    /**
     * Tests scale up from 8x8 to 32x32.
     */
    @Test
    public void testScaleUp8x8To32x32() {
        final int[][] image = createSolidColorImage(SMALL_DIM, SMALL_DIM, COLOR_RED);
        final int[][] scaled = scaler.scale(image, LARGE_DIM, LARGE_DIM);

        assertNotNull(scaled);
        assertEquals(LARGE_DIM, scaled.length);
        assertEquals(LARGE_DIM, scaled[0].length);
    }

    /**
     * Tests scale down from 32x32 to 8x8.
     */
    @Test
    public void testScaleDown32x32To8x8() {
        final int[][] image = createSolidColorImage(LARGE_DIM, LARGE_DIM, COLOR_BLUE);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        assertNotNull(scaled);
        assertEquals(SMALL_DIM, scaled.length);
        assertEquals(SMALL_DIM, scaled[0].length);
    }

    /**
     * Tests scale with non-square aspect ratio (8x16 to 16x8).
     */
    @Test
    public void testScaleNonSquareAspectRatio() {
        final int[][] image = createSolidColorImage(SMALL_DIM, MEDIUM_DIM, COLOR_GREEN);
        final int[][] scaled = scaler.scale(image, MEDIUM_DIM, SMALL_DIM);

        assertNotNull(scaled);
        assertEquals(MEDIUM_DIM, scaled.length);
        assertEquals(SMALL_DIM, scaled[0].length);
    }

    /**
     * Tests scale with rectangular image (16x8).
     */
    @Test
    public void testScaleRectangularImage() {
        final int[][] image = createSolidColorImage(MEDIUM_DIM, SMALL_DIM, COLOR_GRAY);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, MEDIUM_DIM);

        assertNotNull(scaled);
        assertEquals(SMALL_DIM, scaled.length);
        assertEquals(MEDIUM_DIM, scaled[0].length);
    }

    /**
     * Tests scale preserves solid color (black).
     */
    @Test
    public void testScalePreservesSolidColorBlack() {
        final int[][] image = createSolidColorImage(MEDIUM_DIM, MEDIUM_DIM, COLOR_BLACK);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        verifySolidColor(scaled, COLOR_BLACK);
    }

    /**
     * Tests scale preserves solid color (white).
     */
    @Test
    public void testScalePreservesSolidColorWhite() {
        final int[][] image = createSolidColorImage(MEDIUM_DIM, MEDIUM_DIM, COLOR_WHITE);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        verifySolidColor(scaled, COLOR_WHITE);
    }

    /**
     * Tests scale preserves solid color (red).
     */
    @Test
    public void testScalePreservesSolidColorRed() {
        final int[][] image = createSolidColorImage(MEDIUM_DIM, MEDIUM_DIM, COLOR_RED);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        verifySolidColor(scaled, COLOR_RED);
    }

    /**
     * Tests scale with gradient image.
     */
    @Test
    public void testScaleGradientImage() {
        final int[][] image = createGradientImage(MEDIUM_DIM, MEDIUM_DIM);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        assertNotNull(scaled);
        assertEquals(SMALL_DIM, scaled.length);
        assertEquals(SMALL_DIM, scaled[0].length);
    }

    /**
     * Tests scale with checkerboard pattern.
     */
    @Test
    public void testScaleCheckerboardPattern() {
        final int[][] image = createCheckerboardImage(MEDIUM_DIM, MEDIUM_DIM);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        assertNotNull(scaled);
        assertEquals(SMALL_DIM, scaled.length);
        assertEquals(SMALL_DIM, scaled[0].length);
    }

    /**
     * Tests scale up preserves gradient characteristics.
     */
    @Test
    public void testScaleUpPreservesGradient() {
        final int[][] image = createGradientImage(SMALL_DIM, SMALL_DIM);
        final int[][] scaled = scaler.scale(image, MEDIUM_DIM, MEDIUM_DIM);

        assertNotNull(scaled);
        assertEquals(MEDIUM_DIM, scaled.length);
        assertEquals(MEDIUM_DIM, scaled[0].length);

        // Verify gradient is preserved (monotonic increase)
        for (int i = 0; i < scaled.length - 1; i++) {
            final int current = getRedChannel(scaled[i][0]);
            final int next = getRedChannel(scaled[i + 1][0]);
            assertTrue(next >= current - COLOR_DELTA,
                    "Gradient should be approximately preserved");
        }
    }

    /**
     * Tests scale with very small target dimensions (1x1).
     */
    @Test
    public void testScaleTo1x1() {
        final int[][] image = createSolidColorImage(MEDIUM_DIM, MEDIUM_DIM, COLOR_WHITE);
        final int[][] scaled = scaler.scale(image, 1, 1);

        assertNotNull(scaled);
        assertEquals(1, scaled.length);
        assertEquals(1, scaled[0].length);
    }

    /**
     * Tests scale with very large target dimensions.
     */
    @Test
    public void testScaleToLargeDimensions() {
        final int targetSize = 128;
        final int[][] image = createSolidColorImage(SMALL_DIM, SMALL_DIM, COLOR_BLUE);
        final int[][] scaled = scaler.scale(image, targetSize, targetSize);

        assertNotNull(scaled);
        assertEquals(targetSize, scaled.length);
        assertEquals(targetSize, scaled[0].length);
    }

    /**
     * Tests scale with mixed color image.
     */
    @Test
    public void testScaleMixedColorImage() {
        final int[][] image = createMixedColorImage(MEDIUM_DIM, MEDIUM_DIM);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        assertNotNull(scaled);
        assertEquals(SMALL_DIM, scaled.length);
        assertEquals(SMALL_DIM, scaled[0].length);
    }

    /**
     * Tests scale maintains color channel integrity.
     */
    @Test
    public void testScaleMaintainsColorChannels() {
        final int[][] image = createSolidColorImage(MEDIUM_DIM, MEDIUM_DIM, COLOR_GREEN);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        for (int i = 0; i < scaled.length; i++) {
            for (int j = 0; j < scaled[0].length; j++) {
                final int pixel = scaled[i][j];
                final int r = (pixel >> RED_SHIFT) & COLOR_MASK;
                final int g = (pixel >> GREEN_SHIFT) & COLOR_MASK;
                final int b = pixel & COLOR_MASK;

                assertTrue(r >= 0 && r <= COLOR_MASK, "Red channel should be valid");
                assertTrue(g >= 0 && g <= COLOR_MASK, "Green channel should be valid");
                assertTrue(b >= 0 && b <= COLOR_MASK, "Blue channel should be valid");
            }
        }
    }

    /**
     * Tests scale with non-uniform scaling (width and height different ratios).
     */
    @Test
    public void testScaleNonUniformRatio() {
        final int[][] image = createSolidColorImage(MEDIUM_DIM, LARGE_DIM, COLOR_RED);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, MEDIUM_DIM);

        assertNotNull(scaled);
        assertEquals(SMALL_DIM, scaled.length);
        assertEquals(MEDIUM_DIM, scaled[0].length);
    }

    /**
     * Tests scale with edge case: 2x2 to 1x1.
     */
    @Test
    public void testScale2x2To1x1() {
        final int[][] image = createSolidColorImage(2, 2, COLOR_WHITE);
        final int[][] scaled = scaler.scale(image, 1, 1);

        assertNotNull(scaled);
        assertEquals(1, scaled.length);
        assertEquals(1, scaled[0].length);
    }

    /**
     * Tests scale with edge case: 1x1 to 2x2.
     */
    @Test
    public void testScale1x1To2x2() {
        final int[][] image = createSolidColorImage(1, 1, COLOR_BLACK);
        final int[][] scaled = scaler.scale(image, 2, 2);

        assertNotNull(scaled);
        assertEquals(2, scaled.length);
        assertEquals(2, scaled[0].length);
    }

    /**
     * Tests scale with diagonal gradient pattern.
     */
    @Test
    public void testScaleDiagonalGradient() {
        final int[][] image = createDiagonalGradientImage(MEDIUM_DIM, MEDIUM_DIM);
        final int[][] scaled = scaler.scale(image, SMALL_DIM, SMALL_DIM);

        assertNotNull(scaled);
        assertEquals(SMALL_DIM, scaled.length);
        assertEquals(SMALL_DIM, scaled[0].length);
    }

    /**
     * Helper method to create solid color image.
     *
     * @param height image height
     * @param width image width
     * @param color ARGB color value
     * @return image array
     */
    private int[][] createSolidColorImage(final int height, final int width, final int color) {
        final int[][] image = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                image[i][j] = color;
            }
        }
        return image;
    }

    /**
     * Helper method to create gradient image.
     *
     * @param height image height
     * @param width image width
     * @return image array
     */
    private int[][] createGradientImage(final int height, final int width) {
        final int[][] image = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final int value = (i * COLOR_MASK / height);
                image[i][j] = ALPHA_MASK | (value << RED_SHIFT)
                        | (value << GREEN_SHIFT) | value;
            }
        }
        return image;
    }

    /**
     * Helper method to create checkerboard image.
     *
     * @param height image height
     * @param width image width
     * @return image array
     */
    private int[][] createCheckerboardImage(final int height, final int width) {
        final int[][] image = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                image[i][j] = (i + j) % 2 == 0 ? COLOR_WHITE : COLOR_BLACK;
            }
        }
        return image;
    }

    /**
     * Helper method to create mixed color image.
     *
     * @param height image height
     * @param width image width
     * @return image array
     */
    private int[][] createMixedColorImage(final int height, final int width) {
        final int[][] image = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final int colorIndex = (i + j) % 3;
                if (colorIndex == 0) {
                    image[i][j] = COLOR_RED;
                } else if (colorIndex == 1) {
                    image[i][j] = COLOR_GREEN;
                } else {
                    image[i][j] = COLOR_BLUE;
                }
            }
        }
        return image;
    }

    /**
     * Helper method to create diagonal gradient image.
     *
     * @param height image height
     * @param width image width
     * @return image array
     */
    private int[][] createDiagonalGradientImage(final int height, final int width) {
        final int[][] image = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final int value = ((i + j) * COLOR_MASK / (height + width));
                image[i][j] = ALPHA_MASK | (value << RED_SHIFT)
                        | (value << GREEN_SHIFT) | value;
            }
        }
        return image;
    }

    /**
     * Helper method to verify solid color in scaled image.
     *
     * @param scaled scaled image
     * @param expectedColor expected color value
     */
    private void verifySolidColor(final int[][] scaled, final int expectedColor) {
        final int expectedR = (expectedColor >> RED_SHIFT) & COLOR_MASK;
        final int expectedG = (expectedColor >> GREEN_SHIFT) & COLOR_MASK;
        final int expectedB = expectedColor & COLOR_MASK;

        for (int i = 0; i < scaled.length; i++) {
            for (int j = 0; j < scaled[0].length; j++) {
                final int pixel = scaled[i][j];
                final int r = (pixel >> RED_SHIFT) & COLOR_MASK;
                final int g = (pixel >> GREEN_SHIFT) & COLOR_MASK;
                final int b = pixel & COLOR_MASK;

                assertTrue(Math.abs(r - expectedR) <= COLOR_DELTA,
                        "Red channel should match within delta");
                assertTrue(Math.abs(g - expectedG) <= COLOR_DELTA,
                        "Green channel should match within delta");
                assertTrue(Math.abs(b - expectedB) <= COLOR_DELTA,
                        "Blue channel should match within delta");
            }
        }
    }

    /**
     * Helper method to extract red channel from pixel.
     *
     * @param pixel ARGB pixel value
     * @return red channel value
     */
    private int getRedChannel(final int pixel) {
        return (pixel >> RED_SHIFT) & COLOR_MASK;
    }
}

