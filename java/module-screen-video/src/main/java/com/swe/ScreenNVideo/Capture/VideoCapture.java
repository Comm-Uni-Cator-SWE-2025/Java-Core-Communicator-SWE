package com.swe.ScreenNVideo.Capture;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 * VideoCapture class for capturing video frames from a webcam.
 * Provides an interface to capture images from the default webcam.
 */
public class VideoCapture extends ICapture {

    /** Webcam object for video capture. */
    private Webcam webcam;

    /** Capture parameters (used for resolution). */
    private Dimension captureArea;
    /** Capture parameters (not used for webcam). */
    private Point captureLocation;

    /** Listener for frame capture events. */
    private FrameCaptureListener listener;

    /** Default capture settings. */
    private static final int DEFAULT_WIDTH = 640;
    /** Default capture settings. */
    private static final int DEFAULT_HEIGHT = 480;

    /**
     * Constructor - initializes with default webcam and resolution.
     */
    public VideoCapture() {
        // Use a standard resolution like VGA by default
        this.captureArea = WebcamResolution.VGA.getSize();
        // Location is not relevant for a webcam, but we initialize it for consistency
        this.captureLocation = new Point(0, 0);

        initializeCapture();
    }

    /**
     * Constructor with custom capture resolution.
     * @param x X coordinate (ignored for webcam)
     * @param y Y coordinate (ignored for webcam)
     * @param width Width of capture resolution
     * @param height Height of capture resolution
     */
    public VideoCapture(final int x, final int y, final int width, final int height) {
        this();
        // x and y are ignored as they are not applicable for a webcam
        this.captureArea = new Dimension(width, height);
        // Re-initialize with the new custom resolution
        if (this.webcam != null && this.webcam.isOpen()) {
            this.webcam.close();
        }
        initializeCapture();
    }

    /**
     * Initialize webcam capture mechanism.
     */
    private void initializeCapture() {
        try {
            // Get the default webcam
            this.webcam = Webcam.getDefault();
            if (this.webcam == null) {
                throw new IllegalStateException("No webcam found.");
            }
            // Set custom view size
            this.webcam.setCustomViewSizes(new Dimension[]{captureArea});
            this.webcam.setViewSize(captureArea);
            // Open the webcam
            this.webcam.open();
            System.out.println("VideoCapture initialized with webcam: " + webcam.getName());
            System.out.println("Capture resolution: " + captureArea.width + "x" + captureArea.height);

        } catch (Exception e) {
            System.err.println("Error initializing Webcam: " + e.getMessage());
            if (listener != null) {
                listener.onCaptureError("Failed to initialize capture: " + e.getMessage());
            }
        }
    }

    /**
     * Capture a single frame from the webcam.
     * @return BufferedImage of the captured frame, or null if an error occurs.
     */
    public BufferedImage capture() {
        if (webcam == null || !webcam.isOpen()) {
            System.err.println("Capture not started or webcam not available");
            return null;
        }

        try {
            // Capture and return the current image from the webcam
            BufferedImage image = webcam.getImage();
            if (image != null && listener != null) {
                // Assuming FrameCaptureListener has an onFrameCaptured method
                // listener.onFrameCaptured(image);
            }
            return image;

        } catch (Exception e) {
            System.err.println("Error capturing frame: " + e.getMessage());
            if (listener != null) {
                listener.onCaptureError("Frame capture error: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Set capture resolution.
     * @param x X coordinate (ignored)
     * @param y Y coordinate (ignored)
     * @param width Width of capture area
     * @param height Height of capture area
     */
    public void setCaptureArea(final int x, final int y, final int width, final int height) {
        this.captureArea = new Dimension(width, height);
        // Re-initialize the webcam with the new resolution
        if (this.webcam != null) {
            if(this.webcam.isOpen()) {
                this.webcam.close();
            }
            initializeCapture();
            System.out.println("Capture resolution updated: " + width + "x" + height);
        }
    }

    /**
     * Closes the webcam to release the resource.
     * This is an important new method to call when you are done.
     */
    public void close() {
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            System.out.println("Webcam closed.");
        }
    }

    /**
     * Set frame capture listener.
     * @param newListener FrameCaptureListener implementation.
     */
    public void setFrameCaptureListener(final FrameCaptureListener newListener) {
        this.listener = newListener;
    }

    /**
     * Get screen dimensions.
     * @return Dimension of the screen.
     */
    public static Dimension getScreenDimensions() {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    // --- The following classes are assumed to exist based on the original code ---

    /** Dummy ICapture class for compilation. */
    public static abstract class ICapture {}

    /** Dummy listener interface for compilation. */
    public interface FrameCaptureListener {
        void onCaptureError(String errorMessage);
        // void onFrameCaptured(BufferedImage frame); // Example method
    }
}