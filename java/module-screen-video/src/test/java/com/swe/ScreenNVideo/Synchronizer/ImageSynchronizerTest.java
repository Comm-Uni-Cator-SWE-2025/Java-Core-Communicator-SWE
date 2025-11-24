/**
 * Contributed by @chirag9528
 */
package com.swe.ScreenNVideo.Synchronizer;

import com.swe.ScreenNVideo.Codec.Codec;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.PatchGenerator.ImageStitcher;
import com.swe.ScreenNVideo.PatchGenerator.Patch;
import com.swe.ScreenNVideo.PatchGenerator.Stitchable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * Comprehensive test suite for ImageSynchronizer class.
 * Tests synchronization logic, stitching delegation, and state management.
 */
public class ImageSynchronizerTest {
    /**
     * Dummy height for test images.
     */
    private static final int TEST_HEIGHT = 100;

    /**
     * Dummy width for test images.
     */
    private static final int TEST_WIDTH = 100;

    /**
     * Mock for the Video Codec.
     */
    @Mock
    private Codec mockCodec;

    /**
     * Mock for the Image Stitcher.
     * Needs to be injected via reflection as it is created in constructor.
     */
    @Mock
    private ImageStitcher mockStitcher;

    /**
     * Instance under test.
     */
    private ImageSynchronizer synchronizer;

    /**
     * AutoCloseable for Mockito resources.
     */
    private AutoCloseable closeable;

    /**
     * Sets up the test environment.
     */
    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        synchronizer = new ImageSynchronizer(mockCodec);

