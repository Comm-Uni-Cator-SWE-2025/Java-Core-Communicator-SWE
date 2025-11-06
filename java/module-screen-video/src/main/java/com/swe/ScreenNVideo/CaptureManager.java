package com.swe.ScreenNVideo;

import java.util.concurrent.ExecutionException;

/**
 * Interface to other modules.
 */
public interface CaptureManager {
    /**
     * Start Screen And Video Caputering service
     * Never returns.
     */
    void startCapture() throws ExecutionException, InterruptedException;

    /**
     * For the Controller module to call this every time a
     * new participant joins.
     * @param ip IP of the new participant
     */
    void newParticipantJoined(String ip);
}
