/**
 * Contributed by @chirag9528
 */
package com.swe.ScreenNVideo;

import com.swe.ScreenNVideo.Capture.BackgroundCaptureManager;
import com.swe.ScreenNVideo.Model.Feed;
import com.swe.core.Context;
import com.swe.core.RPC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive test suite for VideoComponents class
 * Tests video/audio capture logic, FPS throttling, and RPC interactions.
 */
public class VideoComponentsTest {

    /**
     * FPS for testing.
     */
    private static final int TEST_FPS = 30;

    /**
     * Port value for testing.
     */
    private static final int TEST_PORT = 8080;

    /**
     * Dimension for dummy feed frames.
     */
    private static final int FEED_DIMENSION = 16;

    /**
     * Mock for CaptureComponents.
     */
    @Mock
    private CaptureComponents mockCaptureComponents;

    /**
     * Mock for BackgroundCaptureManager.
     */
    @Mock
    private BackgroundCaptureManager mockBgManager;

    /**
     * Mock for RPC.
     */
    @Mock
    private RPC mockRpc;

    /**
     * Instance of VideoComponents under test.
     */
    private VideoComponents videoComponents;

    /**
     * AutoCloseable for Mockito mocks.
     */
    private AutoCloseable closeable;

    /**
     * Sets up test fixture before each test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        // Inject Mock RPC into Context Singleton using Reflection
        final Context context = Context.getInstance();
        final Field rpcField = context.getClass().getDeclaredField("rpc");
        rpcField.setAccessible(true);
        rpcField.set(context, mockRpc);

        // Initialize VideoComponents
        videoComponents = new VideoComponents(TEST_FPS, TEST_PORT, mockCaptureComponents, mockBgManager);
    }

    /**
     * Tears down the test fixture.
     */
    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Tests that constructor initializes successfully.
     */
    @Test
    public void testConstructorCreatesInstance() {
        assertNotNull(videoComponents);
        assertNotNull(videoComponents.getVideoCodec());
    }

    /**
     * Tests isVideoCaptureOn when videoCapture is on
     */
    @Test
    public void testIsVideoCaptureOn_On() {
        when(mockCaptureComponents.isVideoCaptureOn()).thenReturn(true);
        assertTrue(videoComponents.isVideoCaptureOn());
        verify(mockCaptureComponents).isVideoCaptureOn();
    }

    /**
     * Tests isVideoCaptureOn when videoCapture is Off
     */
    @Test
    public void testIsVideoCaptureOn_Off() {
        when(mockCaptureComponents.isVideoCaptureOn()).thenReturn(false);
        assertFalse(videoComponents.isVideoCaptureOn());
        verify(mockCaptureComponents).isVideoCaptureOn();
    }

    /**
     * Tests isScreenCaptureOn when screenCapture is On
     */
    @Test
    public void testIsScreenCaptureOn_On() {
        when(mockCaptureComponents.isScreenCaptureOn()).thenReturn(true);
        assertTrue(videoComponents.isScreenCaptureOn());
        verify(mockCaptureComponents).isScreenCaptureOn();
    }

    /**
     * Tests isScreenCaptureOn when screenCapture is Off
     */
    @Test
    public void testIsScreenCaptureOn_Off() {
        when(mockCaptureComponents.isScreenCaptureOn()).thenReturn(false);
        assertFalse(videoComponents.isScreenCaptureOn());
        verify(mockCaptureComponents).isScreenCaptureOn();
    }

    /**
     * Tests captureAudio returns null when no audio feed is available.
     */
    @Test
    public void testCaptureAudio_NoFeed() {
        when(mockCaptureComponents.getAudioFeed()).thenReturn(null);
        assertNull(videoComponents.captureAudio());
    }

    /**
     * Tests captureAudio encodes valid audio data.
     */
    @Test
    public void testCaptureAudio_ValidFeed() {
        final byte[] dummyAudio = new byte[]{1, 2, 3, 4, 5};
        when(mockCaptureComponents.getAudioFeed()).thenReturn(dummyAudio);

        final byte[] result = videoComponents.captureAudio();
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(mockCaptureComponents).getAudioFeed();
    }

    /**
     * Tests captureScreenNVideo returns null when called too frequently (FPS throttling).
     */
    @Test
    public void testCaptureScreenNVideo_Throttling() {
        final int[][] dummyFeed = new int[FEED_DIMENSION][FEED_DIMENSION];
        when(mockCaptureComponents.getFeed()).thenReturn(dummyFeed);

        // First call should succeed (initial start time is 0)
        videoComponents.captureScreenNVideo();

        // Immediate second call should be throttled because diff < timeDelay
        final Feed throttledFeed = videoComponents.captureScreenNVideo();
        assertNull(throttledFeed);
    }

    /**
     * Tests captureScreenNVideo successfully processes a frame.
     */
    @Test
    public void testCaptureScreenNVideo_Success() {
        final int[][] dummyFeed = createDummyFeed(FEED_DIMENSION);
        when(mockCaptureComponents.getFeed()).thenReturn(dummyFeed);

        final Feed result = videoComponents.captureScreenNVideo();
        assertNotNull(result, "Feed should not be null for valid capture");
        assertNotNull(result.compressedFeed(), "Compressed Feed should exists.");
        assertNotNull(result.unCompressedFeed(), "UnCompressed Feed should exists.");

        assertEquals(dummyFeed, videoComponents.getFeed());
    }

    /**
     * Tests captureScreenNVideo handles null feed from CaptureComponents (Stop Share).
     */
    @Test
    public void testCaptureScreenNVideo_StopShare() throws Exception {
        final int[][] dummyFeed = createDummyFeed(FEED_DIMENSION);
        when(mockCaptureComponents.getFeed()).thenReturn(dummyFeed).thenReturn(null);

        // First call to set the feed
        videoComponents.captureScreenNVideo();
        assertNotNull(videoComponents.getFeed());

        // Wait to avoid Throttle
        Thread.sleep(50);

        // Second call returns null (Stop Share scenerio)
        final Feed result = videoComponents.captureScreenNVideo();

        assertNull(result);
        assertNull(videoComponents.getFeed(), "Feed should be reset to null");

        // Verify that STOP_SHARE RPC was called
        verify(mockRpc).call(eq(Utils.STOP_SHARE), any(byte[].class));
    }

    /**
     * Tests captureFullImage returns null if no feed has been captured yet.
     */
    @Test
    public void testCaptureFullImage_ReturnsNullInitially() {
        assertNull(videoComponents.captureFullImage());
    }

    /**
     * Tests captureFullImage returns valid patches when feed exists.
     */
    @Test
    public void testCaptureFullImage_WithExistingFeed() {
        // Setup state with a feed
        final int[][] dummyFeed = createDummyFeed(FEED_DIMENSION);
        when(mockCaptureComponents.getFeed()).thenReturn(dummyFeed);
        videoComponents.captureScreenNVideo();

        final Feed fullFeed = videoComponents.captureFullImage();

        assertNotNull(fullFeed, "Full image feed should not be null");
        assertNotNull(fullFeed.compressedFeed(), "Compressed Feed should exists");
        assertNotNull(fullFeed.unCompressedFeed(), "UnCompressed feed should exists");
    }

    /**
     * Helper to create a dummy 2D array feed.
     *
     * @param size dimension
     * @return 2D int array
     */
    private int[][] createDummyFeed(final int size) {
        final int[][] feed = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                feed[i][j] = 0xFFFFFFFF; // White pixel
            }
        }
        return feed;
    }
}