        // Inject the mock ImageStitcher using Reflection
        final Field stitcherField = ImageSynchronizer.class.getDeclaredField("imageStitcher");
        stitcherField.setAccessible(true);
        stitcherField.set(synchronizer, mockStitcher);
    }

    /**
     * Tears down the test environment.
     */
    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Tests that the constructor initializes fields correctly.
     */
    @Test
    public void testConstructorInitialization() {
        assertNotNull(synchronizer.getHeap(), "Heap should be initialized");
        assertEquals(0, synchronizer.getExpectedFeedNumber(), "Expected feed number should start at 0");
        assertEquals(0, synchronizer.getPrevSend(), "Prev send should start at 0");
    }

    /**
     * Tests getters and setters for PrevSend and DataReceived.
     */
    @Test
    public void testGettersAndSetters() throws InterruptedException {
        // Test DataReceived
        synchronizer.setDataReceived(500L);
        assertEquals(500L, synchronizer.getDataReceived());

        final long before = System.currentTimeMillis();
        Thread.sleep(1); // Ensure time moves
        synchronizer.setPrevSend();
        final long after = System.currentTimeMillis();

        assertTrue(synchronizer.getPrevSend() >= before, "PrevSend should be >= before time");
        assertTrue(synchronizer.getPrevSend() <= after, "PrevSend should be <= after time");
    }

    /**
     * Tests setting the expected feed number.
     */
    @Test
    public void testSetExpectedFeedNumber() {
        final int expected = 5;
        synchronizer.setExpectedFeedNumber(expected);
        assertEquals(expected, synchronizer.getExpectedFeedNumber());
    }

    /**
     * Tests logic when synchronizing the very first frame (Previous Image is null).
     * Expects resetCanvas to be called.
     */
    @Test
    public void testSynchronizeFirstFrame() {
        // Setup Patch Data
        final CompressedPatch mockPatch = mock(CompressedPatch.class);
        final byte[] dummyData = new byte[]{1, 2, 3};
        when(mockPatch.data()).thenReturn(dummyData);
        when(mockPatch.x()).thenReturn(0);
        when(mockPatch.y()).thenReturn(0);

        final List<CompressedPatch> patches = Collections.singletonList(mockPatch);

        // Setup Codec Behavior
        final int[][] decodedPatch = new int[][]{{1}};
        when(mockCodec.decode(eq(dummyData), anyBoolean())).thenReturn(decodedPatch);

        // Setup Stitcher Result
        final int[][] finalCanvas = new int[TEST_HEIGHT][TEST_WIDTH];
        when(mockStitcher.getCanvas()).thenReturn(finalCanvas);

        // Execute
        final int[][] result = synchronizer.synchronize(TEST_HEIGHT, TEST_WIDTH, patches, true);

        // Verify
        assertArrayEquals(finalCanvas, result);

        // Since previousImage was null, it should have reset the canvas
        verify(mockStitcher).resetCanvas();
        verify(mockStitcher).setCanvasDimensions(TEST_HEIGHT, TEST_WIDTH);

        // Verify Decoding and Stitching
        verify(mockCodec).decode(eq(dummyData), eq(true));
        verify(mockStitcher).stitch(any(Patch.class));
    }

    /**
     * Tests logic when synchronizing subsequent frames (Previous Image exists).
     * Expects setCanvas(previousImage) to be called.
     */
    @Test
    public void testSynchronizeSubsequentFrames() throws Exception {
        // Prime the synchronizer with a "previous image" using reflection
        final int[][] prevImage = new int[TEST_HEIGHT][TEST_WIDTH];
        prevImage[0][0] = 999; // Marker
        final Field prevImageField = ImageSynchronizer.class.getDeclaredField("previousImage");
        prevImageField.setAccessible(true);
        prevImageField.set(synchronizer, prevImage);

        // Setup Patch Data
        final CompressedPatch mockPatch = mock(CompressedPatch.class);
        final byte[] dummyData = new byte[]{9};
        when(mockPatch.data()).thenReturn(dummyData);
        final List<CompressedPatch> patches = Collections.singletonList(mockPatch);

        // Setup Dependencies
        when(mockCodec.decode(any(), anyBoolean())).thenReturn(new int[][]{{1}});
        final int[][] newCanvas = new int[TEST_HEIGHT][TEST_WIDTH];
        when(mockStitcher.getCanvas()).thenReturn(newCanvas);

        // Execute
        final int[][] result = synchronizer.synchronize(TEST_HEIGHT, TEST_WIDTH, patches, false);

        // Verify
        assertArrayEquals(newCanvas, result);

        // Since previousImage existed, setCanvas should be called, NOT resetCanvas
        verify(mockStitcher).setCanvas(eq(prevImage));
        verify(mockStitcher, times(0)).resetCanvas();

        // Verify dimension update
        verify(mockStitcher).setCanvasDimensions(TEST_HEIGHT, TEST_WIDTH);
    }

    /**
     * Tests handling of empty patch list.
     * Should still update dimensions and return canvas.
     */
    @Test
    public void testSynchronizeEmptyPatches() {
        final List<CompressedPatch> emptyPatches = Collections.emptyList();
        final int[][] emptyCanvas = new int[TEST_HEIGHT][TEST_WIDTH];
        when(mockStitcher.getCanvas()).thenReturn(emptyCanvas);

        final int[][] result = synchronizer.synchronize(TEST_HEIGHT, TEST_WIDTH, emptyPatches, true);

        assertArrayEquals(emptyCanvas, result);
        verify(mockCodec, times(0)).decode(any(), anyBoolean());
        verify(mockStitcher, times(0)).stitch((Stitchable) any());
    }

    /**
     * Tests synchronize when imageStitcher.getCanvas() returns null
     */
    @Test
    public void testSynchronizeEmptyPatches_AndGotNullImage() {
        final List<CompressedPatch> emptyPatches = Collections.emptyList();
        when(mockStitcher.getCanvas()).thenReturn(null);

        final int[][] result = synchronizer.synchronize(TEST_HEIGHT, TEST_WIDTH, emptyPatches, true);

        assertArrayEquals(null, result);
        verify(mockCodec, times(0)).decode(any(), anyBoolean());
        verify(mockStitcher, times(0)).stitch((Stitchable) any());
    }
}
