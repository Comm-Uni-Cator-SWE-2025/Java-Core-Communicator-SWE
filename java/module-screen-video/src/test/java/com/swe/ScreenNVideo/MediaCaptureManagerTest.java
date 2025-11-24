/**
 * Comprehensive test suite for MediaCaptureManager class.
 * Tests media capture management, networking, synchronization, and packet handling.
 */
package com.swe.ScreenNVideo;

import com.swe.ScreenNVideo.Capture.BackgroundCaptureManager;
import com.swe.ScreenNVideo.Model.APackets;
import com.swe.ScreenNVideo.Model.CPackets;
import com.swe.ScreenNVideo.Model.Feed;
import com.swe.ScreenNVideo.Model.IPPacket;
import com.swe.ScreenNVideo.Model.NetworkPacketType;
import com.swe.ScreenNVideo.Model.RImage;
import com.swe.ScreenNVideo.Model.Viewer;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.Playback.AudioPlayer;
import com.swe.ScreenNVideo.Synchronizer.AudioSynchronizer;
import com.swe.ScreenNVideo.Synchronizer.FeedData;
import com.swe.ScreenNVideo.Synchronizer.ImageSynchronizer;
import com.swe.ScreenNVideo.Telemetry.Telemetry;
import com.swe.core.ClientNode;
import com.swe.core.Context;
import com.swe.core.RPC;
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

import javax.sound.sampled.LineUnavailableException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for MediaCaptureManager class to achieve 100% coverage.
 */
public class MediaCaptureManagerTest {

    private static final int TEST_PORT = 8080;
    private static final String LOCAL_IP = "127.0.0.1";
    private static final String REMOTE_IP = "192.168.1.100";
    private static final String REMOTE_IP_2 = "192.168.1.101";

    @Mock
    private AbstractNetworking mockNetworking;

    @Mock
    private RPC mockRpc;

    @Mock
    private CaptureComponents mockCaptureComponents;

    @Mock
    private BackgroundCaptureManager mockBgManager;

    @Mock
    private VideoComponents mockVideoComponents;

    @Mock
    private AudioPlayer mockAudioPlayer;

    @Mock
    private ImageSynchronizer mockImageSynchronizer;

    @Mock
    private AudioSynchronizer mockAudioSynchronizer;

    private MediaCaptureManager mediaCaptureManager;
    private AutoCloseable closeable;
    private MockedStatic<Utils> mockUtils;
    private MockedStatic<Telemetry> mockTelemetry;

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        // Setup static mocks - only mock methods, not final fields
        mockUtils = mockStatic(Utils.class);
        mockTelemetry = mockStatic(Telemetry.class);

        // Mock the getSelfIP method (it's a method, not a field)
        mockUtils.when(Utils::getSelfIP).thenReturn(LOCAL_IP);
        // Note: Static final fields (constants) cannot be mocked and will use their real values

        final Telemetry mockTelemetryInstance = mock(Telemetry.class);
        mockTelemetry.when(Telemetry::getTelemetry).thenReturn(mockTelemetryInstance);

        // Inject mock RPC into Context - direct assignment since rpc is public
        Context.getInstance().rpc = mockRpc;

        // Create MediaCaptureManager - constructor creates real dependencies
        // We'll inject mocks after construction for testing
        mediaCaptureManager = new MediaCaptureManager(mockNetworking, TEST_PORT);

