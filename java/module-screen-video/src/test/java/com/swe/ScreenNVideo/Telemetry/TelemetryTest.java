/**
 * Comprehensive test suite for Telemetry class.
 * Tests singleton pattern, model management, and telemetry data collection.
 */
package com.swe.ScreenNVideo.Telemetry;

import com.swe.core.Analytics.ScreenVideoTelemetryModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Telemetry class to achieve 100% coverage.
 */
public class TelemetryTest {

    private Telemetry telemetry;

    @BeforeEach
    public void setUp() throws Exception {
        // Reset singleton instance before each test
        resetSingleton();
        telemetry = Telemetry.getTelemetry();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Reset singleton instance after each test
        resetSingleton();
    }

    /**
     * Resets the singleton instance using reflection.
     */
    private void resetSingleton() throws Exception {
        final Field instanceField = Telemetry.class.getDeclaredField("telemetry");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    /**
     * Tests that getTelemetry returns the same singleton instance.
     */
    @Test
    public void testGetTelemetry_Singleton() {
        final Telemetry instance1 = Telemetry.getTelemetry();
        final Telemetry instance2 = Telemetry.getTelemetry();
        assertSame(instance1, instance2, "getTelemetry should return the same singleton instance");
    }

    /**
     * Tests that addNewModel creates a new model and sets initial values.
     */
    @Test
    public void testAddNewModel() {
        telemetry.addNewModel();
        
        // Verify model was created by checking getAllScreenVideosTelemetry
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(1, models.size(), "Should have one model after addNewModel");
        
        final ScreenVideoTelemetryModel model = models.get(0);
        assertNotNull(model, "Model should not be null");
        assertFalse(model.isWithCamera(), "Model should have withCamera set to false initially");
        assertFalse(model.isWithScreen(), "Model should have withScreen set to false initially");
    }

    /**
     * Tests that addFps creates a new model if none exists.
     */
    @Test
    public void testAddFps_CreatesNewModelIfNoneExists() {
        final double testFps = 30.5;
        telemetry.addFps(testFps);
        
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(1, models.size(), "Should have one model after addFps");
        
        final ScreenVideoTelemetryModel model = models.get(0);
        assertNotNull(model, "Model should not be null");
        // Verify FPS was added (we can't directly check the list, but model should exist)
    }

    /**
     * Tests that addFps adds to existing model if one exists.
     */
    @Test
    public void testAddFps_AddsToExistingModel() {
        telemetry.addNewModel();
        final double testFps1 = 30.5;
        final double testFps2 = 25.0;
        
        telemetry.addFps(testFps1);
        telemetry.addFps(testFps2);
        
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(1, models.size(), "Should have one model");
    }

    /**
     * Tests that setWithCamera creates a new model if none exists.
     */
    @Test
    public void testSetWithCamera_CreatesNewModelIfNoneExists() {
        telemetry.setWithCamera(true);
        
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(1, models.size(), "Should have one model after setWithCamera");
        
        final ScreenVideoTelemetryModel model = models.get(0);
        assertTrue(model.isWithCamera(), "Model should have withCamera set to true");
    }

    /**
     * Tests that setWithCamera updates existing model.
     */
    @Test
    public void testSetWithCamera_UpdatesExistingModel() {
        telemetry.addNewModel();
        telemetry.setWithCamera(true);
        
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(1, models.size(), "Should have one model");
        assertTrue(models.get(0).isWithCamera(), "Model should have withCamera set to true");
    }

    /**
     * Tests that setWithScreen creates a new model if none exists.
     */
    @Test
    public void testSetWithScreen_CreatesNewModelIfNoneExists() {
        telemetry.setWithScreen(true);
        
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(1, models.size(), "Should have one model after setWithScreen");
        
        final ScreenVideoTelemetryModel model = models.get(0);
        assertTrue(model.isWithScreen(), "Model should have withScreen set to true");
    }

    /**
     * Tests that setWithScreen updates existing model.
     */
    @Test
    public void testSetWithScreen_UpdatesExistingModel() {
        telemetry.addNewModel();
        telemetry.setWithScreen(true);
        
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(1, models.size(), "Should have one model");
        assertTrue(models.get(0).isWithScreen(), "Model should have withScreen set to true");
    }

    /**
     * Tests that closeModel does nothing if current model is null.
     */
    @Test
    public void testCloseModel_NoCurrentModel() {
        // No model exists, so closeModel should do nothing
        telemetry.closeModel();
        
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(0, models.size(), "Should have no models if none were created");
    }


    /**
     * Tests that getAllScreenVideosTelemetry returns empty list when no models exist.
     */
    @Test
    public void testGetAllScreenVideosTelemetry_Empty() {
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertNotNull(models, "Should return non-null list");
        assertEquals(0, models.size(), "Should return empty list when no models exist");
    }

    /**
     * Tests that getAllScreenVideosTelemetry returns both queued and current models.
     */
    @Test
    public void testGetAllScreenVideosTelemetry_WithQueuedAndCurrent() {
        // Add and close first model (goes to queue)
        telemetry.addNewModel();
        telemetry.closeModel();
        
        // Add second model (current)
        telemetry.addNewModel();
        
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(2, models.size(), "Should return both queued and current models");
    }


    /**
     * Tests multiple operations in sequence.
     */
    @Test
    public void testMultipleOperations() {
        telemetry.addNewModel();
        telemetry.setWithCamera(true);
        telemetry.setWithScreen(true);
        telemetry.addFps(30.0);
        telemetry.addFps(25.0);
        telemetry.closeModel();
        
        telemetry.addNewModel();
        telemetry.addFps(20.0);
        
        final List<ScreenVideoTelemetryModel> models = telemetry.getAllScreenVideosTelemetry();
        assertEquals(2, models.size(), "Should have two models");
    }
}

