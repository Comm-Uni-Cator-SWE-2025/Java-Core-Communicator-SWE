/**
 * Contributed by @chirag9528
 */
package com.swe.ScreenNVideo;

import com.swe.ScreenNVideo.Capture.AudioCapture;
import com.swe.ScreenNVideo.Codec.ImageScaler;
import com.swe.ScreenNVideo.Model.IPPacket;
import com.swe.ScreenNVideo.PatchGenerator.ImageStitcher;
import com.swe.ScreenNVideo.PatchGenerator.Patch;
import com.swe.ScreenNVideo.PatchGenerator.Stitchable;
import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.AbstractNetworking;
import com.swe.networking.ModuleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for CaptureComponents class.
 * Tests capture flags, feed generation, audio capture, and RPC handlers.
 */
public class CaptureComponentsTest {

    /**
     * Dummy port for testing.
     */
    private static final int TEST_PORT = 8080;

    /**
     * Dummy local IP.
     */
    private static final String LOCAL_IP = "127.0.0.1";

    /**
     * Dummy remote IP.
     */
    private static final String REMOTE_IP = "192.168.1.5";

    /**
     * Mock for Networking module.
     */
    @Mock
    private AbstractNetworking mockNetworking;

    /**
     * Mock for RPC module.
     */
    @Mock
    private AbstractRPC mockRpc;

    /**
     * Mock for AudioCapture dependency.
     */
    @Mock
    private AudioCapture mockAudioCapture;

    /**
     * Mock for ImageScaler dependency.
     */
    @Mock
    private ImageScaler mockScaler;

    /**
     * Mock for ImageStitcher dependency.
     */
    @Mock
    private ImageStitcher mockStitcher;

    /**
     * Mock for synchronization function.
     */
    @Mock
    private BiFunction<String, Boolean, Void> mockAddSynchron;

    /**
     * Static mock for Utils class.
     */
    private MockedStatic<Utils> mockUtils;

    /**
     * Static mock for IPPacket class.
     */
    private MockedStatic<IPPacket> mockIPPacket;

    /**
     * Instance under test.
     */
    private CaptureComponents captureComponents;

    /**
     * AutoCloseable for Mockito.
     */
    private AutoCloseable closeable;


    /**
     * Sets up the test environment.
     * Initialize mocks, static mocks, and injects private dependencies via reflection.
     */
    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        // Initialize static mocks
        mockUtils = mockStatic(Utils.class);
        mockIPPacket = mockStatic(IPPacket.class);

        // Setup Utils common behavior
        mockUtils.when(Utils::getSelfIP).thenReturn(LOCAL_IP);

        // Initialize the component
        captureComponents = new CaptureComponents(mockNetworking, mockRpc, TEST_PORT, mockAddSynchron);

        // Inject the mock AudioCapture using Reflection (since it's created in constructor)
        final Field audioField = CaptureComponents.class.getDeclaredField("audioCapture");
        audioField.setAccessible(true);
        audioField.set(captureComponents, mockAudioCapture);

        // Inject ImageScaler
        final Field scalerField = CaptureComponents.class.getDeclaredField("scalar");
        scalerField.setAccessible(true);
        scalerField.set(captureComponents, mockScaler);

