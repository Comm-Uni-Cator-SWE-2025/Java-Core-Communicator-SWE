/**
 *  Contributed by Ram charan.
 */

package com.swe.core.Analytics;

public class CanvasTelemetryModel {
    private Integer circleCount;
    private Integer lineCount;
    private Integer rectangleCount;
    private Integer textCount;
    private Integer editsSoFar;

    public CanvasTelemetryModel(Integer circleCount, Integer lineCount, Integer rectangleCount, Integer textCount, Integer editsSoFar) {
        this.circleCount = defaultValue(circleCount);
        this.lineCount = defaultValue(lineCount);
        this.rectangleCount = defaultValue(rectangleCount);
        this.textCount = defaultValue(textCount);
        this.editsSoFar = defaultValue(editsSoFar);
    }

    private static Integer defaultValue(Integer value) {
        return value == null ? Integer.valueOf(0) : value;
    }

    public Integer getCircleCount() {
        return circleCount;
    }

    public Integer getLineCount() {
        return lineCount;
    }
    
    public Integer getRectangleCount() {
        return rectangleCount;
    }

    public Integer getTextCount() {
        return textCount;
    }

    public Integer getEditsSoFar() {
        return editsSoFar;
    }
}
