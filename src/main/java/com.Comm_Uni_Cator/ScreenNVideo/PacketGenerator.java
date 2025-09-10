package com.Comm_Uni_Cator.ScreenNVideo;

import java.util.ArrayList;
import java.util.List;

public class PacketGenerator {

    private static final int TILE_SIZE = 64; // default tile size
    private ICompressor compressor;
    private IHasher hasher;

    // Values/Cache of last hashes per tile (grid of tiles, each storing long hash)
    private long[][] prevHashes;

    public PacketGenerator(ICompressor compressor_arg, IHasher hasher_arg) {
        this.compressor = compressor_arg;
        this.hasher = hasher_arg;
    }

    /**
     * Split frames into tiles, compare hashes, compress dirty tiles
     * @param curr is the image frame (int[width][height][3] RGB array)
     * @return list of compressed patches
     */
    public List<CompressedPatch> generatePackets(int[][][] curr) {
        int width = curr.length;
        int height = curr[0].length;

        // Tile grid size
        int tilesX = (int) Math.ceil((double) width / TILE_SIZE);
        int tilesY = (int) Math.ceil((double) height / TILE_SIZE);

        // Initialize hash cache if first frame
        if (prevHashes == null) {
            prevHashes = new long[tilesX][tilesY];
        }

        List<CompressedPatch> patches = new ArrayList<>();

        for (int ty = 0; ty < tilesY; ty++) {
            for (int tx = 0; tx < tilesX; tx++) {
                int x = tx * TILE_SIZE;
                int y = ty * TILE_SIZE;
                int w = Math.min(TILE_SIZE, width - x);
                int h = Math.min(TILE_SIZE, height - y);

                long currHash = hasher.hash(curr, x, y, w, h);
                if (currHash != prevHashes[tx][ty]) {
                    String compressedString = compressor.compress(curr, x, y, w, h);
                    patches.add(new CompressedPatch(x, y, w, h, compressedString));
                    prevHashes[tx][ty] = currHash;
                }
            }
        }
        return patches;
    }

}
