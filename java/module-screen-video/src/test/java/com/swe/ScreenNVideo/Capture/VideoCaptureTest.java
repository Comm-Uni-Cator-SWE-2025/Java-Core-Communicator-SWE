/**
 * Contributed by @Bhupathi-Varun
 */

package com.swe.ScreenNVideo.Capture;

import com.github.sarxos.webcam.Webcam;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoCaptureTest {

    private VideoCapture videoCapture;

    // Mocking the hardware Webcam object
    @Mock
    private Webcam mockWebcam;

    // Mocking the listener to verify error handling
    @Mock
    private FrameCaptureListener mockListener;

    @BeforeEach
    void setUp() {
        videoCapture = new VideoCapture();
    }

    @AfterEach
    void tearDown() {
        videoCapture = null;
    }

    /**
     * Helper method to inject the Mock Webcam into the private 'webcam' field.
     */
    private void injectMockWebcam(Webcam webcam) {
        try {
            Field webcamField = VideoCapture.class.getDeclaredField("webcam");
            webcamField.setAccessible(true);
            webcamField.set(videoCapture, webcam);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject mock webcam via Reflection: " + e.getMessage());
        }
    }

    // =================================================================
    // 1. Initialization, Configuration & Getters
    // =================================================================

    @Test
    @DisplayName("Should update capture area and verify with getter")
    void testSetCaptureArea() {
        int x = 10, y = 10, w = 1920, h = 1080;

        // Test Setter
        assertDoesNotThrow(() -> videoCapture.setCaptureArea(x, y, w, h));

        // Test Getter (Covers getCaptureLocation)
        Point loc = videoCapture.getCaptureLocation();
        assertNotNull(loc);
        assertEquals(x, loc.x);
        assertEquals(y, loc.y);
    }

    @Test
    @DisplayName("Should retrieve screen dimensions (Static method coverage)")
    void testGetScreenDimensions() {
        Dimension dim = VideoCapture.getScreenDimensions();
        assertNotNull(dim);
        assertTrue(dim.width > 0);
        assertTrue(dim.height > 0);
    }

    @Test
    @DisplayName("reInit() should attempt to open webcam safely")
    void testReInit_Coverage() {
        // We set a listener to catch potential errors during the real hardware lookup
        videoCapture.setFrameCaptureListener(mockListener);

        // Action: Call reInit.
        // This covers the openWebcam() method. Even if it fails (no camera),
        // it catches the exception and prints/notifies, so it shouldn't throw here.
        assertDoesNotThrow(() -> videoCapture.reInit());
    }

    // =================================================================
    // 2. Resource Management Tests (Stop/Close)
    // =================================================================

    @Test
    @DisplayName("Stop() should close the webcam if it is currently open")
    void testStop_ClosesWebcamIfOpen() {
        injectMockWebcam(mockWebcam);
        when(mockWebcam.isOpen()).thenReturn(true);

        videoCapture.stop();

        verify(mockWebcam, times(1)).close();
    }

    @Test
    @DisplayName("Stop() should do nothing if webcam is already closed")
    void testStop_DoesNothingIfClosed() {
        injectMockWebcam(mockWebcam);
        when(mockWebcam.isOpen()).thenReturn(false);

        videoCapture.stop();

        verify(mockWebcam, never()).close();
    }

    @Test
    @DisplayName("Stop() should be safe even if webcam object is null")
    void testStop_HandlesNullWebcam() {
        injectMockWebcam(null);
        assertDoesNotThrow(() -> videoCapture.stop());
    }

    // =================================================================
    // 3. Image Capture Logic Tests
    // =================================================================

    @Test
    @DisplayName("Capture() should return image from webcam when open")
    void testCapture_HappyPath() {
        injectMockWebcam(mockWebcam);
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        when(mockWebcam.isOpen()).thenReturn(true);
        when(mockWebcam.getImage()).thenReturn(dummyImage);

        BufferedImage result = videoCapture.capture();

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        verify(mockWebcam, times(1)).getImage();
    }


    @Test
    @DisplayName("Capture() should return null if webcam is closed")
    void testCapture_ReturnsNullIfClosed() {
        injectMockWebcam(mockWebcam);
        when(mockWebcam.isOpen()).thenReturn(false);

        BufferedImage result = videoCapture.capture();

        assertNull(result);
        verify(mockWebcam, never()).getImage();
    }

    @Test
    @DisplayName("Capture() should handle exceptions and notify listener")
    void testCapture_ExceptionHandling() {
        injectMockWebcam(mockWebcam);
        videoCapture.setFrameCaptureListener(mockListener);

        // Scenario: Webcam is open, but getImage throws a RuntimeException
        when(mockWebcam.isOpen()).thenReturn(true);
        when(mockWebcam.getImage()).thenThrow(new RuntimeException("Camera disconnected"));

        // Action
        BufferedImage result = videoCapture.capture();

        // Verify result is null and listener was called
        assertNull(result);
        verify(mockListener).onCaptureError(contains("Camera disconnected"));
    }

    // =================================================================
    // 4. Complex Data Processing & Static Utilities
    // =================================================================

    @Test
    @DisplayName("CaptureAsRGBMatrix should correctly convert Colors to Int Arrays")
    void testCaptureAsRGBMatrix_DataIntegrity() throws AWTException {
        // Fix: Added 'throws AWTException' to signature
        VideoCapture spyVideoCapture = spy(new VideoCapture());

        BufferedImage testImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        testImage.setRGB(0, 0, 0xFF0000); // Red
        testImage.setRGB(1, 0, 0x00FF00); // Green
        testImage.setRGB(0, 1, 0x0000FF); // Blue
        testImage.setRGB(1, 1, 0xFFFFFF); // White

        doReturn(testImage).when(spyVideoCapture).capture();

        int[][][] matrix = spyVideoCapture.captureAsRGBMatrix();

        assertEquals(2, matrix.length);
        assertArrayEquals(new int[]{255, 0, 0}, matrix[0][0], "Pixel (0,0) should be Red");
        assertArrayEquals(new int[]{0, 255, 0}, matrix[0][1], "Pixel (1,0) should be Green");
    }

    @Test
    @DisplayName("EnsureIntARGB: Should convert RGB to ARGB")
    void testEnsureIntARGB_Conversion() {
        BufferedImage rgbImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);

        BufferedImage result = VideoCapture.ensureIntARGB(rgbImage);

        assertNotNull(result);
        assertEquals(BufferedImage.TYPE_INT_ARGB, result.getType());
        assertEquals(50, result.getWidth());
    }

    @Test
    @DisplayName("EnsureIntARGB: Should optimize if already ARGB")
    void testEnsureIntARGB_Optimization() {
        // Covers the 'if (src.getType() == BufferedImage.TYPE_INT_ARGB)' branch
        BufferedImage argbImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);

        BufferedImage result = VideoCapture.ensureIntARGB(argbImage);

        assertNotNull(result);
        assertEquals(BufferedImage.TYPE_INT_ARGB, result.getType());
        // Verify it returns a new instance (clone), not the exact same object
        assertNotSame(argbImage, result);
    }

    // =================================================================
    // OPEN WEBCAM LOGIC TESTS
    // =================================================================

    @Test
    @DisplayName("openWebcam: Should handle missing device gracefully (Error Path)")
    void testOpenWebcam_NoDeviceFound() {
        // Scenario: Webcam.getDefault() returns null (Simulating no camera connected)
        // We do NOT use static mocks here to simulate the natural failure state of a CI server.

        videoCapture.setFrameCaptureListener(mockListener);

        // Action
        assertDoesNotThrow(() -> videoCapture.reInit());

        // Verification
        // Since webcam is null, the code throws IllegalStateException("No webcam found...")
        // inside the try block. The catch block should catch it and notify the listener.

        // Note: If you are running this on a laptop with a real camera, this test might
        // actually succeed (find a camera). To force failure without static mocks,
        // we check if the listener was called ONLY if webcam remains null.
        try {
            Field webcamField = VideoCapture.class.getDeclaredField("webcam");
            webcamField.setAccessible(true);
            Object currentWebcam = webcamField.get(videoCapture);

            if (currentWebcam == null) {
                // If no camera was found, we MUST have hit the error listener
                verify(mockListener).onCaptureError(contains("No webcam found"));
            }
        } catch (Exception e) {
            fail("Reflection failed");
        }
    }
}