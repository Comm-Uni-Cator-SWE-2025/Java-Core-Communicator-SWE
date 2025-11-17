package com.swe.core.Analytics;

public class CanvasTelemetryModel {
    private Integer circleCount;
    private Integer lineCount;
    private Integer rectangleCount;
    private Integer textCount;
    private Integer editsSoFar;

    public CanvasTelemetryModel(Integer circleCount, Integer lineCount, Integer rectangleCount, Integer textCount, Integer editsSoFar) {
        this.circleCount = circleCount;
        this.lineCount = lineCount;
        this.rectangleCount = rectangleCount;
        this.textCount = textCount;
        this.editsSoFar = editsSoFar;
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
