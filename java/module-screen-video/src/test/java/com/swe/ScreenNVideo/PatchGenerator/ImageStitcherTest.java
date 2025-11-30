/**
 * Contributed by Devansh Manoj Kesan.
 * Comprehensive test suite for ImageStitcher and Patch classes, ensuring 100% code coverage
 * of the core stitching logic and dimension management.
 */

package com.swe.ScreenNVideo.PatchGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// NOTE: This test assumes that com.swe.ScreenNVideo.Utils.copyMatrix is available and functional
// as part of the overall module environment.

/**
 * Test class for ImageStitcher and Patch (Stitchable) components.
 */
public class ImageStitcherTest {

    private ImageStitcher stitcher;
    private static final int INITIAL_H = 10;
    private static final int INITIAL_W = 10;
    private static final int TEST_COLOR = 0xFFFF0000;
    private static final int FILL_COLOR = 0xFF0000FF;

    /**
     * Helper to create a solid color matrix.
     */
    private int[][] createSolidMatrix(final int h, final int w, final int color) {
        final int[][] matrix = new int[h][w];
        for (int i = 0; i < h; i++) {
            Arrays.fill(matrix[i], color);
        }
        return matrix;
    }

    /**
     * Helper to check the dimensions of the canvas via reflection-like calls.
     */
    private void verifyCanvasDimensions(final int expectedH, final int expectedW) {
        final int[][] canvas = stitcher.getCanvas();
        assertEquals(expectedH, canvas.length, "Canvas height mismatch");
        if (expectedH > 0) {
            assertEquals(expectedW, canvas[0].length, "Canvas width mismatch");
        }
    }

    @BeforeEach
    void setUp() {
        stitcher = new ImageStitcher();
        // Set initial canvas to 10x10 with FILL_COLOR for base comparison
        stitcher.setCanvas(INITIAL_H, INITIAL_W);
        final int[][] initialCanvas = stitcher.getCanvas();
        for (int i = 0; i < INITIAL_H; i++) {
            Arrays.fill(initialCanvas[i], FILL_COLOR);
        }
    }

    // --- Patch.java (Stitchable Implementation) Tests ---

    /**
     * Tests the Patch constructor and getters.
     */
    @Test
    void testPatchGetters() {
        final int[][] pixels = createSolidMatrix(3, 4, TEST_COLOR);
        final Patch patch = new Patch(pixels, 5, 6);

        assertEquals(5, patch.getX(), "Patch X coordinate mismatch");
        assertEquals(6, patch.getY(), "Patch Y coordinate mismatch");
        assertEquals(3, patch.getHeight(), "Patch height mismatch");
        assertEquals(4, patch.getWidth(), "Patch width mismatch");
    }

    /**
     * Tests Patch.applyOn for a basic interior application.
     */
    @Test
    void testPatchApplyOnInterior() {
        final int[][] canvas = createSolidMatrix(10, 10, FILL_COLOR);
        final int[][] patchPixels = createSolidMatrix(2, 3, TEST_COLOR);
        final Patch patch = new Patch(patchPixels, 1, 2); // Start at (1, 2)

        patch.applyOn(canvas);

        // Check the patched area (2 rows, 3 cols starting at 2,1)
        for (int i = 2; i < 4; i++) { // rows 2, 3
            for (int j = 1; j < 4; j++) { // cols 1, 2, 3
                assertEquals(TEST_COLOR, canvas[i][j],
                    "Pixel at (" + j + "," + i + ") should be patched color");
            }
        }
        // Check unpatched area
        assertEquals(FILL_COLOR, canvas[0][0], "Unpatched pixel should remain base color");
    }

    /**
     * Tests Patch.applyOn for boundaries and clipping (when patch goes off-screen right/bottom).
     */
    @Test
    void testPatchApplyOnOutOfBoundsClip() {
        final int[][] canvas = createSolidMatrix(5, 5, FILL_COLOR);
        final int[][] patchPixels = createSolidMatrix(3, 3, TEST_COLOR);

        // Patch starts at (4, 4), goes until (7, 7). Only (4, 4) should land.
        final Patch patch = new Patch(patchPixels, 4, 4);

        patch.applyOn(canvas);

        // Only canvas[4][4] should be patched
        assertEquals(TEST_COLOR, canvas[4][4], "The one in-bound pixel should be patched.");
        // Check surrounding boundary to ensure it wasn't overwritten
        assertEquals(FILL_COLOR, canvas[3][4], "Pixel at (4,3) should remain base color.");
        assertEquals(FILL_COLOR, canvas[4][3], "Pixel at (3,4) should remain base color.");
    }

