package com.swe.ScreenNVideo.Synchronizer;

import com.swe.ScreenNVideo.Codec.Codec;
import com.swe.ScreenNVideo.Codec.JpegCodec;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.PatchGenerator.Hasher;
import com.swe.ScreenNVideo.PatchGenerator.IHasher;
import com.swe.ScreenNVideo.PatchGenerator.ImageStitcher;
import com.swe.ScreenNVideo.PatchGenerator.Patch;
import com.swe.ScreenNVideo.Utils;

import java.util.List;

public class ImageSynchronizer {
    int[][] previousImage;
    final Codec videoCodec;
    final IHasher hasher;
    final ImageStitcher imageStitcher;

    public ImageSynchronizer(final Codec codec, final IHasher hasherArgs, final ImageStitcher imageStitcherArgs) {
        this.videoCodec = codec;
        this.hasher = hasherArgs;
        this.imageStitcher = imageStitcherArgs;
        previousImage = null;
    }

    public int[][] synchronize(final List<CompressedPatch> compressedPatches) {
        if (previousImage != null) {
            imageStitcher.setCanvas(previousImage);
        } else {
            imageStitcher.resetCanvas();
        }
        compressedPatches.forEach(compressedPatch -> {
            final int[][] decodedImage = videoCodec.decode(compressedPatch.getData());
            final Patch patch = new Patch(decodedImage, compressedPatch.getX(), compressedPatch.getY());
            imageStitcher.stitch(patch);
        });
        return imageStitcher.getCanvas();
    }

}
