package com.swe.ScreenNVideo.Capture;

import com.swe.ScreenNVideo.CaptureComponents;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for BackgroundCaptureManager.
 * Verifies thread management and capture loop logic.
 */
class BackgroundCaptureManagerTest {

    @Mock
    private CaptureComponents mockCapCom;

    @Mock
    private ICapture mockScreenCapture;

    @Mock
    private ICapture mockVideoCapture;

    private BackgroundCaptureManager manager;
    private Thread managerThread;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        manager = new BackgroundCaptureManager(mockCapCom);

        // Inject mocks
        setPrivateField(manager, "screenCapture", mockScreenCapture);
        setPrivateField(manager, "videoCapture", mockVideoCapture);
    }

    @AfterEach
    void tearDown() {
        // Ensure thread cleanup
        if (managerThread != null && managerThread.isAlive()) {
            managerThread.interrupt();
        }
        try {
            final Field threadField = BackgroundCaptureManager.class.getDeclaredField("captureThread");
            threadField.setAccessible(true);
            final Thread t = (Thread) threadField.get(null);
            if (t != null) {
                t.interrupt();
            }
            threadField.set(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testStartThread() throws Exception {
        manager.start();

        final Field threadField = BackgroundCaptureManager.class.getDeclaredField("captureThread");
        threadField.setAccessible(true);
        managerThread = (Thread) threadField.get(null);

        assertNotNull(managerThread);
        assertTrue(managerThread.isAlive());
        assertTrue(managerThread.isDaemon());
    }

    @Test
    void testStartAlreadyRunning() throws Exception {
        manager.start();

        final Field threadField = BackgroundCaptureManager.class.getDeclaredField("captureThread");
        threadField.setAccessible(true);
        final Thread firstThread = (Thread) threadField.get(null);

        assertNotNull(firstThread, "Thread should be initialized");
        assertTrue(firstThread.isAlive(), "Thread should be running");

        // Call start again, should not create new thread
        manager.start();

        final Thread secondThread = (Thread) threadField.get(null);
        assertSame(firstThread, secondThread, "Should return if already running");

        managerThread = firstThread;
    }

    @Test
    void testStartWithDeadThread() throws Exception {
        manager.start();

        final Field threadField = BackgroundCaptureManager.class.getDeclaredField("captureThread");
        threadField.setAccessible(true);
        final Thread deadThread = (Thread) threadField.get(null);

        // Simulate thread death
        deadThread.interrupt();
        deadThread.join(2000);

        assertFalse(deadThread.isAlive(), "Thread should be dead");
        assertNotNull(threadField.get(null), "Static field should still reference dead thread");

        // Restart, should create new thread
        manager.start();

        final Thread newThread = (Thread) threadField.get(null);
        assertNotNull(newThread);
        assertNotSame(deadThread, newThread, "Should replace dead thread");
        assertTrue(newThread.isAlive());

        managerThread = newThread;
    }

    @Test
    void testLoopBothCapturesOnSuccess() throws Exception {
        when(mockCapCom.isScreenCaptureOn()).thenReturn(true);
        when(mockCapCom.isVideoCaptureOn()).thenReturn(true);

        final BufferedImage mockImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        when(mockScreenCapture.capture()).thenReturn(mockImg);
        when(mockVideoCapture.capture()).thenReturn(mockImg);

        manager.start();

        verify(mockCapCom, timeout(1000).atLeastOnce()).setLatestScreenFrame(mockImg);
        verify(mockCapCom, timeout(1000).atLeastOnce()).setLatestVideoFrame(mockImg);
    }

    @Test
    void testLoopBothCapturesOffStops() throws Exception {
        when(mockCapCom.isScreenCaptureOn()).thenReturn(false);
        when(mockCapCom.isVideoCaptureOn()).thenReturn(false);

        manager.start();

        verify(mockScreenCapture, timeout(1000).atLeastOnce()).stop();
        verify(mockVideoCapture, timeout(1000).atLeastOnce()).stop();
    }

    @Test
    void testLoopOnlyVideoOn() throws Exception {
        when(mockCapCom.isScreenCaptureOn()).thenReturn(false);
        when(mockCapCom.isVideoCaptureOn()).thenReturn(true);

        final BufferedImage mockImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        when(mockVideoCapture.capture()).thenReturn(mockImg);

        manager.start();

        verify(mockCapCom, timeout(1000).atLeastOnce()).setLatestVideoFrame(mockImg);
        verify(mockScreenCapture, timeout(1000).atLeastOnce()).stop();
        verify(mockCapCom, timeout(1000).atLeastOnce()).setLatestScreenFrame(null);
    }

    @Test
    void testLoopExceptionsHandled() throws Exception {
        when(mockCapCom.isScreenCaptureOn()).thenReturn(true);
        when(mockCapCom.isVideoCaptureOn()).thenReturn(true);

        when(mockScreenCapture.capture()).thenThrow(new AWTException("Screen Fail"));
        when(mockVideoCapture.capture()).thenThrow(new AWTException("Video Fail"));

        manager.start();

        verify(mockCapCom, timeout(1000).atLeastOnce()).setLatestScreenFrame(null);
        verify(mockCapCom, timeout(1000).atLeastOnce()).setLatestVideoFrame(null);

        // Wait for sleep in catch block to finish and re-instantiation to occur
        Thread.sleep(600);

        final Field screenField = BackgroundCaptureManager.class.getDeclaredField("screenCapture");
        screenField.setAccessible(true);
        final Object currentScreen = screenField.get(manager);

        assertNotSame(mockScreenCapture, currentScreen, "ScreenCapture should have been re-instantiated");
    }

    @Test
    void testReInitMethods() throws Exception {
        manager.reInitScreen();

        final Field screenField = BackgroundCaptureManager.class.getDeclaredField("screenCapture");
        screenField.setAccessible(true);
        final Object currentScreen = screenField.get(manager);
        assertNotSame(mockScreenCapture, currentScreen);

        manager.reInitVideo();
        verify(mockVideoCapture, times(1)).reInit();
    }

    private void setPrivateField(final Object target, final String fieldName, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
