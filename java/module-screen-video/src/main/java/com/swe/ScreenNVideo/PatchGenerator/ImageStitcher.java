package com.swe.ScreenNVideo.PatchGenerator;

import java.util.List;

/**
 * Handles stitching multiple patches together into a single canvas.
 *
 * @param canvas The target canvas for image stitching.
 */
public record ImageStitcher(int[][][] canvas) {

    /**
     * Creates a new {@code ImageStitcher}.
     *
     * @param canvas is the target canvas
     */
    public ImageStitcher {
    }

    /**
     * Stitches the provided patches list onto the canvas.
     *
     * @param patches the list of patches to apply
     */
    public void stitch(final List<Stitchable> patches) {
        for (Stitchable patch : patches) {
            patch.applyOn(canvas);
        }
    }

    public void stitch(final Stitchable patch) {
        patch.applyOn(canvas);
    }

    /**
     * Returns the final stitched canvas.
     *
     * @return the canvas
     */
    @Override
    public int[][][] canvas() {
        return canvas;
    }
}
