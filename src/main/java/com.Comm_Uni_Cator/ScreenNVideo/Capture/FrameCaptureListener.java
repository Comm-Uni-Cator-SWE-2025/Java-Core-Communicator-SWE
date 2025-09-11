package com.Comm_Uni_Cator.ScreenNVideo.Capture;

/**
 * Interface for handling captured frames
 */
public interface FrameCaptureListener {
    void onCaptureError(String error);
    void onCaptureStarted();
    void onCaptureStopped();
}
