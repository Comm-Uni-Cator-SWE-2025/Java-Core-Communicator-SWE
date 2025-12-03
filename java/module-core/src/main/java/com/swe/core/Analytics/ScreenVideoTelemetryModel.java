package com.swe.core.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model representing screen / video telemetry collected every 3 seconds.
 *
 * <p>
 * The model stores an ordered list of FPS samples collected every 3 seconds
 * together with start/end timestamps and flags indicating whether the camera
 * or screen sharing were active. Derived metrics (average, min, max and 95th
 * percentile) are calculated at construction time.
 * </p>
 *
 * <p>
 * Contributed by Jyoti.
 * </p>
 */
public class ScreenVideoTelemetryModel {
    /** Start time of the telemetry period (milliseconds since epoch). */
    private Long startTime;

    /** End time of the telemetry period (milliseconds since epoch). */
    private Long endTime;

    /** FPS values recorded every 3 seconds. */
    private ArrayList<Double> fpsEvery3Seconds;

    /** Whether camera was active during this period. */
    private boolean withCamera;

    /** Whether screen sharing was active during this period. */
    private boolean withScreen;

    // Derived metrics
    private Double avgFps;
    private Double maxFps;
    private Double minFps;
    private Double p95Fps;

    /**
     * Creates a new telemetry model.
     *
     * @param startTimeParam start time of the interval in milliseconds
     * @param endTimeParam   end time of the interval in milliseconds
     * @param fpsList        list of FPS samples recorded every 3 seconds (may be
     *                       null)
     * @param cameraActive   true if camera was active during the interval
     * @param screenActive   true if screen sharing was active during the interval
     */
    public ScreenVideoTelemetryModel(final Long startTimeParam,
            final Long endTimeParam,
            final ArrayList<Double> fpsList,
            final boolean cameraActive,
            final boolean screenActive) {
        this.startTime = startTimeParam;
        this.endTime = endTimeParam;
        this.fpsEvery3Seconds = fpsList != null ? new ArrayList<>(fpsList) : new ArrayList<>();
        this.withCamera = cameraActive;
        this.withScreen = screenActive;
        calculateMetrics();
    }

    /**
     * Returns whether the camera was active.
     *
     * @return true if camera was active
     */
    public boolean isWithCamera() {
        return withCamera;
    }

    /**
     * Returns whether screen sharing was active.
     *
     * @return true if screen sharing was active
     */
    public boolean isWithScreen() {
        return withScreen;
    }

    /**
     * Gets the start time for the telemetry interval.
     *
     * @return start time in milliseconds since epoch
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time for the telemetry interval.
     *
     * @return end time in milliseconds since epoch
     */
    public Long getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time for the telemetry interval.
     *
     * @param endTimeValue the end time in milliseconds since epoch
     */
    public void setEndTime(final long endTimeValue) {
        endTime = endTimeValue;
    }

    /**
     * Sets the start time for the telemetry interval.
     *
     * @param startTimeValue the start time in milliseconds since epoch
     */
    public void setStartTime(final long startTimeValue) {
        startTime = startTimeValue;
    }

    /**
     * Sets whether screen sharing was active.
     *
     * @param active true if screen sharing was active
     */
    public void setWithScreen(final boolean active) {
        withScreen = active;
    }

    /**
     * Sets whether camera was active.
     *
     * @param active true if camera was active
     */
    public void setWithCamera(final boolean active) {
        withCamera = active;
    }

    /**
     * Adds an FPS sample to the internal list.
     *
     * @param fps the FPS sample to add (may be null)
     */
    public void addFps(final Double fps) {
        fpsEvery3Seconds.add(fps);
    }

    /**
     * Returns a copy of the FPS samples recorded every 3 seconds.
     *
     * @return list of FPS samples
     */
    public ArrayList<Double> getFpsEvery3Seconds() {
        return new ArrayList<>(fpsEvery3Seconds);
    }

    /**
     * Returns the average FPS for the interval.
     *
     * @return average FPS
     */
    public Double getAvgFps() {
        return avgFps;
    }

    /**
     * Returns the maximum FPS observed.
     *
     * @return maximum FPS
     */
    public Double getMaxFps() {
        return maxFps;
    }

    /**
     * Returns the minimum FPS observed.
     *
     * @return minimum FPS
     */
    public Double getMinFps() {
        return minFps;
    }

    /**
     * Returns the 95th percentile FPS (worst 5%).
     *
     * @return 95th percentile FPS
     */
    public Double getP95Fps() {
        return p95Fps;
    }

    /**
     * Calculates derived metrics (average, min, max, 95th percentile).
     */
    private void calculateMetrics() {
        if (fpsEvery3Seconds == null || fpsEvery3Seconds.isEmpty()) {
            avgFps = 0.0;
            maxFps = 0.0;
            minFps = 0.0;
            p95Fps = 0.0;
            return;
        }

        final List<Double> sanitized = new ArrayList<>();
        for (final Double value : fpsEvery3Seconds) {
            if (value != null) {
                sanitized.add(value);
            }
        }

        if (sanitized.isEmpty()) {
            avgFps = 0.0;
            maxFps = 0.0;
            minFps = 0.0;
            p95Fps = 0.0;
            return;
        }

        double sum = 0.0;
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;

        for (final Double fps : sanitized) {
            sum += fps;
            if (fps > max) {
                max = fps;
            }
            if (fps < min) {
                min = fps;
            }
        }

        avgFps = sum / sanitized.size();
        maxFps = max;
        minFps = min;

        final List<Double> sorted = new ArrayList<>(sanitized);
        Collections.sort(sorted);

        // 95th percentile index → use upper end of distribution
        int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
        if (index < 0) {
            index = 0;
        }
        if (index >= sorted.size()) {
            index = sorted.size() - 1;
        }

        p95Fps = sorted.get(index);
    }
}
