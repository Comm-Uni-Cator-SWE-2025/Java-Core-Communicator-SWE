package com.swe.ScreenNVideo.Playback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sound.sampled.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AudioPlayerTest {

    private SourceDataLine mockLine;

    @BeforeEach
    void setup() {
        mockLine = mock(SourceDataLine.class);
    }

    /**
     * Uses reflection to set the private field "line" in AudioPlayer.
     */
    private void injectMockLine(AudioPlayer ap, SourceDataLine mockLine) throws Exception {
        Field f = AudioPlayer.class.getDeclaredField("line");
        f.setAccessible(true);
        f.set(ap, mockLine);
    }

    @Test
    void testInitOpensAndStartsLine() throws Exception {
        try (MockedStatic<AudioSystem> audioSystem = Mockito.mockStatic(AudioSystem.class)) {

            audioSystem.when(() -> AudioSystem.getLine(any(DataLine.Info.class)))
                    .thenReturn(mockLine);

            AudioPlayer ap = new AudioPlayer(48000f, 1, 16);

            ap.init();

            verify(mockLine, times(1)).open(any(AudioFormat.class));
            verify(mockLine, times(1)).start();
        }
    }

    @Test
    void testPlayDropsAudioWhenBufferTooSmall() throws Exception {
        AudioPlayer ap = new AudioPlayer(44100f, 1, 16);

        injectMockLine(ap, mockLine);

        byte[] pcm = new byte[100];
        when(mockLine.available()).thenReturn(50);

        ap.play(pcm);

        verify(mockLine, never()).write(any(), anyInt(), anyInt());
    }

    @Test
    void testPlayWritesDataWhenEnoughSpace() throws Exception {
        AudioPlayer ap = new AudioPlayer(44100f, 1, 16);

        injectMockLine(ap, mockLine);

        byte[] pcm = new byte[100];
        when(mockLine.available()).thenReturn(200);

        ap.play(pcm);

        verify(mockLine, times(1)).write(pcm, 0, pcm.length);
    }

    @Test
    void testStopClosesResources() throws Exception {
        AudioPlayer ap = new AudioPlayer(44100f, 1, 16);

        injectMockLine(ap, mockLine);

        ap.stop();

        verify(mockLine, times(1)).drain();
        verify(mockLine, times(1)).stop();
        verify(mockLine, times(1)).close();
    }

    @Test
    void testInitThrowsLineUnavailableException() throws Exception {
        try (MockedStatic<AudioSystem> audioSystem = Mockito.mockStatic(AudioSystem.class)) {

            audioSystem.when(() -> AudioSystem.getLine(any(DataLine.Info.class)))
                    .thenThrow(new LineUnavailableException("Mocked failure"));

            AudioPlayer ap = new AudioPlayer(48000f, 1, 16);

            // Ensure the exception is propagated
            assertThrows(LineUnavailableException.class, ap::init);
        }
    }

    @Test
    void testPlayDoesNothingWhenLineIsNull() {
        AudioPlayer ap = new AudioPlayer(44100f, 1, 16);

        // do NOT inject mock line → line stays null
        byte[] pcm = new byte[50];

        // This should simply do nothing and not throw
        ap.play(pcm);

        // No exception = test passes
    }

    @Test
    void testStopDoesNothingWhenLineIsNull() {
        AudioPlayer ap = new AudioPlayer(44100f, 1, 16);

        // no line injected → line = null
        ap.stop();

        // Should not throw anything
    }

}
