/**
 * Contributed by @alonot.
 */

package com.swe.ScreenNVideo;

import java.util.concurrent.ExecutionException;

/**
 * Interface to other modules.
 */
public interface CaptureManager {
    /**
     * Start Screen And Video Caputering service
     * Never returns.
     *
     * @param sendFPS The number of frames per second to capture and send.
     * @throws ExecutionException      If an error occurs during asynchronous execution.
     * @throws InterruptedException    If the operation is interrupted.
     */
    void startCapture(int sendFPS) throws ExecutionException, InterruptedException;

}
