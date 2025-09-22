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

    public ImageSynchronizer(final Codec codec, final IHasher hasherArgs, final ImageStitcher imageStitcherArgs) {
        this.videoCodec = codec;
        this.hasher = hasherArgs;
        this.imageStitcher = imageStitcherArgs;
        previousImage = null;
    }

    public int[][] synchronize(final List<CompressedPatch> compressedPatches, int[][] feed) {
        if (previousImage != null) {
            System.out.println("Using previous Image");
            imageStitcher.setCanvas(previousImage);
        } else {
            imageStitcher.setCanvas(1080, 1920);
        }
        for (CompressedPatch compressedPatch : compressedPatches) {
            final int[][] decodedImage = videoCodec.decode(compressedPatch.data());
//            int[][] dImage = new int[64][64];
//            for (int i = 0; i < 64; i++) {
//                for (int j = 0; j < 64; j++) {
//                    dImage[i][j] = feed[i][j];
//                    if (decodedImage[i][j] != feed[i][j]) {
//                        throw new RuntimeException("Mis Match");
//                    }
//                }
//            }
            final Patch patch = new Patch(decodedImage, compressedPatch.x(), compressedPatch.y());
            imageStitcher.stitch(patch);
            break;
        }
        previousImage = imageStitcher.getCanvas();
        return previousImage;
    }

}
