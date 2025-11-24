package com.swe.ScreenNVideo.PatchGenerator;

import com.swe.ScreenNVideo.Codec.Codec;
import com.swe.ScreenNVideo.Model.FeedPatch;
import com.swe.ScreenNVideo.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PacketGeneratorTest {

    private Codec codec;
    private IHasher hasher;
    private PacketGenerator generator;

    @BeforeEach
    void setup() {
        codec = mock(Codec.class);
        hasher = mock(IHasher.class);
        generator = new PacketGenerator(codec, hasher);
    }

    // ---------------------------------------------------------
    // TEST 1 : FULL IMAGE ENCODE TEST
    // ---------------------------------------------------------
    @Test
    void testGenerateFullImage() {
        int[][] img = new int[4][4];

        byte[] compressed = new byte[]{1,2,3};
        byte[] raw = new byte[]{4,5,6};

        when(codec.encode(img, 0, 0, 4, 4))
                .thenReturn(List.of(compressed, raw));

        FeedPatch fp = generator.generateFullImage(img);

        assertEquals(1, fp.compressedPatches().size());
        assertEquals(1, fp.unCompressedPatches().size());

        CompressedPatch cp = fp.compressedPatches().get(0);
        assertEquals(0, cp.x());
        assertEquals(0, cp.y());
        assertEquals(4, cp.width());
        assertEquals(4, cp.height());
        assertArrayEquals(compressed, cp.data());
    }

    // ---------------------------------------------------------
    // TEST 2 : FIRST FRAME = ALL TILES DIRTY
    // ---------------------------------------------------------
    @Test
    void testGeneratePacketsFirstFrameAllTilesDirty() {
        // 32x32 tile → full frame becomes 1 tile
        int[][] img = new int[32][32];

        // force hasher to always return 111
        when(hasher.hash(any(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(111L);

        byte[] compressed = new byte[]{9};
        byte[] raw = new byte[]{8};

        when(codec.encode(any(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(compressed, raw));

        FeedPatch out = generator.generatePackets(img);

        assertEquals(1, out.compressedPatches().size());
        assertEquals(1, out.unCompressedPatches().size());
    }

    // ---------------------------------------------------------
    // TEST 3 : SECOND FRAME NO CHANGE → MUST RETURN EMPTY LIST
    // ---------------------------------------------------------
    @Test
    void testGeneratePacketsNoChange() {
        int[][] img = new int[32][32];

        // Mock compressor
        when(codec.encode(any(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(new byte[]{1}, new byte[]{2}));

        // First frame: hash = 10
        when(hasher.hash(any(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(10L);

        generator.generatePackets(img); // initializes prevHashes

        // Second frame: same hash
        when(hasher.hash(any(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(10L);

        FeedPatch out = generator.generatePackets(img);

        assertEquals(0, out.compressedPatches().size());
        assertEquals(0, out.unCompressedPatches().size());
    }


    // ---------------------------------------------------------
    // TEST 4 : ONLY ONE TILE CHANGES
    // ---------------------------------------------------------
    @Test
    void testGeneratePacketsOneTileChanged() {
        int[][] img = new int[64][64]; // 2x2 tiles of 32x32

        // Stub exact calls for 32x32 tile hashes in correct order

        // Tile (0,0): first frame = 10, second frame = 99
        when(hasher.hash(any(), eq(0), eq(0), eq(32), eq(32)))
                .thenReturn(10L, 99L);

        // Tile (32,0)
        when(hasher.hash(any(), eq(32), eq(0), eq(32), eq(32)))
                .thenReturn(10L, 10L);

        // Tile (0,32)
        when(hasher.hash(any(), eq(0), eq(32), eq(32), eq(32)))
                .thenReturn(10L, 10L);

        // Tile (32,32)
        when(hasher.hash(any(), eq(32), eq(32), eq(32), eq(32)))
                .thenReturn(10L, 10L);

        // Mock encode for 32x32 tiles only
        byte[] compressed = new byte[]{55};
        byte[] raw = new byte[]{44};

        when(codec.encode(any(), anyInt(), anyInt(), eq(32), eq(32)))
                .thenReturn(List.of(compressed, raw));

        // First frame initializes prevHashes
        generator.generatePackets(img);

        // Second frame: only (0,0) changed
        FeedPatch fp = generator.generatePackets(img);

        assertEquals(1, fp.compressedPatches().size());
        assertEquals(1, fp.unCompressedPatches().size());

        CompressedPatch cp = fp.compressedPatches().get(0);
        assertEquals(0, cp.x());
        assertEquals(0, cp.y());
    }



    // ---------------------------------------------------------
    // TEST 5 : PREV HASH RESIZE
    // ---------------------------------------------------------
    @Test
    void testGeneratePacketsPrevHashResize() {
        int[][] img1 = new int[32][32];   // 1 tile
        int[][] img2 = new int[64][64];   // 4 tiles

        // --- FIRST FRAME: 1 TILE ---
        // Only this one is called in the first frame
        when(hasher.hash(eq(img1), eq(0), eq(0), anyInt(), anyInt()))
                .thenReturn(1L);

        // --- SECOND FRAME: 4 TILES ---
        when(hasher.hash(eq(img2), eq(0), eq(0), anyInt(), anyInt()))
                .thenReturn(2L);

        when(hasher.hash(eq(img2), eq(32), eq(0), anyInt(), anyInt()))
                .thenReturn(3L);

        when(hasher.hash(eq(img2), eq(0), eq(32), anyInt(), anyInt()))
                .thenReturn(4L);

        when(hasher.hash(eq(img2), eq(32), eq(32), anyInt(), anyInt()))
                .thenReturn(5L);

        when(codec.encode(any(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(new byte[]{1}, new byte[]{2}));

        // First frame
        generator.generatePackets(img1);

        // Second frame
        FeedPatch fp = generator.generatePackets(img2);

        assertEquals(4, fp.compressedPatches().size());
    }

    // ---------------------------------------------------------
    // TEST 6 : PREV HASH RESIZE WITH 1 FRAME UNCHANGED
    // ---------------------------------------------------------
    @Test
    void testGeneratePacketsPrevHashResizeOneFrameUnchanged() {
        int[][] img1 = new int[32][32];   // 1 tile
        int[][] img2 = new int[64][64];   // 4 tiles

        // --- FIRST FRAME: 1 TILE ---
        // Only this one is called in the first frame
        when(hasher.hash(eq(img1), eq(0), eq(0), anyInt(), anyInt()))
                .thenReturn(1L);

        // --- SECOND FRAME: 4 TILES ---
        when(hasher.hash(eq(img2), eq(0), eq(0), anyInt(), anyInt()))
                .thenReturn(1L);

        when(hasher.hash(eq(img2), eq(32), eq(0), anyInt(), anyInt()))
                .thenReturn(3L);

        when(hasher.hash(eq(img2), eq(0), eq(32), anyInt(), anyInt()))
                .thenReturn(4L);

        when(hasher.hash(eq(img2), eq(32), eq(32), anyInt(), anyInt()))
                .thenReturn(5L);

        when(codec.encode(any(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(new byte[]{1}, new byte[]{2}));

        // First frame
        generator.generatePackets(img1);

        // Second frame
        FeedPatch fp = generator.generatePackets(img2);

        assertEquals(3, fp.compressedPatches().size());
    }

    @Test
    void testResizeOnlyYDimension() {
        // FIRST FRAME: 64x32 → tiles = 2x1
        int[][] img1 = new int[32][64];

        when(hasher.hash(eq(img1), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(1L);

        when(codec.encode(any(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(new byte[]{1}, new byte[]{2}));

        generator.generatePackets(img1); // initializes prevHashes = [2][1]


        // SECOND FRAME: 64x64 → tiles = 2x2
        int[][] img2 = new int[64][64];

        when(hasher.hash(eq(img2), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(2L);

        FeedPatch fp = generator.generatePackets(img2);

        // all 4 tiles are dirty
        assertEquals(4, fp.compressedPatches().size());
    }

}
