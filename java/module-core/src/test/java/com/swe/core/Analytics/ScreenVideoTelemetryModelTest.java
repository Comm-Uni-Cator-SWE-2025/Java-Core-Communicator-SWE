/**
 *  Contributed by Jyoti.
 */
package com.swe.core.Analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

public class ScreenVideoTelemetryModelTest {

    private ArrayList<Double> fpsList;

    @Before
    public void setUp() {
        fpsList = new ArrayList<>();
        fpsList.add(30.0);
        fpsList.add(29.5);
        fpsList.add(30.2);
        fpsList.add(28.0);
        fpsList.add(31.0);
    }

    @Test
    public void constructorSetsAllFields() {
        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            fpsList,
            true,
            true
        );

        assertEquals(Long.valueOf(1000L), model.getStartTime());
        assertEquals(Long.valueOf(2000L), model.getEndTime());
        assertEquals(5, model.getFpsEvery3Seconds().size());
        assertTrue(model.isWithCamera());
        assertTrue(model.isWithScreen());
    }

    @Test
    public void constructorCalculatesMetrics() {
        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            fpsList,
            true,
            true
        );

        assertNotNull(model.getAvgFps());
        assertNotNull(model.getMaxFps());
        assertNotNull(model.getMinFps());
        assertNotNull(model.getP95Fps());
    }

    @Test
    public void constructorWithNullFpsList() {
        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            null,
            false,
            false
        );

        assertNotNull(model.getFpsEvery3Seconds());
        assertEquals(0, model.getFpsEvery3Seconds().size());
        assertEquals(0.0, model.getAvgFps(), 0.001);
        assertEquals(0.0, model.getMaxFps(), 0.001);
        assertEquals(0.0, model.getMinFps(), 0.001);
        assertEquals(0.0, model.getP95Fps(), 0.001);
    }

    @Test
    public void constructorWithEmptyFpsList() {
        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            new ArrayList<>(),
            true,
            false
        );

        assertEquals(0, model.getFpsEvery3Seconds().size());
        assertEquals(0.0, model.getAvgFps(), 0.001);
    }

    @Test
    public void metricsCalculation() {
        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            fpsList,
            true,
            true
        );

        // Average should be sum / count
        final double expectedAvg = (30.0 + 29.5 + 30.2 + 28.0 + 31.0) / 5.0;
        assertEquals(expectedAvg, model.getAvgFps(), 0.001);

        // Max should be 31.0
        assertEquals(31.0, model.getMaxFps(), 0.001);

        // Min should be 28.0
        assertEquals(28.0, model.getMinFps(), 0.001);
    }

    @Test
    public void p95Calculation() {
        final ArrayList<Double> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeList.add((double) i);
        }
        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            largeList,
            true,
            true
        );

        assertNotNull(model.getP95Fps());
        // 95th percentile (worst 5%) should be at index floor(0.05 * 100) = 5
        assertEquals(5.0, model.getP95Fps(), 0.001);
    }

    @Test
    public void settersUpdateFields() {
        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            fpsList,
            true,
            true
        );

        model.setStartTime(3000L);
        model.setEndTime(4000L);
        model.setWithCamera(false);
        model.setWithScreen(false);

        assertEquals(Long.valueOf(3000L), model.getStartTime());
        assertEquals(Long.valueOf(4000L), model.getEndTime());
        assertFalse(model.isWithCamera());
        assertFalse(model.isWithScreen());
    }

    @Test
    public void addFpsAddsToList() {
        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            fpsList,
            true,
            true
        );

        final int initialSize = model.getFpsEvery3Seconds().size();
        model.addFps(32.5);
        assertEquals(initialSize + 1, model.getFpsEvery3Seconds().size());
    }

    @Test
    public void constructorCreatesCopyOfFpsList() {
        final ArrayList<Double> originalList = new ArrayList<>();
        originalList.add(30.0);
        originalList.add(29.0);

        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            originalList,
            true,
            true
        );

        originalList.add(28.0);
        // Model's list should not be affected
        assertEquals(2, model.getFpsEvery3Seconds().size());
    }

    @Test
    public void metricsWithNullFpsValues() {
        final ArrayList<Double> listWithNulls = new ArrayList<>();
        listWithNulls.add(30.0);
        listWithNulls.add(null);
        listWithNulls.add(29.0);
        listWithNulls.add(null);

        final ScreenVideoTelemetryModel model = new ScreenVideoTelemetryModel(
            1000L,
            2000L,
            listWithNulls,
            true,
            true
        );

        // Should calculate metrics ignoring nulls
        assertNotNull(model.getAvgFps());
        assertNotNull(model.getMaxFps());
        assertNotNull(model.getMinFps());
    }
}

