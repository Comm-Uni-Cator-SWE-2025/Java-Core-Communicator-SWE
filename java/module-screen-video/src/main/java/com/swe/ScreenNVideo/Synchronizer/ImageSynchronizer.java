package com.swe.ScreenNVideo.Synchronizer;

import com.swe.ScreenNVideo.Codec.Codec;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.PatchGenerator.ImageStitcher;
import com.swe.ScreenNVideo.PatchGenerator.Patch;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Synchronizer to synchronize the image from the patches.
 */
public class ImageSynchronizer {
    /**
     * The previous image.
     * The new patch will be stitched on this image.
     */
    private int[][] previousImage;
    /**
     * The codec to decode the patches.
     */
    private final Codec videoCodec;
    /**
     * The image stitcher to stitch the patches.
     */
    private final ImageStitcher imageStitcher;

    /**
     * The next feed number we expect to recieve in correct order.
     * Used to ensure patches are applied sequentially.
     */
    public int expectedfeedNumber;

    /**
     * Min-heap storing out-of-order feed packets.
     */
    private final PriorityQueue<FeedData> heap;

    /**
     * @return the min-heap of {@link FeedData} items used for ordering incoming packets.
     */
    public PriorityQueue<FeedData> getHeap() {
        return this.heap;
    }

    /**
     * Create a new image synchronizer.
     * @param codec the codec to decode the patches.
     */
    public ImageSynchronizer(final Codec codec) {
        this.videoCodec = codec;
        this.imageStitcher = new ImageStitcher();
        previousImage = null;
        this.expectedfeedNumber = 0;
        this.heap = new PriorityQueue<>((a, b) -> Integer.compare(a.getFeedNumber(), b.getFeedNumber()));
    }

    /**
     * Synchronize the image from the patches.
     * @param compressedPatches the patches to synchronize the image.
     * @return the image.
     */
    public int[][] synchronize(final int newHeight, final int newWidth, final List<CompressedPatch> compressedPatches) {
        if (previousImage != null) {
            imageStitcher.setCanvas(previousImage);
        } else {
            imageStitcher.resetCanvas();
        }

        imageStitcher.setCanvasDimensions(newHeight, newWidth);

        for (CompressedPatch compressedPatch : compressedPatches) {
            final int[][] decodedImage = videoCodec.decode(compressedPatch.data());
            final Patch patch = new Patch(decodedImage, compressedPatch.x(), compressedPatch.y());
            imageStitcher.stitch(patch);
//            break;
        }
        previousImage = imageStitcher.getCanvas();
        return previousImage;
    }

}
