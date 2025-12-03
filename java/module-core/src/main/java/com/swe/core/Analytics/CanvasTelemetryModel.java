/**
 * Model representing canvas telemetry data.
 *
 * Contains counts of various canvas elements and edit operations.
 */
package com.swe.core.Analytics;

public class CanvasTelemetryModel {
    /** Number of circles on the canvas. */
    private Integer circleCount;

    /** Number of lines on the canvas. */
    private Integer lineCount;

    /** Number of rectangles on the canvas. */
    private Integer rectangleCount;

    /** Number of text elements on the canvas. */
    private Integer textCount;

    /** Total number of edits performed so far. */
    private Integer editsSoFar;

    /**
     * Constructs a CanvasTelemetryModel with the specified counts.
     *
     * @param circleCount    number of circles
     * @param lineCount      number of lines
     * @param rectangleCount number of rectangles
     * @param textCount      number of text elements
     * @param editsSoFar     total number of edits performed
     */
    public CanvasTelemetryModel(final Integer circleCount,
            final Integer lineCount,
            final Integer rectangleCount,
            final Integer textCount,
            final Integer editsSoFar) {
        this.circleCount = defaultValue(circleCount);
        this.lineCount = defaultValue(lineCount);
        this.rectangleCount = defaultValue(rectangleCount);
        this.textCount = defaultValue(textCount);
        this.editsSoFar = defaultValue(editsSoFar);
    }

    /**
     * Returns a default value of 0 for null integers.
     *
     * @param value the integer to check
     * @return 0 if value is null, otherwise the original value
     */
    private static Integer defaultValue(final Integer value) {
        return value == null ? Integer.valueOf(0) : value;
    }

    /**
     * Returns the circle count.
     *
     * @return number of circles
     */
    public Integer getCircleCount() {
        return circleCount;
    }

    /**
     * Returns the line count.
     *
     * @return number of lines
     */
    public Integer getLineCount() {
        return lineCount;
    }

    /**
     * Returns the rectangle count.
     *
     * @return number of rectangles
     */
    public Integer getRectangleCount() {
        return rectangleCount;
    }

    /**
     * Returns the text count.
     *
     * @return number of text elements
     */
    public Integer getTextCount() {
        return textCount;
    }

    /**
     * Returns the total edits count.
     *
     * @return number of edits so far
     */
    public Integer getEditsSoFar() {
        return editsSoFar;
    }
}
