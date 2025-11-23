/**
 *  Contributed by Ram Charan.
 */
package com.swe.core.Analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CanvasTelemetryModelTest {

    @Test
    public void constructorSetsAllFields() {
        final CanvasTelemetryModel model = new CanvasTelemetryModel(1, 2, 3, 4, 5);

        assertEquals(Integer.valueOf(1), model.getCircleCount());
        assertEquals(Integer.valueOf(2), model.getLineCount());
        assertEquals(Integer.valueOf(3), model.getRectangleCount());
        assertEquals(Integer.valueOf(4), model.getTextCount());
        assertEquals(Integer.valueOf(5), model.getEditsSoFar());
    }

    @Test
    public void constructorWithZeroValues() {
        final CanvasTelemetryModel model = new CanvasTelemetryModel(0, 0, 0, 0, 0);

        assertEquals(Integer.valueOf(0), model.getCircleCount());
        assertEquals(Integer.valueOf(0), model.getLineCount());
        assertEquals(Integer.valueOf(0), model.getRectangleCount());
        assertEquals(Integer.valueOf(0), model.getTextCount());
        assertEquals(Integer.valueOf(0), model.getEditsSoFar());
    }

    @Test
    public void constructorWithNullValues() {
        final CanvasTelemetryModel model = new CanvasTelemetryModel(null, null, null, null, null);

        assertNotNull(model);
        assertNotNull(model.getCircleCount());
        assertNotNull(model.getLineCount());
        assertNotNull(model.getRectangleCount());
        assertNotNull(model.getTextCount());
        assertNotNull(model.getEditsSoFar());
    }

    @Test
    public void constructorWithLargeValues() {
        final CanvasTelemetryModel model = new CanvasTelemetryModel(
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE
        );

        assertEquals(Integer.valueOf(Integer.MAX_VALUE), model.getCircleCount());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), model.getLineCount());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), model.getRectangleCount());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), model.getTextCount());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), model.getEditsSoFar());
    }
}