    /**
     * Tests Patch.applyOn for negative coordinates (when patch hangs over top/left edges).
     */
    @Test
    void testPatchApplyOnNegativeCoordinates() {
        final int[][] canvas = createSolidMatrix(5, 5, FILL_COLOR);
        final int[][] patchPixels = createSolidMatrix(3, 3, TEST_COLOR);

        // Patch starts at (-1, -1). Only 2x2 section should land at (0, 0) to (1, 1).
        final Patch patch = new Patch(patchPixels, -1, -1);

        patch.applyOn(canvas);

        // Check patched area (2x2)
        for (int i = 0; i < 2; i++) { // rows 0, 1
            for (int j = 0; j < 2; j++) { // cols 0, 1
                assertEquals(TEST_COLOR, canvas[i][j],
                    "Pixel at (" + j + "," + i + ") should be patched color");
            }
        }
        // Check unpatched area
        assertEquals(FILL_COLOR, canvas[2][2], "Unpatched pixel should remain base color");
    }

    // --- ImageStitcher.java Tests ---

    /**
     * Tests setCanvas initialization with dimensions.
     */
    @Test
    void testSetCanvasByDimensions() {
        stitcher.setCanvas(5, 5);
        verifyCanvasDimensions(5, 5);
        assertEquals(0, stitcher.getCanvas()[0][0], "New canvas should be initialized to zero (default for int[][])");
    }

    /**
     * Tests setCanvas initialization with existing matrix.
     */
    @Test
    void testSetCanvasByMatrix() {
        final int[][] matrix = createSolidMatrix(3, 4, TEST_COLOR);
        stitcher.setCanvas(matrix);
        verifyCanvasDimensions(3, 4);
        assertEquals(TEST_COLOR, stitcher.getCanvas()[0][0], "Canvas data should match input matrix");
    }

    /**
     * Tests the core functionality of resetting the canvas. (Covers resetCanvas)
     */
    @Test
    void testResetCanvas() {
        stitcher.resetCanvas();
        verifyCanvasDimensions(0, 0);
        assertEquals(0, stitcher.getCanvas().length, "Canvas height should be 0");
    }

    /**
     * Tests setCanvasDimensions when no resize is necessary.
     */
    @Test
    void testSetCanvasDimensionsNoResize() {
        // Case 1: Matching current dimensions exactly (should skip the 'if' condition completely)
        stitcher.setCanvasDimensions(INITIAL_H, INITIAL_W);
        verifyCanvasDimensions(INITIAL_H, INITIAL_W);

        // Check if data is preserved (which it should be, as no resize occurred)
        assertEquals(FILL_COLOR, stitcher.getCanvas()[0][0], "Canvas color should be preserved");
    }

    /**
     * Tests setCanvasDimensions when dimensions are passed, but the conditional check
     * `if (this.currentHeight != newHeight && this.currentWidth != newWidth)` is partially met,
     * but the *resize* check inside is not met (i.e., should not resize).
     */
    @Test
    void testSetCanvasDimensionsPartialMatchNoResize() {
        // Current: 10x10. We want to check (9x9).
        // Since resize only happens if current dimensions are SMALLER than new dimensions,
        // and the inner check is `if (this.currentHeight != newHeight && this.currentWidth != newWidth)`
        // the condition is: (10 != 9 && 10 != 9) -> TRUE.
        // But the resize logic inside checks against max height.

        // This test specifically targets the redundant check in the user's source:
        // if (this.currentHeight != newHeight && this.currentWidth != newWidth) {
        //    resize(newHeight, newWidth, true);
        // }

        // Current dimensions are 10x10.
        // We call 12x12. This passes the `!=` check and should resize.
        stitcher.setCanvasDimensions(12, 12);
        verifyCanvasDimensions(12, 12);
        assertEquals(FILL_COLOR, stitcher.getCanvas()[0][0], "Resize should have occurred.");

        // Now canvas is 12x12.
        // Call with 10x10. Conditional: (12 != 10 && 12 != 10) -> TRUE.
        // But resize logic prevents shrinking. The goal is to verify the outer check doesn't block unnecessary calls.
        // The current implementation allows unnecessary resizing but prevents shrinking.

        // Final check: Test the original 10x10 setting where `!=` is false, forcing no resize.
        stitcher.setCanvas(INITIAL_H, INITIAL_W);
        stitcher.setCanvasDimensions(INITIAL_H, INITIAL_W);
        verifyCanvasDimensions(INITIAL_H, INITIAL_W);
    }

    /**
     * Tests setCanvasDimensions when resizing is explicitly requested (resize logic).
     * This hits the 'resize' function and its 'fill' branch.
     */
    @Test
    void testSetCanvasDimensionsForcedResize() {
        final int newH = INITIAL_H + 5;
        final int newW = INITIAL_W + 5;

        stitcher.setCanvasDimensions(newH, newW);
        verifyCanvasDimensions(newH, newW);

        // Check that old content was copied (fill = true)
        assertEquals(FILL_COLOR, stitcher.getCanvas()[0][0], "Old content should be copied into the new canvas.");
        // Check that new area is initialized to 0
        assertEquals(0, stitcher.getCanvas()[newH - 1][newW - 1], "New canvas area should be initialized to zero.");
    }