        // Inject ImageStitcher
        final Field stitcherField = CaptureComponents.class.getDeclaredField("imageStitcher");
        stitcherField.setAccessible(true);
        stitcherField.set(captureComponents, mockStitcher);
    }

    /**
     * Tears down the test environment.
     * Closes static mocks and mockito resources.
     */
    @AfterEach
    public void tearDown() throws Exception {
        mockUtils.close();
        mockIPPacket.close();
        closeable.close();
    }

    /**
     * Tests that all RPC handlers are registered upon initialization.
     */
    @Test
    public void testInitializationRegistersHandlers() {
        verify(mockRpc).subscribe(eq(Utils.START_VIDEO_CAPTURE), any());
        verify(mockRpc).subscribe(eq(Utils.STOP_VIDEO_CAPTURE), any());
        verify(mockRpc).subscribe(eq(Utils.START_SCREEN_CAPTURE), any());
        verify(mockRpc).subscribe(eq(Utils.STOP_SCREEN_CAPTURE), any());
        verify(mockRpc).subscribe(eq(Utils.START_AUDIO_CAPTURE), any());
        verify(mockRpc).subscribe(eq(Utils.STOP_AUDIO_CAPTURE), any());
        verify(mockRpc).subscribe(eq(Utils.SUBSCRIBE_AS_VIEWER), any());
        verify(mockRpc).subscribe(eq(Utils.UNSUBSCRIBE_AS_VIEWER), any());
    }

    /**
     * Tests toggling video capture via RPC callback.
     */
    @Test
    public void testVideoCaptureToggle() {
        // Capture the handler passed to RPC
        final Function<byte[], byte[]> startHandler = captureHandler(Utils.START_VIDEO_CAPTURE);
        final Function<byte[], byte[]> stopHandler = captureHandler(Utils.STOP_VIDEO_CAPTURE);

        // Initial state
        assertFalse(captureComponents.isVideoCaptureOn());
        // Simulate RPC call to START
        startHandler.apply(new byte[0]);
        assertTrue(captureComponents.isVideoCaptureOn());

        // Simulate RPC call to STOP
        stopHandler.apply(new byte[0]);
        assertFalse(captureComponents.isVideoCaptureOn());
    }

    /**
     * Tests toggling screen capture via RPC callback.
     */
    @Test
    public void testScreenCaptureToggle() {
        final Function<byte[], byte[]> startHandler = captureHandler(Utils.START_SCREEN_CAPTURE);
        final Function<byte[], byte[]> stopHandler = captureHandler(Utils.STOP_SCREEN_CAPTURE);

        assertFalse(captureComponents.isScreenCaptureOn());

        // Enable
        startHandler.apply(new byte[0]);
        assertTrue(captureComponents.isScreenCaptureOn());

        // Disable
        stopHandler.apply(new byte[0]);
        assertFalse(captureComponents.isScreenCaptureOn());
    }

    /**
     * Tests audio Feed retrieval when audio is off.
     */
    @Test
    public void testGetAudioFeed_AudioOff() {
        assertNull(captureComponents.getAudioFeed());
        verify(mockAudioCapture, atLeastOnce()).stop();
    }

    /**
     * Tests audio feed retrieval.
     * Logic:
     * 1. If audio is ON, return chunk from AudioCapture.
     * 2. If audio is OFF, stop AudioCapture and return null.
     */
    @Test
    public void testAudioFeedLogic_AudioOn() {
        final Function<byte[], byte[]> startHandler = captureHandler(Utils.START_AUDIO_CAPTURE);
        final Function<byte[], byte[]> stopHandler = captureHandler(Utils.STOP_AUDIO_CAPTURE);

        // Turning Audio On
        startHandler.apply(new byte[0]);
        final byte[] expectedAudio = new byte[]{1, 2, 3};
        when(mockAudioCapture.getChunk()).thenReturn(expectedAudio);

        final byte[] result = captureComponents.getAudioFeed();
        assertArrayEquals(expectedAudio, result);

        // Turning Audio Off
        stopHandler.apply(new byte[0]);
        assertNull(captureComponents.getAudioFeed());
        verify(mockAudioCapture, times(1)).stop();
    }

    /**
     * Tests getFeed returns null when no capture is active.
     */
    @Test
    public void testGetFeedReturnsNullWhenOff() {
        assertNull(captureComponents.getFeed());
    }

    /**
     * Tests getFeed logic when only Screen Capture is active.
     */
    @Test
    public void testGetFeedScreenOnly() {
        // Enable Screen Capture
        final Function<byte[], byte[]> startScreen = captureHandler(Utils.START_SCREEN_CAPTURE);
        startScreen.apply(new byte[0]);

        // Mock image data
        final BufferedImage mockImage = mock(BufferedImage.class);
        final int[][] mockMatrix = new int[][]{{1, 2}, {3, 4}};
        captureComponents.setLatestScreenFrame(mockImage);

        mockUtils.when(() -> Utils.convertToRGBMatrix(mockImage)).thenReturn(mockMatrix);

        final int[][] result = captureComponents.getFeed();

        assertNotNull(result);
        assertArrayEquals(mockMatrix, result);
    }

    /**
     * Tests getFeed logic when only Screen Capture is active.
     * Returns Scaled Down Feed
     */
    @Test
    public void testGetFeedScreenOnly_ScaledDown() {
        // Enable Screen Capture
        final Function<byte[], byte[]> startScreen = captureHandler(Utils.START_SCREEN_CAPTURE);
        startScreen.apply(new byte[0]);

        // Mock image data
        final BufferedImage mockImage = mock(BufferedImage.class);
        final int[][] largeMatrix = new int[1000][1000];
        captureComponents.setLatestScreenFrame(mockImage);

        // Create the expected small matrix that the scaler should produce
        final int[][] scaledMatrix = new int[100][100];

        mockUtils.when(() -> Utils.convertToRGBMatrix(mockImage)).thenReturn(largeMatrix);
        when(mockScaler.scale(eq(largeMatrix), anyInt(), anyInt())).thenReturn(scaledMatrix);

        final int[][] result = captureComponents.getFeed();

        assertNotNull(result);
        assertArrayEquals(scaledMatrix, result , "Returned Feed should be scaled down.");
    }

    /**
     * Tests getFeed logic when only Video Capture is active.
     * Should return the video matrix without stitching.
     */
    @Test
    public void testGetFeedVideoOnly() {
        // Enable VideoCapture
        final Function<byte[], byte[]> startVideo = captureHandler(Utils.START_VIDEO_CAPTURE);
        startVideo.apply(new byte[0]);

        // Mock Video Data
        final BufferedImage mockVideoFrame = mock(BufferedImage.class);
        captureComponents.setLatestVideoFrame(mockVideoFrame);

        // Mock util Conversion
        final int[][] videoMatrix = new int[][]{{10, 10}, {20, 20}};
        mockUtils.when(() -> Utils.convertToRGBMatrix(mockVideoFrame)).thenReturn(videoMatrix);

        when(mockScaler.scale(any(), anyInt(), anyInt())).thenReturn(videoMatrix);

        // Calling actual function
        final int[][] result = captureComponents.getFeed();

        assertNotNull(result);
        assertArrayEquals(videoMatrix, result);

        verify(mockStitcher, times(0)).stitch((Stitchable) any());
    }

    /**
     * Tests getFeed logic when BOTH Screen and Video are active.
     * This verifies the Picture-in-Picture (Stitching) logic.
     */
    @Test
    public void testGetFeedBothScreenAndVideo() {
        // Enable Both Captures
        final Function<byte[], byte[]> startVideo = captureHandler(Utils.START_VIDEO_CAPTURE);
        final Function<byte[], byte[]> startScreen = captureHandler(Utils.START_SCREEN_CAPTURE);
        startVideo.apply(new byte[0]);
        startScreen.apply(new byte[0]);

        // Mock Frames
        final BufferedImage mockScreenFrame = mock(BufferedImage.class);
        final BufferedImage mockVideoFrame = mock(BufferedImage.class);
        captureComponents.setLatestScreenFrame(mockScreenFrame);
        captureComponents.setLatestVideoFrame(mockVideoFrame);

        // Mock Utils conversions
        // Screen is 100x100
        final int[][] screenMatrix = new int[100][100];
        // Video is 50x50
        final int[][] videoMatrix = new int[50][50];

        mockUtils.when(() -> Utils.convertToRGBMatrix(mockScreenFrame)).thenReturn(screenMatrix);
        mockUtils.when(() -> Utils.convertToRGBMatrix(mockVideoFrame)).thenReturn(videoMatrix);

        final int[][] scaledVideoMatrix = new int[10][10];
        when(mockScaler.scale(eq(videoMatrix), anyInt(), anyInt())).thenReturn(scaledVideoMatrix);

        when(mockStitcher.getCanvas()).thenReturn(screenMatrix);

        final int[][] result = captureComponents.getFeed();
        assertNotNull(result);

        verify(mockScaler).scale(eq(videoMatrix), anyInt(), anyInt());
        verify(mockStitcher).setCanvas(screenMatrix);
        verify(mockStitcher).stitch(any(Patch.class));
    }

    /**
     * Tests Viewer Subscription RPC logic.
     * Verifies that it deserializes the packet, checks IP, updates sync, and sends data.
     */
    @Test
    public void testSubscribeAsViewer() {
        final Function<byte[], byte[]> subscribeHandler = captureHandler(Utils.SUBSCRIBE_AS_VIEWER);

        // Mock deserialization to return a valid packet from a REMOTE IP
        final IPPacket mockPacket = mock(IPPacket.class);
        when(mockPacket.ip()).thenReturn(REMOTE_IP);
        when(mockPacket.reqCompression()).thenReturn(true);
        mockIPPacket.when(() -> IPPacket.deserialize(any())).thenReturn(mockPacket);

        // Invoke handler
        subscribeHandler.apply(new byte[]{1, 2, 3});

        // Verify addSynchron called
        verify(mockAddSynchron).apply(REMOTE_IP, true);

        // Verify networking sent data
        verify(mockNetworking).sendData(any(), any(ClientNode[].class), anyInt(), eq(2));
    }

    /**
     * Tests Viewer Subscription ignores self-requests.
     */
    @Test
    public void testSubscribeAsViewerIgnoredForSelf() {
        final Function<byte[], byte[]> subscribeHandler = captureHandler(Utils.SUBSCRIBE_AS_VIEWER);

        // Mock packet from LOCAL IP
        final IPPacket mockPacket = mock(IPPacket.class);
        when(mockPacket.ip()).thenReturn(LOCAL_IP);
        mockIPPacket.when(() -> IPPacket.deserialize(any())).thenReturn(mockPacket);

        subscribeHandler.apply(new byte[]{0});

        // Verify networking was NOT called
        verify(mockNetworking, times(0)).sendData(any(), any(), anyInt(), anyInt());
    }

    /**
     * Tests Unsubscribe RPC logic.
     */
    @Test
    public void testUnsubscribeAsViewer() {
        final Function<byte[], byte[]> unsubscribeHandler = captureHandler(Utils.UNSUBSCRIBE_AS_VIEWER);

        final IPPacket mockPacket = mock(IPPacket.class);
        when(mockPacket.ip()).thenReturn(REMOTE_IP);
        mockIPPacket.when(() -> IPPacket.deserialize(any())).thenReturn(mockPacket);

        unsubscribeHandler.apply(new byte[]{0});

        // Verify networking sent the unsubscribe packet
        verify(mockNetworking).sendData(any(), any(ClientNode[].class), eq(ModuleType.SCREENSHARING.ordinal()), eq(2));
    }

    /**
     * Tests Unsubscribe RPC logic.
     */
    @Test
    public void testUnsubscribeAsViewerIgnoredForSelf() {
        final Function<byte[], byte[]> unsubscribeHandler = captureHandler(Utils.UNSUBSCRIBE_AS_VIEWER);

        final IPPacket mockPacket = mock(IPPacket.class);
        when(mockPacket.ip()).thenReturn(LOCAL_IP);
        mockIPPacket.when(() -> IPPacket.deserialize(any())).thenReturn(mockPacket);

        unsubscribeHandler.apply(new byte[]{0});

        // Verify networking was NOT called
        verify(mockNetworking, times(0)).sendData(any(), any(), anyInt(), anyInt());
    }

    /**
     * Helper method to capture the RPC handler lambda for a specific topic.
     *
     * @param topic the RPC topic constant
     * @return the captured Function handler
     */
    @SuppressWarnings("unchecked")
    private Function<byte[], byte[]> captureHandler(final String topic) {
        final ArgumentCaptor<Function<byte[], byte[]>> captor = ArgumentCaptor.forClass(Function.class);
        // We look for the call to subscribe with the specific topic
        verify(mockRpc, atLeastOnce()).subscribe(eq(topic), captor.capture());

        // Return the last captured value (most recent subscription)
        final List<Function<byte[], byte[]>> allValues = captor.getAllValues();
        return allValues.get(allValues.size() - 1);
    }

}
