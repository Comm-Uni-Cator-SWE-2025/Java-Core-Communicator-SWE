package com.Comm_Uni_Cator.ScreenNVideo.Capture;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * VideoCapture class for capturing video frames from webcam using pure Java
 * Provides interface to capture images and convert them to matrix format
 */
public class VideoCapture extends ICapture {

    // Robot for screen capture (fallback method)
    private Robot robot;

    // Capture parameters
    private Dimension captureArea;
    private Point captureLocation;

    // Listener for frame capture events
    private FrameCaptureListener listener;

    // Default capture settings
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    private static final int DEFAULT_X = 100;
    private static final int DEFAULT_Y = 100;

    /**
     * Constructor - initializes with default screen capture area
     */
    public VideoCapture() {
        this.captureArea = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.captureLocation = new Point(DEFAULT_X, DEFAULT_Y);

        initializeCapture();
    }

    /**
     * Constructor with custom capture area
     * @param x X coordinate of capture area
     * @param y Y coordinate of capture area
     * @param width Width of capture area
     * @param height Height of capture area
     */
    public VideoCapture(int x, int y, int width, int height) {
        this();
        this.captureLocation = new Point(x, y);
        this.captureArea = new Dimension(width, height);
    }

    /**
     * Initialize capture mechanism
     */
    private void initializeCapture() {
        try {
            // Initialize Robot for screen capture
            this.robot = new Robot();
            System.out.println("VideoCapture initialized with screen capture");
            System.out.println("Capture area: " + captureArea.width + "x" + captureArea.height +
                    " at (" + captureLocation.x + "," + captureLocation.y + ")");
        } catch (AWTException e) {
            System.err.println("Error initializing Robot: " + e.getMessage());
            if (listener != null) {
                listener.onCaptureError("Failed to initialize capture: " + e.getMessage());
            }
        }
    }

    /**
     * Capture a single frame
     * @return BufferedImage of the captured frame
     */
    public BufferedImage capture() {
        if (robot == null) {
            System.err.println("Capture not started or robot not available");
            return null;
        }

        try {
            // Create rectangle for capture area
            Rectangle captureRect = new Rectangle(
                    captureLocation.x,
                    captureLocation.y,
                    captureArea.width,
                    captureArea.height
            );

            // Capture screen area
            BufferedImage bufferedImage = robot.createScreenCapture(captureRect);

            return bufferedImage;

        } catch (Exception e) {
            System.err.println("Error capturing frame: " + e.getMessage());
            if (listener != null) {
                listener.onCaptureError("Frame capture error: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Set capture area and location
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width of capture area
     * @param height Height of capture area
     */
    public void setCaptureArea(int x, int y, int width, int height) {
        this.captureLocation = new Point(x, y);
        this.captureArea = new Dimension(width, height);
        System.out.println("Capture area updated: " + width + "x" + height +
                " at (" + x + "," + y + ")");
    }

    /**
     * Set frame capture listener
     * @param listener FrameCaptureListener implementation
     */
    public void setFrameCaptureListener(FrameCaptureListener listener) {
        this.listener = listener;
    }


    /**
     * Get screen dimensions
     * @return Dimension of the screen
     */
    public static Dimension getScreenDimensions() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }
}