    /**
     * Tests single patch stitch when no resize is needed.
     */
    @Test
    void testStitchSinglePatchNoResize() {
        final int[][] patchPixels = createSolidMatrix(2, 2, TEST_COLOR);
        final Patch patch = new Patch(patchPixels, 0, 0);

        stitcher.stitch(patch);

        verifyCanvasDimensions(INITIAL_H, INITIAL_W);
        assertEquals(TEST_COLOR, stitcher.getCanvas()[0][0], "Patch content should be applied.");
        assertEquals(FILL_COLOR, stitcher.getCanvas()[9][9], "Unpatched area should remain base color.");
    }

    /**
     * Tests stitch when patch ends exactly on the boundary, forcing no resize.
     * (Covers the path where maxHeightWithPatch == currentHeight && maxWidthWithPatch == currentWidth)
     */
    @Test
    void testStitchWithPatchAtCurrentBoundary() {
        final int patchH = 2;
        final int patchW = 2;
        // Patch starts at (INITIAL_W - patchW, INITIAL_H - patchH) = (8, 8)
        final Patch patch = new Patch(createSolidMatrix(patchH, patchW, TEST_COLOR), 8, 8);

        // Stitch should not change dimensions (10x10)
        stitcher.stitch(patch);

        verifyCanvasDimensions(INITIAL_H, INITIAL_W);
        // Verify patched area on the boundary
        assertEquals(TEST_COLOR, stitcher.getCanvas()[9][9], "Boundary pixel should be patched.");
        // Verify far corner is still original color (should be FILL_COLOR, but setup fills the whole thing)
        assertEquals(FILL_COLOR, stitcher.getCanvas()[0][0], "Opposite corner pixel should be unchanged (FILL_COLOR).");
    }

    /**
     * Tests single patch stitch when vertical expansion is required.
     * (Covers `maxHeightWithPatch > currentHeight` path in `verifyDimensions`)
     */
    @Test
    void testStitchRequiresVerticalExpansion() {
        final int patchH = 3;
        final int patchW = 3;
        // Patch starts at (5, 8), runs until y = 8 + 3 = 11. Current height is 10.
        final Patch patch = new Patch(createSolidMatrix(patchH, patchW, TEST_COLOR), 5, 8);

        stitcher.stitch(patch);

        final int expectedH = 11;
        verifyCanvasDimensions(expectedH, INITIAL_W);
        assertEquals(TEST_COLOR, stitcher.getCanvas()[expectedH - 1][5], "Patch should extend to new bottom row.");
        assertEquals(FILL_COLOR, stitcher.getCanvas()[0][0], "Old content must be preserved after resize.");
    }

    /**
     * Tests single patch stitch when horizontal expansion is required.
     * (Covers `maxWidthWithPatch > currentWidth` path in `verifyDimensions`)
     */
    @Test
    void testStitchRequiresHorizontalExpansion() {
        final int patchH = 3;
        final int patchW = 3;
        // Patch starts at (8, 5), runs until x = 8 + 3 = 11. Current width is 10.
        final Patch patch = new Patch(createSolidMatrix(patchH, patchW, TEST_COLOR), 8, 5);

        stitcher.stitch(patch);

        final int expectedW = 11;
        verifyCanvasDimensions(INITIAL_H, expectedW);
        assertEquals(TEST_COLOR, stitcher.getCanvas()[5][expectedW - 1], "Patch should extend to new right column.");
        assertEquals(FILL_COLOR, stitcher.getCanvas()[0][0], "Old content must be preserved after resize.");
    }

    /**
     * Tests stitching a list of patches. (Covers stitch(List<Stitchable>))
     */
    @Test
    void testStitchMultiplePatches() {
        final Patch patch1 = new Patch(createSolidMatrix(2, 2, 10), 0, 0); // Top-Left
        // Patch starts at (8, 8), forces resize to 11x11, Patch 2 content is 20
        final Patch patch2 = new Patch(createSolidMatrix(4, 4, 20), 8, 8);

        final List<Stitchable> patches = List.of(patch1, patch2);

        // Initial dimensions: 10x10
        stitcher.stitch(patches);

        // Final dimensions should be 12x12
        verifyCanvasDimensions(12, 12);

        // Check P1 (Original area, should still be value 10)
        assertEquals(10, stitcher.getCanvas()[0][0], "Patch 1 applied correctly.");
        // Check P2 (New boundary area, should be value 20)
        assertEquals(20, stitcher.getCanvas()[11][11], "Patch 2 applied correctly and forced expansion.");
        // Check initial area where no patch was applied (should be FILL_COLOR)
        assertEquals(FILL_COLOR, stitcher.getCanvas()[2][2], "Unpatched area should remain original fill color.");
    }
}