/**
 * Contributed by @aman112201041
 */

package com.swe.ScreenNVideo.Capture;

/**
 * Interface for audio capture functionality.
 * Provides methods to initialize, capture, stop, and reinitialize audio recording.
 */
public interface IAudioCapture {

    /**
     * Initializes the audio capture system.
     *
     * @return true if initialization is successful, false otherwise
     */
    boolean init();

    /**
     * Captures the next chunk of audio data.
     *
     * @return a byte array containing the captured audio data
     */
    byte[] getChunk();


    /**
     * Stops the audio capture and releases resources.
     */
    void stop();

    /**
     * Reinitializes the audio capture (used if input device changes or fails).
     *
     * @return true if reinitialization is successful, false otherwise
     */
    boolean reinit();
}
