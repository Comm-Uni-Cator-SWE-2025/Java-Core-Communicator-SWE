package com.swe.ScreenNVideo.Capture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests for ScreenCapture.
 * Mocks the java.awt.Robot class to test capture logic without real hardware.
 */
class ScreenCaptureTest {

    /**
     * System under test.
     */
    private ScreenCapture screenCapture;

    /**
     * Mocked Robot instance.
     */
    private Robot mockRobot;

    @BeforeEach
    void setUp() throws Exception {
        try {
            screenCapture = new ScreenCapture();
        } catch (Exception e) {
            // Handle headless CI environments gracefully
            if (GraphicsEnvironment.isHeadless()) {
                System.out.println("Skipping ScreenCapture instantiation in headless mode");
                return;
            }
            throw e;
        }

        // Inject mock robot
        mockRobot = mock(Robot.class);
        final Field robotField = ScreenCapture.class.getDeclaredField("robot");
        robotField.setAccessible(true);
        robotField.set(screenCapture, mockRobot);
    }

    @Test
    void testCaptureSuccess() throws AWTException {
        if (screenCapture == null) {
            return;
        }

        final BufferedImage expectedImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        when(mockRobot.createScreenCapture(any())).thenReturn(expectedImage);

        final BufferedImage actualImage = screenCapture.capture();

        assertNotNull(actualImage);
        assertEquals(expectedImage, actualImage);
    }

    @Test
    void testCaptureTimeout() throws Exception {
        if (screenCapture == null) {
            return;
        }

        // Delay the mock response to trigger timeout
        when(mockRobot.createScreenCapture(any())).thenAnswer(invocation -> {
            Thread.sleep(6000);
            return null;
        });

        final AWTException exception = assertThrows(AWTException.class, () -> {
            screenCapture.capture();
        });

        assertTrue(exception.getMessage().contains("timed out"));
    }

    @Test
    void testCaptureInterrupted() throws Exception {
        if (screenCapture == null) {
            return;
        }

        // Block inside the mock
        when(mockRobot.createScreenCapture(any())).thenAnswer(invocation -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
                // Ignore internal interruption
            }
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        });

        // Interrupt the main thread from a separate thread
        final Thread testThread = Thread.currentThread();
        final Thread interrupter = new Thread(() -> {
            try {
                Thread.sleep(200);
                testThread.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        interrupter.start();

        final AWTException exception = assertThrows(AWTException.class, () -> {
            screenCapture.capture();
        });

        // Clear interrupt flag
        Thread.interrupted();

        assertTrue(exception.getMessage().contains("was interrupted"));
    }

    @Test
    void testCaptureGenericException() throws Exception {
        if (screenCapture == null) {
            return;
        }

        when(mockRobot.createScreenCapture(any())).thenThrow(new RuntimeException("Generic error"));

        final AWTException exception = assertThrows(AWTException.class, () -> {
            screenCapture.capture();
        });

        assertTrue(exception.getMessage().contains("failed"));
    }

    @Test
    void testStop() throws NoSuchFieldException, IllegalAccessException {
        if (screenCapture == null) {
            return;
        }

        screenCapture.stop();

        final Field robotField = ScreenCapture.class.getDeclaredField("robot");
        robotField.setAccessible(true);
        assertNull(robotField.get(screenCapture), "Robot should be null after stop");
    }

    @Test
    void testCaptureWithNullRobot() throws Exception {
        if (screenCapture == null) {
            return;
        }

        // Set robot to null to force reInit
        screenCapture.stop();

        try {
            final BufferedImage result = screenCapture.capture();
            assertNotNull(result);
        } catch (Exception e) {
            // Even if capture fails, check that reInit happened (robot is not null)
            final Field robotField = ScreenCapture.class.getDeclaredField("robot");
            robotField.setAccessible(true);
            assertNotNull(robotField.get(screenCapture), "Robot should be re-initialized");
        }
    }

    @Test
    void testReInit() {
        if (screenCapture == null || GraphicsEnvironment.isHeadless()) {
            return;
        }
        assertDoesNotThrow(() -> screenCapture.reInit());
    }

    @Test
    void testReInitAWTExceptionCoverage() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        // Mock static GraphicsEnvironment to force AWTException in constructor
        try (MockedStatic<GraphicsEnvironment> mockedStaticEnv = mockStatic(GraphicsEnvironment.class)) {
            final GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            mockedStaticEnv.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);

            when(mockEnv.isHeadlessInstance()).thenReturn(true);

            final GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            when(mockEnv.getScreenDevices()).thenReturn(new GraphicsDevice[]{mockDevice});

            final RuntimeException exception = assertThrows(RuntimeException.class, () -> screenCapture.reInit());

            assertNotNull(exception.getCause());
            assertEquals(AWTException.class, exception.getCause().getClass());
        }
    }
}
