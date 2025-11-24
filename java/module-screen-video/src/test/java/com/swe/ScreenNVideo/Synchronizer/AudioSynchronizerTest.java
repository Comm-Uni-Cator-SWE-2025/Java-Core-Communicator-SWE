/**
 * Contributed by @chirag9528.
 */
package com.swe.ScreenNVideo.Synchronizer;

import com.swe.ScreenNVideo.Codec.ADPCMDecoder;
import com.swe.ScreenNVideo.Model.APackets;
import com.swe.ScreenNVideo.Playback.AudioPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for AudioSynchronizer class.
 * Tests synchronization of audio packets, decoding state updates, and playback delegation.
 */
public class AudioSynchronizerTest {

    /**
     * Mock for the Audio Player.
     */
    @Mock
    private AudioPlayer mockAudioPlayer;

    /**
     * Mock for the ADPCM Decoder.
     * Injected via reflection as it is instantiated internally.
     */
    @Mock
    private ADPCMDecoder mockDecoder;

    /**
     * Instance under test.
     */
    private AudioSynchronizer synchronizer;

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

        // Initialize the synchronizer with the mock player
        synchronizer = new AudioSynchronizer(mockAudioPlayer);

        // Inject the mock ADPCMDecoder using Reflection
        // The field 'decoder' is created inside the constructor, so we swap it out here.
        final Field decoderField = AudioSynchronizer.class.getDeclaredField("decoder");
        decoderField.setAccessible(true);
        decoderField.set(synchronizer, mockDecoder);
    }

    /**
     * Tears down the test environment.
     */
    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Tests that the constructor initializes the object successfully.
     */
    @Test
    public void testConstructorInitialization() {
        assertNotNull(synchronizer, "AudioSynchronizer should be initialized");
    }

    /**
     * Tests the synchronize method flow.
     * <ol>
     * <li>Extracts state (predictedPCM, indexPCM) from packet.</li>
     * <li>Updates decoder state.</li>
     * <li>Decodes the data.</li>
     * <li>Plays the decoded audio.</li>
     * </ol>
     */
    @Test
    public void testSynchronizeProcess() {
        // 1. Setup Mock Packet Data
        final APackets mockPacket = mock(APackets.class);
        final int testPredictedPCM = 1234;
        final int testIndexPCM = 5;
        final byte[] encodedData = new byte[]{10, 20, 30};
        final byte[] decodedData = new byte[]{100, 101, 102}; // Dummy PCM output

        when(mockPacket.predictedPCM()).thenReturn(testPredictedPCM);
        when(mockPacket.indexPCM()).thenReturn(testIndexPCM);
        when(mockPacket.data()).thenReturn(encodedData);

        // 2. Setup Decoder Behavior
        when(mockDecoder.decode(eq(encodedData))).thenReturn(decodedData);

        // 3. Execute
        final boolean result = synchronizer.synchronize(mockPacket);

        // 4. Verify
        assertTrue(result, "Synchronize should return true on success");

        // Verify decoder state was set BEFORE decoding
        verify(mockDecoder).setState(eq(testPredictedPCM), eq(testIndexPCM));

        // Verify decoding was called with packet data
        verify(mockDecoder).decode(eq(encodedData));

        // Verify player was called with the DECODED data (not raw data)
        verify(mockAudioPlayer).play(eq(decodedData));
    }

    /**
     * Tests synchronize with empty data.
     * Ensures components handle empty inputs gracefully if passed through.
     */
    @Test
    public void testSynchronizeWithEmptyData() {
        final APackets mockPacket = mock(APackets.class);
        final byte[] emptyData = new byte[0];
        final byte[] emptyDecoded = new byte[0];

        when(mockPacket.data()).thenReturn(emptyData);
        when(mockDecoder.decode(emptyData)).thenReturn(emptyDecoded);

        final boolean result = synchronizer.synchronize(mockPacket);

        assertTrue(result);
        verify(mockAudioPlayer).play(emptyDecoded);
    }
}
