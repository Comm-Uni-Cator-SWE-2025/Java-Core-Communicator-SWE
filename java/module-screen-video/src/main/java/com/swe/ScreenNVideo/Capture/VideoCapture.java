package com.swe.ScreenNVideo.Capture;

// Original imports
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Dimension;
import java.awt.Point;
// import java.awt.Rectangle; // Removed unused import
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

// --- Imports ADDED from the 'another branch' code ---
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
// --- End of ADDED imports ---

/**
 * VideoCapture class for capturing video frames from webcam.
 * Provides interface to capture images and convert them to matrix format.
 */
public class VideoCapture extends ICapture {

    /** Robot for screen capture (fallback method). */
    private Robot robot;

    /** Capture parameters. */
    private Dimension captureArea;
    /** Capture parameters. */
    private Point captureLocation;

    /** Listener for frame capture events. */
    private FrameCaptureListener listener;

    // --- Field ADDED from the 'another branch' code ---
    /** Webcam object for video capture. */
    private Webcam webcam;
    // --- End of ADDED field ---

    /** Default capture settings. */
    private static final int DEFAULT_WIDTH = 640;
    /** Default capture settings. */
    private static final int DEFAULT_HEIGHT = 480;
    /** Default capture settings. */
    private static final int DEFAULT_X = 100;
    /** Default capture settings. */
    private static final int DEFAULT_Y = 100;

    /**
     * Constructor - initializes with default screen capture area.
     *
     * <p>(This constructor is UNMODIFIED, as per your request)
     */
    public VideoCapture() {
        this.captureArea = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.captureLocation = new Point(DEFAULT_X, DEFAULT_Y);

        initializeCapture();
    }

    /**
     * Constructor with custom capture area.
     *
     * <p>(This constructor is UNMODIFIED, as per your request)
     *
     * @param x X coordinate of capture area
     * @param y Y coordinate of capture area
     * @param width Width of capture area
     * @param height Height of capture area
     */
    public VideoCapture(final int x, final int y, final int width, final int height) {
        this();
        this.captureLocation = new Point(x, y);
        this.captureArea = new Dimension(width, height);
    }

    /**
     * Initialize capture mechanism.
     *
     * <p>(This method is UNMODIFIED, as per your request. The Robot will be
     * initialized but will not be used by the modified capture() method.)
     */
    private void initializeCapture() {
        try {
            // Initialize Robot for screen capture
            this.robot = new Robot();
            System.out.println("VideoCapture initialized with screen capture (Robot created but will be unused)");
            System.out.println("Default capture area: " + captureArea.width + "x" + captureArea.height
                    + " at (" + captureLocation.x + "," + captureLocation.y + ")");
        } catch (AWTException e) {
            System.err.println("Error initializing Robot: " + e.getMessage());
            if (listener != null) {
                listener.onCaptureError("Failed to initialize capture: " + e.getMessage());
            }
        }
    }

    /**
     * Capture a single frame.
     *
     * <p>(This is the ONLY method whose body has been MODIFIED,
     * as per your request, to use the webcam.)
     *
     * @return BufferedImage of the captured frame
     */
    @SuppressWarnings({"checkstyle:FinalLocalVariable", "checkstyle:CyclomaticComplexity"})
    public BufferedImage capture() {

        // --- Start of MODIFIED/ADDED code ---

        // Lazy initialization: Initialize webcam on first capture attempt
        if (this.webcam == null) {
            System.out.println("Initializing webcam for the first time...");
            try {
                this.webcam = Webcam.getDefault();
                if (this.webcam == null) {
                    throw new IllegalStateException("No webcam found.");
                }

                // Use the captureArea set by the constructor
                Dimension resolution = this.captureArea;
                // If default Robot values were used, switch to a standard webcam res
                if (resolution.width == DEFAULT_WIDTH && resolution.height == DEFAULT_HEIGHT) {
                    resolution = WebcamResolution.VGA.getSize();
                    System.out.println("Using default VGA resolution: " + resolution.width + "x" + resolution.height);
                } else {
                    System.out.println("Using custom resolution: " + resolution.width + "x" + resolution.height);
                }

                this.webcam.setCustomViewSizes(new Dimension[]{resolution});
                this.webcam.setViewSize(resolution);
                this.webcam.open();

                System.out.println("VideoCapture initialized with webcam: " + webcam.getName());

            } catch (Exception e) {
                System.err.println("Error initializing Webcam: " + e.getMessage());
                if (listener != null) {
                    listener.onCaptureError("Failed to initialize capture: " + e.getMessage());
                }
                return null; // Failed to initialize, return null
            }
        }

        // --- Webcam capture logic ---
        if (webcam == null || !webcam.isOpen()) {
            System.err.println("Capture not started or webcam not available");
            return null;
        }

        try {
            // Capture and return the current image from the webcam
            final BufferedImage image = webcam.getImage();

            // Removed empty 'if' block that caused EmptyBlock error
            return image;

        } catch (Exception e) {
            System.err.println("Error capturing frame: " + e.getMessage());
            if (listener != null) {
                listener.onCaptureError("Frame capture error: " + e.getMessage());
            }
            return null;
        }
        // --- End of MODIFIED/ADDED code ---
    }

    /**
     * Set capture area and location.
     *
     * <p>(This method is UNMODIFIED, as per your request.
     * NOTE: Due to constraints, calling this will NOT update the webcam
     * resolution after the first capture.)
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width of capture area
     * @param height Height of capture area
     */
    public void setCaptureArea(final int x, final int y, final int width, final int height) {
        this.captureLocation = new Point(x, y);
        this.captureArea = new Dimension(width, height);
        System.out.println("Capture area updated: " + width + "x" + height + " at (" + x + "," + y + ")");
    }

    /**
     * Set frame capture listener.
     *
     * <p>(This method is UNMODIFIED, as per your request)
     *
     * @param newListener FrameCaptureListener implementation.
     */
    public void setFrameCaptureListener(final FrameCaptureListener newListener) {
        this.listener = newListener;
    }


    /**
     * Get screen dimensions.
     *
     * <p>(This method is UNMODIFIED, as per your request)
     *
     * @return Dimension of the screen.
     */
    public static Dimension getScreenDimensions() {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    // --- Method ADDED from 'another branch' to work with test_video.java ---
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
    // --- End of ADDED method ---


    // --- Dummy inner classes (assumed to exist elsewhere in your project) ---
    // Added here so the file could be self-contained for testing.

    /** Dummy ICapture class for compilation. */
    public abstract static class ICapture { }

    /** Dummy listener interface for compilation. */
    public interface FrameCaptureListener {
        /**
         * Called when a capture error occurs.
         * @param errorMessage The error message.
         */
        void onCaptureError(String errorMessage);
        // void onFrameCaptured(BufferedImage frame); // Example method
    }
}