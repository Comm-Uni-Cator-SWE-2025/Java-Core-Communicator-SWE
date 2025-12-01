/**
 * Contributed by @sandeep-kumar.
 */

package com.swe.ScreenNVideo.Capture;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.swe.ScreenNVideo.Telemetry.Telemetry;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The {@code ScreenCapture} class is responsible for capturing
 * the entire screen as a {@link BufferedImage}.
 *
 * <p>It uses the {@link Robot} class to create a screenshot of the
 * current display. Works on Windows, macOS, and Linux.
 */
public class ScreenCapture extends ICapture {

    /** The {@link Robot} used to capture the screen image. */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("SCREEN-VIDEO");
    /** robot instance to capture screen. */
    private Robot robot;

    /** Cached screen rectangle to avoid repeated Toolkit calls. */
    private Rectangle screenRect;

    /** Executor for timeout protection. */
    private static final ExecutorService TIMEOUT_EXECUTOR = Executors.newCachedThreadPool(r -> {
        final Thread t = new Thread(r, "ScreenCapture-Timeout-Thread");
        t.setDaemon(true);
        return t;
    });

    /** Timeout for capture operations in seconds. */
    private static final int CAPTURE_TIMEOUT_SECONDS = 5;

    public ScreenCapture() {
    }

    /**
     * Captures the entire screen and returns it as a BufferedImage.
     *
     * @return BufferedImage containing the screenshot
     * @throws AWTException if the platform configuration does not allow low-level input control
     */
    @Override
    public BufferedImage capture() throws AWTException {
        Telemetry.getTelemetry().setWithScreen(true);
        if (robot == null) {
            reInit();
        }

        // Capture with timeout protection
        return executeWithTimeout(
            () -> robot.createScreenCapture(screenRect)
        );
    }

    /**
     * Executes a task with timeout protection.
     *
     * @param <T>  The return type of the task
     * @param task The task to execute
     * @return The result of the task
     * @throws AWTException if the operation times out, is interrupted, or fails
     */
    private <T> T executeWithTimeout(final Callable<T> task) throws AWTException {
        try {
            final Future<T> future = TIMEOUT_EXECUTOR.submit(task);
            return future.get(CAPTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            final String message = "Screen capture" + " timed out after " + CAPTURE_TIMEOUT_SECONDS + " seconds";
            LOG.error(message);
            reInit();
            throw new AWTException(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AWTException("Screen capture" + " was interrupted: " + e.getMessage());
        } catch (Exception e) {
            throw new AWTException("Screen capture" + " failed: " + e.getMessage());
        }
    }

    @Override
    public void reInit() {
        try {
            final GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            robot = new Robot(screens[0]);
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            screenRect = new Rectangle(screenSize);
            LOG.info("Reinitialized");
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        robot = null;
    }
}
