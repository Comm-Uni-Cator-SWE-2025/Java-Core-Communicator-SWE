package com.swe.ScreenNVideo.Synchronizer;

import com.swe.ScreenNVideo.Codec.Codec;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.PatchGenerator.IHasher;
import com.swe.ScreenNVideo.PatchGenerator.ImageStitcher;
import com.swe.ScreenNVideo.PatchGenerator.Patch;

import java.util.List;

public class ImageSynchronizer {
    int[][] previousImage;
    final Codec videoCodec;
    final IHasher hasher;
    final ImageStitcher imageStitcher;

    public ImageSynchronizer(final Codec codec, final IHasher hasherArgs) {
        this.videoCodec = codec;
        this.hasher = hasherArgs;
        this.imageStitcher = new ImageStitcher();
        previousImage = null;
    }

    public int[][] synchronize(final List<CompressedPatch> compressedPatches) {
        if (previousImage != null) {
            imageStitcher.setCanvas(previousImage);
        } else {
            imageStitcher.resetCanvas();
        }
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
