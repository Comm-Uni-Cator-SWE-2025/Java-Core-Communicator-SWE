/**
 * Contributed by @alonot.
 */

package com.swe.ScreenNVideo.Model;

import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;

import java.util.List;

/**
 * Feed patch model for the ScreenNVideo module.
 * @param compressedPatches The compressed patches.
 * @param unCompressedPatches The uncompressed patches.
 */
public record FeedPatch(List<CompressedPatch> compressedPatches, List<CompressedPatch> unCompressedPatches) {
}
