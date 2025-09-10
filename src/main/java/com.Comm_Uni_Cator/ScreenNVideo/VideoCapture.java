package com.Comm_Uni_Cator.ScreenNVideo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * VideoCapture class for capturing video frames from webcam using pure Java
 * Provides interface to capture images and convert them to matrix format
 */
public class VideoCapture {

    // Robot for screen capture (fallback method)
    private Robot robot;

    // Capture parameters
    private boolean isCapturing;
    private List<BufferedImage> capturedFrames;
    private Dimension captureArea;
    private Point captureLocation;

    // Threading for continuous capture
    private ExecutorService executorService;
    private volatile boolean continuousCapture;

    /**
     * Interface for handling captured frames
     */
    public interface FrameCaptureListener {
        void onFrameCaptured(int[][] matrix, BufferedImage image);
        void onFramesCapture(List<int[][]> matrices, List<BufferedImage> images);
        void onCaptureError(String error);
        void onCaptureStarted();
        void onCaptureStopped();
    }

    /**
     * Interface for frame processing operations
     */
    public interface FrameProcessor {
        int[][] processFrame(BufferedImage image);
        boolean shouldProcessFrame(int frameNumber);
    }

    // Listener for frame capture events
    private FrameCaptureListener listener;
    private FrameProcessor processor;

    // Default capture settings
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    private static final int DEFAULT_X = 100;
    private static final int DEFAULT_Y = 100;