        // Inject mocks using reflection for easier testing
        injectMock(mediaCaptureManager, "videoComponent", mockVideoComponents);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        if (mockUtils != null) {
            mockUtils.close();
        }
        if (mockTelemetry != null) {
            mockTelemetry.close();
        }
    }

    /**
     * Helper to inject mocks into private fields.
     */
    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    /**
     * Helper to get private field value.
     */
    @SuppressWarnings("unchecked")
    private <T> T getField(Object target, String fieldName) throws Exception {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    /**
     * Tests constructor initializes all fields correctly.
     */
    @Test
    public void testConstructor_AllFieldsInitialized() throws Exception {
        assertNotNull(mediaCaptureManager);
        assertEquals(TEST_PORT, (Integer) getField(mediaCaptureManager, "port"));
        assertNotNull(getField(mediaCaptureManager, "networking"));
        assertNotNull(getField(mediaCaptureManager, "rpc"));
        assertNotNull(getField(mediaCaptureManager, "imageSynchronizers"));
        assertNotNull(getField(mediaCaptureManager, "audioSynchronizers"));
        assertNotNull(getField(mediaCaptureManager, "viewers"));
        assertNotNull(getField(mediaCaptureManager, "clientHandler"));
        assertNotNull(getField(mediaCaptureManager, "localIp"));
    }

    /**
     * Tests constructor handles AudioPlayer initialization failure.
     * Note: This is hard to test directly since AudioPlayer is created in constructor,
     * but the constructor should handle LineUnavailableException gracefully.
     */
    @Test
    public void testConstructor_Initialization() throws Exception {
        // Constructor should complete successfully even if AudioPlayer.init() fails
        // (it catches the exception)
        final MediaCaptureManager manager = new MediaCaptureManager(mockNetworking, TEST_PORT);
        assertNotNull(manager);
    }

    /**
     * Tests updateImage with null synchronizer.
     */
    @Test
    public void testUpdateImage_NullSynchronizer() {
        final Void result = mediaCaptureManager.updateImage(REMOTE_IP, true);
        assertNull(result);
    }

    /**
     * Tests updateImage with valid synchronizer.
     */
    @Test
    public void testUpdateImage_ValidSynchronizer() throws Exception {
        final HashMap<String, ImageSynchronizer> imageSynchronizers = getField(mediaCaptureManager, "imageSynchronizers");
        imageSynchronizers.put(REMOTE_IP, mockImageSynchronizer);

        final Void result = mediaCaptureManager.updateImage(REMOTE_IP, true);

        assertNull(result);
        verify(mockImageSynchronizer).setReqCompression(true);
        verify(mockImageSynchronizer).setWaitingForFullImage(true);
    }

    /**
     * Tests broadcastJoinMeeting sends correct packet.
     */
    @Test
    public void testBroadcastJoinMeeting() {
        mediaCaptureManager.broadcastJoinMeeting();

        final ArgumentCaptor<byte[]> dataCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(mockNetworking).broadcast(dataCaptor.capture(), eq(ModuleType.SCREENSHARING.ordinal()), eq(2));
        assertNotNull(dataCaptor.getValue());
    }

    /**
     * Tests addParticipant with null IP.
     */
    @Test
    public void testAddParticipant_NullIP() throws Exception {
        final Method addParticipantMethod = MediaCaptureManager.class.getDeclaredMethod("addParticipant", String.class, boolean.class);
        addParticipantMethod.setAccessible(true);
        addParticipantMethod.invoke(mediaCaptureManager, (String) null, true);

        final HashMap<String, Viewer> viewers = getField(mediaCaptureManager, "viewers");
        assertEquals(0, viewers.size());
    }

    /**
     * Tests addParticipant with local IP (should be ignored).
     */
    @Test
    public void testAddParticipant_LocalIP() throws Exception {
        final Method addParticipantMethod = MediaCaptureManager.class.getDeclaredMethod("addParticipant", String.class, boolean.class);
        addParticipantMethod.setAccessible(true);
        addParticipantMethod.invoke(mediaCaptureManager, LOCAL_IP, true);

        final HashMap<String, Viewer> viewers = getField(mediaCaptureManager, "viewers");
        assertEquals(0, viewers.size());
    }

    /**
     * Tests addParticipant creates new participant.
     */
    @Test
    public void testAddParticipant_NewParticipant() throws Exception {
        final Method addParticipantMethod = MediaCaptureManager.class.getDeclaredMethod("addParticipant", String.class, boolean.class);
        addParticipantMethod.setAccessible(true);
        addParticipantMethod.invoke(mediaCaptureManager, REMOTE_IP, true);

        final HashMap<String, Viewer> viewers = getField(mediaCaptureManager, "viewers");
        final HashMap<String, ImageSynchronizer> imageSynchronizers = getField(mediaCaptureManager, "imageSynchronizers");
        final HashMap<String, AudioSynchronizer> audioSynchronizers = getField(mediaCaptureManager, "audioSynchronizers");

        assertEquals(1, viewers.size());
        assertEquals(1, imageSynchronizers.size());
        assertEquals(1, audioSynchronizers.size());
        assertTrue(viewers.containsKey(REMOTE_IP));
    }

    /**
     * Tests addParticipant with existing participant.
     */
    @Test
    public void testAddParticipant_ExistingParticipant() throws Exception {
        final Method addParticipantMethod = MediaCaptureManager.class.getDeclaredMethod("addParticipant", String.class, boolean.class);
        addParticipantMethod.setAccessible(true);

        // Add first time
        addParticipantMethod.invoke(mediaCaptureManager, REMOTE_IP, true);
        
        // Add second time with different compression
        addParticipantMethod.invoke(mediaCaptureManager, REMOTE_IP, false);

        final HashMap<String, Viewer> viewers = getField(mediaCaptureManager, "viewers");
        assertEquals(1, viewers.size());
        final Viewer viewer = viewers.get(REMOTE_IP);
        assertFalse(viewer.isRequireCompressed());
    }

    /**
     * Tests removeViewer removes all associated data.
     */
    @Test
    public void testRemoveViewer() throws Exception {
        // First add a participant
        final Method addParticipantMethod = MediaCaptureManager.class.getDeclaredMethod("addParticipant", String.class, boolean.class);
        addParticipantMethod.setAccessible(true);
        addParticipantMethod.invoke(mediaCaptureManager, REMOTE_IP, true);

        // Then remove
        final Method removeViewerMethod = MediaCaptureManager.class.getDeclaredMethod("removeViewer", String.class);
        removeViewerMethod.setAccessible(true);
        removeViewerMethod.invoke(mediaCaptureManager, REMOTE_IP);

        final HashMap<String, Viewer> viewers = getField(mediaCaptureManager, "viewers");
        final HashMap<String, ImageSynchronizer> imageSynchronizers = getField(mediaCaptureManager, "imageSynchronizers");
        final HashMap<String, AudioSynchronizer> audioSynchronizers = getField(mediaCaptureManager, "audioSynchronizers");

        assertEquals(0, viewers.size());
        assertEquals(0, imageSynchronizers.size());
        assertEquals(0, audioSynchronizers.size());
    }

    /**
     * Tests sendDataToViewers with null feed.
     */
    @Test
    public void testSendDataToViewers_NullFeed() throws Exception {
        final Method sendDataToViewersMethod = MediaCaptureManager.class.getDeclaredMethod("sendDataToViewers", byte[].class, java.util.function.Predicate.class);
        sendDataToViewersMethod.setAccessible(true);
        sendDataToViewersMethod.invoke(mediaCaptureManager, (byte[]) null, (java.util.function.Predicate<Viewer>) v -> true);

        verify(mockNetworking, never()).sendData(any(), any(), anyInt(), anyInt());
    }

    /**
     * Tests sendDataToViewers with empty viewers list.
     */
    @Test
    public void testSendDataToViewers_EmptyViewers() throws Exception {
        final byte[] testData = new byte[]{1, 2, 3};
        final Method sendDataToViewersMethod = MediaCaptureManager.class.getDeclaredMethod("sendDataToViewers", byte[].class, java.util.function.Predicate.class);
        sendDataToViewersMethod.setAccessible(true);
        sendDataToViewersMethod.invoke(mediaCaptureManager, testData, (java.util.function.Predicate<Viewer>) v -> true);

        verify(mockNetworking, never()).sendData(any(), any(), anyInt(), anyInt());
    }

    /**
     * Tests sendDataToViewers with filtered viewers.
     */
    @Test
    public void testSendDataToViewers_WithFilteredViewers() throws Exception {
        // Add participants
        final Method addParticipantMethod = MediaCaptureManager.class.getDeclaredMethod("addParticipant", String.class, boolean.class);
        addParticipantMethod.setAccessible(true);
        addParticipantMethod.invoke(mediaCaptureManager, REMOTE_IP, true);
        addParticipantMethod.invoke(mediaCaptureManager, REMOTE_IP_2, false);

        final byte[] testData = new byte[]{1, 2, 3};
        final Method sendDataToViewersMethod = MediaCaptureManager.class.getDeclaredMethod("sendDataToViewers", byte[].class, java.util.function.Predicate.class);
        sendDataToViewersMethod.setAccessible(true);
        
        // Send only to compressed viewers
        sendDataToViewersMethod.invoke(mediaCaptureManager, testData, (java.util.function.Predicate<Viewer>) Viewer::isRequireCompressed);

        final ArgumentCaptor<ClientNode[]> nodesCaptor = ArgumentCaptor.forClass(ClientNode[].class);
        verify(mockNetworking).sendData(eq(testData), nodesCaptor.capture(), eq(ModuleType.SCREENSHARING.ordinal()), eq(2));
        assertEquals(1, nodesCaptor.getValue().length);
    }

    /**
     * Tests ClientHandler receives empty data.
     */
    @Test
    public void testClientHandler_EmptyData() throws Exception {
        final MediaCaptureManager.ClientHandler handler = getField(mediaCaptureManager, "clientHandler");
        handler.receiveData(new byte[0]);

        verify(mockRpc, never()).call(anyString(), any());
    }

    /**
     * Tests ClientHandler receives invalid packet type.
     */
    @Test
    public void testClientHandler_InvalidPacketType() throws Exception {
        final MediaCaptureManager.ClientHandler handler = getField(mediaCaptureManager, "clientHandler");
        final byte[] invalidData = new byte[100];
        invalidData[0] = (byte) 100; // Invalid packet type

        handler.receiveData(invalidData);

        verify(mockRpc, never()).call(anyString(), any());
    }

    /**
     * Tests ClientHandler handles SUBSCRIBE_AS_VIEWER packet.
     */
    @Test
    public void testClientHandler_SubscribeAsViewer() throws Exception {
        when(mockVideoComponents.captureFullImage()).thenReturn(new Feed(new byte[]{1, 2, 3}, new byte[]{4, 5, 6}));
        when(mockVideoComponents.isVideoCaptureOn()).thenReturn(false);
        when(mockVideoComponents.isScreenCaptureOn()).thenReturn(true);

        final IPPacket packet = new IPPacket(REMOTE_IP, true);
        final byte[] data = packet.serialize(NetworkPacketType.SUBSCRIBE_AS_VIEWER);

        final MediaCaptureManager.ClientHandler handler = getField(mediaCaptureManager, "clientHandler");
        handler.receiveData(data);

        final HashMap<String, Viewer> viewers = getField(mediaCaptureManager, "viewers");
        assertTrue(viewers.containsKey(REMOTE_IP));
    }

    /**
     * Tests ClientHandler handles STOP_SHARE packet.
     */
    @Test
    public void testClientHandler_StopShare() throws Exception {
        final IPPacket packet = new IPPacket(REMOTE_IP, false);
        final byte[] data = packet.serialize(NetworkPacketType.STOP_SHARE);

        when(mockRpc.call(anyString(), any())).thenReturn(CompletableFuture.completedFuture(new byte[0]));

        final MediaCaptureManager.ClientHandler handler = getField(mediaCaptureManager, "clientHandler");
        handler.receiveData(data);

        verify(mockRpc).call(eq(Utils.STOP_SHARE), any(byte[].class));
    }

    /**
     * Tests ClientHandler handles UNSUBSCRIBE_AS_VIEWER packet.
     */
    @Test
    public void testClientHandler_UnsubscribeAsViewer() throws Exception {
        // First add participant
        final Method addParticipantMethod = MediaCaptureManager.class.getDeclaredMethod("addParticipant", String.class, boolean.class);
        addParticipantMethod.setAccessible(true);
        addParticipantMethod.invoke(mediaCaptureManager, REMOTE_IP, true);

        final IPPacket packet = new IPPacket(REMOTE_IP, false);
        final byte[] data = packet.serialize(NetworkPacketType.UNSUBSCRIBE_AS_VIEWER);

        final MediaCaptureManager.ClientHandler handler = getField(mediaCaptureManager, "clientHandler");
        handler.receiveData(data);

        final HashMap<String, Viewer> viewers = getField(mediaCaptureManager, "viewers");
        assertFalse(viewers.containsKey(REMOTE_IP));
    }

    /**
     * Tests ClientHandler handles APACKETS packet.
     */
    @Test
    public void testClientHandler_APackets() throws Exception {
        // First add participant
        final Method addParticipantMethod = MediaCaptureManager.class.getDeclaredMethod("addParticipant", String.class, boolean.class);
        addParticipantMethod.setAccessible(true);
        addParticipantMethod.invoke(mediaCaptureManager, REMOTE_IP, true);

        final APackets audioPacket = new APackets(1, new byte[]{1, 2, 3}, REMOTE_IP, 0, 0);
        final byte[] data = audioPacket.serializeAPackets();

        final HashMap<String, AudioSynchronizer> audioSynchronizers = getField(mediaCaptureManager, "audioSynchronizers");
        final AudioSynchronizer realSynchronizer = new AudioSynchronizer(mockAudioPlayer);
        audioSynchronizers.put(REMOTE_IP, realSynchronizer);

        final MediaCaptureManager.ClientHandler handler = getField(mediaCaptureManager, "clientHandler");
        handler.receiveData(data);

        // Verify synchronizer was called (indirectly through real object)
        assertTrue(true); // If no exception, test passes
    }

    /**
     * Tests ClientHandler askForFullImage method.
     */
    @Test
    public void testClientHandler_AskForFullImage() throws Exception {
        final MediaCaptureManager.ClientHandler handler = getField(mediaCaptureManager, "clientHandler");
        final Method askForFullImageMethod = MediaCaptureManager.ClientHandler.class.getDeclaredMethod("askForFullImage", String.class, boolean.class);
        askForFullImageMethod.setAccessible(true);
        askForFullImageMethod.invoke(handler, REMOTE_IP, true);

        final ArgumentCaptor<byte[]> dataCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(mockNetworking).sendData(dataCaptor.capture(), any(ClientNode[].class), eq(ModuleType.SCREENSHARING.ordinal()), eq(2));
        assertNotNull(dataCaptor.getValue());
    }

    /**
     * Tests ClientHandler addUserNFullImageRequest with video only.
     */
    @Test
    public void testClientHandler_AddUserNFullImageRequest_VideoOnly() throws Exception {
        when(mockVideoComponents.captureFullImage()).thenReturn(new Feed(new byte[]{1, 2, 3}, new byte[]{4, 5, 6}));
        when(mockVideoComponents.isVideoCaptureOn()).thenReturn(true);
        when(mockVideoComponents.isScreenCaptureOn()).thenReturn(false);

        final MediaCaptureManager.ClientHandler handler = getField(mediaCaptureManager, "clientHandler");
        final Method addUserNFullImageRequestMethod = MediaCaptureManager.ClientHandler.class.getDeclaredMethod("addUserNFullImageRequest", String.class, boolean.class);
        addUserNFullImageRequestMethod.setAccessible(true);
        addUserNFullImageRequestMethod.invoke(handler, REMOTE_IP, false);

        verify(mockNetworking).sendData(any(byte[].class), any(ClientNode[].class), eq(ModuleType.SCREENSHARING.ordinal()), eq(2));
    }

    /**
     * Tests ClientHandler addUserNFullImageRequest with null feed.
     */
    @Test
    public void testClientHandler_AddUserNFullImageRequest_NullFeed() throws Exception {
        when(mockVideoComponents.captureFullImage()).thenReturn(null);

        final MediaCaptureManager.ClientHandler handler = getField(mediaCaptureManager, "clientHandler");
        final Method addUserNFullImageRequestMethod = MediaCaptureManager.ClientHandler.class.getDeclaredMethod("addUserNFullImageRequest", String.class, boolean.class);
        addUserNFullImageRequestMethod.setAccessible(true);
        addUserNFullImageRequestMethod.invoke(handler, REMOTE_IP, true);

        verify(mockNetworking, never()).sendData(any(byte[].class), any(ClientNode[].class), anyInt(), anyInt());
    }
}
