package com.swe.ScreenNVideo.Capture;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sound.sampled.*;
import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AudioCaptureTest {

    private AudioCapture audioCapture;
    private TargetDataLine mockLine;

    // ---------------------------
    // Reflection Helpers
    // ---------------------------
    private <T> T getPrivateField(Object obj, String fieldName, Class<T> type) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return type.cast(f.get(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // ---------------------------

    @BeforeEach
    void setUp() {
        audioCapture = new AudioCapture();
        mockLine = mock(TargetDataLine.class);
    }

    @Test
    void testInitSuccess() throws Exception {
        try (MockedStatic<AudioSystem> mocked = Mockito.mockStatic(AudioSystem.class)) {

            mocked.when(() -> AudioSystem.getLine(any(DataLine.Info.class)))
                    .thenReturn(mockLine);

            doNothing().when(mockLine).open(any(AudioFormat.class));
            doNothing().when(mockLine).start();

            boolean result = audioCapture.init();
            assertTrue(result);

            TargetDataLine capturedLine =
                    getPrivateField(audioCapture, "microphone", TargetDataLine.class);

            Boolean running =
                    getPrivateField(audioCapture, "running", Boolean.class);

            assertNotNull(capturedLine);
            assertTrue(running);
        }
    }

    @Test
    void testInitFailure() throws Exception {
        try (MockedStatic<AudioSystem> mocked = Mockito.mockStatic(AudioSystem.class)) {

            mocked.when(() -> AudioSystem.getLine(any(DataLine.Info.class)))
                    .thenThrow(new LineUnavailableException());

            boolean result = audioCapture.init();
            assertFalse(result);
        }
    }

    @Test
    void testRunCapturesChunks() throws Exception {
        try (MockedStatic<AudioSystem> mocked = Mockito.mockStatic(AudioSystem.class)) {

            mocked.when(() -> AudioSystem.getLine(any(DataLine.Info.class)))
                    .thenReturn(mockLine);

            doNothing().when(mockLine).open(any(AudioFormat.class));
            when(mockLine.isOpen()).thenReturn(true, true, false);

            // When microphone.read is called…
            when(mockLine.read(any(byte[].class), anyInt(), anyInt()))
                    .thenAnswer(inv -> {
                        byte[] buf = inv.getArgument(0);
                        buf[0] = 10;
                        return 1;
                    });

            audioCapture.init();
            Thread.sleep(50); // allow capture thread to run

            BlockingQueue<byte[]> queue =
                    getPrivateField(audioCapture, "audioQueue", BlockingQueue.class);

            assertFalse(queue.isEmpty());
            assertEquals(10, queue.peek()[0]);
        }
    }

    @Test
    void testGetChunk() throws Exception {
        BlockingQueue<byte[]> queue =
                getPrivateField(audioCapture, "audioQueue", BlockingQueue.class);

        byte[] testData = new byte[]{42};
        queue.offer(testData);

        byte[] result = audioCapture.getChunk();

        assertNotNull(result);
        assertEquals(42, result[0]);
    }

    @Test
    void testStop() throws Exception {
        setPrivateField(audioCapture, "microphone", mockLine);
        setPrivateField(audioCapture, "running", true);

        Thread dummy = new Thread(() -> {});
        setPrivateField(audioCapture, "captureThread", dummy);

        audioCapture.stop();

        verify(mockLine, times(1)).stop();
        verify(mockLine, times(1)).close();

        Boolean running =
                getPrivateField(audioCapture, "running", Boolean.class);

        assertFalse(running);
    }

    @Test
    void testReinit() throws Exception {
        AudioCapture spyCapture = Mockito.spy(new AudioCapture());

        doNothing().when(spyCapture).stop();
        doReturn(true).when(spyCapture).init();

        boolean result = spyCapture.reinit();

        verify(spyCapture).stop();
        verify(spyCapture).init();
        assertTrue(result);
    }

    @Test
    void testStopWhenMicrophoneIsNull() {
        setPrivateField(audioCapture, "microphone", null);
        setPrivateField(audioCapture, "captureThread", null);
        setPrivateField(audioCapture, "running", true);

        assertDoesNotThrow(() -> audioCapture.stop());
    }

    @Test
    void testStopHandlesException() throws Exception {
        setPrivateField(audioCapture, "microphone", mockLine);

        Thread badThread = mock(Thread.class);
        doThrow(new InterruptedException()).when(badThread).join();

        setPrivateField(audioCapture, "captureThread", badThread);

        assertDoesNotThrow(() -> audioCapture.stop());
    }

    @Test
    void testRunWhenNotRunning() {
        setPrivateField(audioCapture, "running", false);
        setPrivateField(audioCapture, "microphone", mockLine);

        when(mockLine.isOpen()).thenReturn(true);

        audioCapture.run(); // should skip loop

        BlockingQueue<byte[]> queue =
                getPrivateField(audioCapture, "audioQueue", BlockingQueue.class);

        assertTrue(queue.isEmpty());
    }

    @Test
    void testRunWhenBytesReadIsZero() throws Exception {
        setPrivateField(audioCapture, "running", true);
        setPrivateField(audioCapture, "microphone", mockLine);

        when(mockLine.isOpen()).thenReturn(true, false); // loop once then exit
        when(mockLine.read(any(), anyInt(), anyInt())).thenReturn(0);

        audioCapture.run();

        BlockingQueue<byte[]> queue =
                getPrivateField(audioCapture, "audioQueue", BlockingQueue.class);

        assertTrue(queue.isEmpty());
    }

    @Test
    void testGetChunkBlockingInterrupted() throws Exception {
        AudioCapture spyCapture = Mockito.spy(new AudioCapture());

        BlockingQueue<byte[]> mockQueue = mock(BlockingQueue.class);

        // Replace private audioQueue with our mock
        setPrivateField(spyCapture, "audioQueue", mockQueue);

        // Simulate take() throwing InterruptedException
        when(mockQueue.take()).thenThrow(new InterruptedException());

        byte[] result = spyCapture.getChunkBlocking();

        assertNull(result); // must return null
    }

    @Test
    void testGetChunkWhenRunningIsTrue() {
        AudioCapture spyCapture = Mockito.spy(new AudioCapture());

        // Force running = true
        setPrivateField(spyCapture, "running", true);

        // Ensure init() is NOT called
        doReturn(true).when(spyCapture).init();

        // Add one item to the queue
        BlockingQueue<byte[]> queue =
                getPrivateField(spyCapture, "audioQueue", BlockingQueue.class);
        queue.offer(new byte[]{55});

        byte[] result = spyCapture.getChunk();

        // Verify init() was NOT called
        verify(spyCapture, never()).init();

        assertNotNull(result);
        assertEquals(55, result[0]);
    }


}