    /**
     * Constructor - initializes with default screen capture area
     */
    public VideoCapture() {
        this.capturedFrames = new ArrayList<>();
        this.isCapturing = false;
        this.continuousCapture = false;
        this.captureArea = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.captureLocation = new Point(DEFAULT_X, DEFAULT_Y);
        this.executorService = Executors.newSingleThreadExecutor();

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
     * Start video capture
     * @return true if capture started successfully, false otherwise
     */
    public boolean startCapture() {
        if (robot == null) {
            System.err.println("Capture not initialized properly");
            return false;
        }

        try {
            isCapturing = true;

            if (listener != null) {
                listener.onCaptureStarted();
            }

            System.out.println("Video capture started successfully");
            return true;

        } catch (Exception e) {
            System.err.println("Error starting capture: " + e.getMessage());
            if (listener != null) {
                listener.onCaptureError("Failed to start capture: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Stop video capture
     */
    public void stopCapture() {
        try {
            isCapturing = false;
            continuousCapture = false;

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }

            if (listener != null) {
                listener.onCaptureStopped();
            }

            System.out.println("Video capture stopped");

        } catch (Exception e) {
            System.err.println("Error stopping capture: " + e.getMessage());
        }
    }

    /**
     * Capture a single frame and return as matrix
     * @return 2D integer array representing the image in grayscale
     */
    public int[][] captureFrameAsMatrix() {
        BufferedImage image = captureFrame();
        if (image != null) {
            int[][] matrix = convertImageToMatrix(image);

            // Notify listener if available
            if (listener != null && matrix != null) {
                listener.onFrameCaptured(matrix, image);
            }

            return matrix;
        }
        return null;
    }

    /**
     * Capture a single frame
     * @return BufferedImage of the captured frame
     */
    public BufferedImage captureFrame() {
        if (!isCapturing || robot == null) {
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

            // Store captured frame
            capturedFrames.add(bufferedImage);

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
     * Convert BufferedImage to 2D integer matrix (grayscale)
     * @param image Input BufferedImage
     * @return 2D integer array representing grayscale values
     */
    private int[][] convertImageToMatrix(BufferedImage image) {
        if (image == null) {
            return null;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int[][] matrix = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get RGB value
                int rgb = image.getRGB(x, y);

                // Extract RGB components
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Convert to grayscale using standard formula
                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                matrix[y][x] = gray;
            }
        }

        return matrix;
    }

    /**
     * Convert BufferedImage to 3D RGB matrix
     * @param image Input BufferedImage
     * @return 3D integer array [height][width][3] representing RGB values
     */
    public int[][][] convertImageToRGBMatrix(BufferedImage image) {
        if (image == null) {
            return null;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int[][][] matrix = new int[height][width][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                matrix[y][x][0] = (rgb >> 16) & 0xFF; // Red
                matrix[y][x][1] = (rgb >> 8) & 0xFF;  // Green
                matrix[y][x][2] = rgb & 0xFF;         // Blue
            }
        }

        return matrix;
    }

    /**
     * Capture multiple frames continuously
     * @param numberOfFrames Number of frames to capture
     * @param intervalMs Interval between captures in milliseconds
     * @return List of captured frames as matrices
     */
    public List<int[][]> captureMultipleFrames(int numberOfFrames, long intervalMs) {
        List<int[][]> frames = new ArrayList<>();
        List<BufferedImage> images = new ArrayList<>();

        for (int i = 0; i < numberOfFrames && isCapturing; i++) {
            BufferedImage image = captureFrame();
            if (image != null) {
                int[][] matrix = convertImageToMatrix(image);
                if (matrix != null) {
                    frames.add(matrix);
                    images.add(image);
                    System.out.println("Captured frame " + (i + 1) + " of " + numberOfFrames);
                }
            }

            if (i < numberOfFrames - 1) {
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Notify listener about multiple frames
        if (listener != null && !frames.isEmpty()) {
            listener.onFramesCapture(frames, images);
        }

        return frames;
    }

    /**
     * Start continuous frame capture in background thread
     * @param intervalMs Interval between captures in milliseconds
     * @return Future for the continuous capture task
     */
    public Future<Void> startContinuousCapture(long intervalMs) {
        if (!isCapturing) {
            System.err.println("Cannot start continuous capture - main capture not started");
            return null;
        }

        continuousCapture = true;

        return CompletableFuture.runAsync(() -> {
            int frameCount = 0;
            while (continuousCapture && isCapturing) {
                try {
                    int[][] matrix = captureFrameAsMatrix();
                    if (matrix != null) {
                        frameCount++;
                        System.out.println("Continuous capture - Frame: " + frameCount);

                        // Apply custom processing if processor is set
                        if (processor != null && processor.shouldProcessFrame(frameCount)) {
                            BufferedImage image = capturedFrames.get(capturedFrames.size() - 1);
                            int[][] processedMatrix = processor.processFrame(image);
                            if (processedMatrix != null) {
                                matrix = processedMatrix;
                            }
                        }
                    }

                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in continuous capture: " + e.getMessage());
                    if (listener != null) {
                        listener.onCaptureError("Continuous capture error: " + e.getMessage());
                    }
                }
            }
        }, executorService);
    }

    /**
     * Stop continuous capture
     */
    public void stopContinuousCapture() {
        continuousCapture = false;
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
     * Get image dimensions
     * @return Dimension object with width and height
     */
    public Dimension getImageDimensions() {
        return new Dimension(captureArea.width, captureArea.height);
    }

    /**
     * Check if capture is active
     * @return true if capturing, false otherwise
     */
    public boolean isCapturing() {
        return isCapturing;
    }

    /**
     * Check if continuous capture is active
     * @return true if continuous capturing, false otherwise
     */
    public boolean isContinuousCapturing() {
        return continuousCapture;
    }

    /**
     * Get all captured frames
     * @return List of BufferedImages
     */
    public List<BufferedImage> getCapturedFrames() {
        return new ArrayList<>(capturedFrames);
    }

    /**
     * Get captured frames as matrices
     * @return List of 2D integer matrices
     */
    public List<int[][]> getCapturedFramesAsMatrices() {
        List<int[][]> matrices = new ArrayList<>();
        for (BufferedImage image : capturedFrames) {
            int[][] matrix = convertImageToMatrix(image);
            if (matrix != null) {
                matrices.add(matrix);
            }
        }
        return matrices;
    }

    /**
     * Clear captured frames from memory
     */
    public void clearCapturedFrames() {
        capturedFrames.clear();
        System.gc(); // Suggest garbage collection
        System.out.println("Captured frames cleared from memory");
    }

    /**
     * Set frame capture listener
     * @param listener FrameCaptureListener implementation
     */
    public void setFrameCaptureListener(FrameCaptureListener listener) {
        this.listener = listener;
    }

    /**
     * Set frame processor for custom processing
     * @param processor FrameProcessor implementation
     */
    public void setFrameProcessor(FrameProcessor processor) {
        this.processor = processor;
    }

    /**
     * Get screen dimensions
     * @return Dimension of the screen
     */
    public static Dimension getScreenDimensions() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    /**
     * Print matrix values (for debugging)
     * @param matrix 2D integer matrix
     * @param maxRows Maximum rows to print
     * @param maxCols Maximum columns to print
     */
    public static void printMatrix(int[][] matrix, int maxRows, int maxCols) {
        if (matrix == null) {
            System.out.println("Matrix is null");
            return;
        }

        System.out.println("Matrix dimensions: " + matrix.length + "x" +
                (matrix.length > 0 ? matrix[0].length : 0));

        for (int i = 0; i < Math.min(maxRows, matrix.length); i++) {
            for (int j = 0; j < Math.min(maxCols, matrix[i].length); j++) {
                System.out.printf("%3d ", matrix[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Create a simple brightness-based frame processor
     * @param brightnessThreshold Minimum brightness level (0-255)
     * @return FrameProcessor that filters based on brightness
     */
    public static FrameProcessor createBrightnessProcessor(int brightnessThreshold) {
        return new FrameProcessor() {
            @Override
            public int[][] processFrame(BufferedImage image) {
                int width = image.getWidth();
                int height = image.getHeight();
                int[][] matrix = new int[height][width];

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = image.getRGB(x, y);
                        int red = (rgb >> 16) & 0xFF;
                        int green = (rgb >> 8) & 0xFF;
                        int blue = rgb & 0xFF;

                        int brightness = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                        matrix[y][x] = brightness > brightnessThreshold ? 255 : 0;
                    }
                }
                return matrix;
            }

            @Override
            public boolean shouldProcessFrame(int frameNumber) {
                return frameNumber % 2 == 0; // Process every other frame
            }
        };
    }

    /**
     * Example usage and testing
     */
    public static void main(String[] args) {
        System.out.println("VideoCapture Test Started");
        System.out.println("Screen size: " + getScreenDimensions());

        // Create VideoCapture instance with custom area
        VideoCapture capture = new VideoCapture(0, 0, 800, 600);

        // Set up listener
        capture.setFrameCaptureListener(new FrameCaptureListener() {
            @Override
            public void onFrameCaptured(int[][] matrix, BufferedImage image) {
                System.out.println("Frame captured - Matrix size: " +
                        matrix.length + "x" + matrix[0].length);
            }

            @Override
            public void onFramesCapture(List<int[][]> matrices, List<BufferedImage> images) {
                System.out.println("Multiple frames captured: " + matrices.size());
            }

            @Override
            public void onCaptureError(String error) {
                System.err.println("Capture error: " + error);
            }

            @Override
            public void onCaptureStarted() {
                System.out.println("Capture started!");
            }

            @Override
            public void onCaptureStopped() {
                System.out.println("Capture stopped!");
            }
        });

        // Start capture
        if (capture.startCapture()) {
            try {
                System.out.println("Testing single frame capture...");

                // Test single frame capture
                int[][] matrix = capture.captureFrameAsMatrix();
                if (matrix != null) {
                    System.out.println("Single frame captured successfully!");
                    printMatrix(matrix, 5, 5);
                }

                Thread.sleep(1000);

                // Test multiple frames capture
                System.out.println("Testing multiple frames capture...");
                List<int[][]> frames = capture.captureMultipleFrames(3, 500);
                System.out.println("Captured " + frames.size() + " frames");

                // Test continuous capture
                System.out.println("Testing continuous capture for 5 seconds...");
                Future<Void> continuousTask = capture.startContinuousCapture(1000);
                Thread.sleep(5000);
                capture.stopContinuousCapture();

                System.out.println("Test completed successfully!");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Test interrupted");
            } finally {
                capture.stopCapture();
            }
        } else {
            System.err.println("Failed to start video capture");
        }
    }
}