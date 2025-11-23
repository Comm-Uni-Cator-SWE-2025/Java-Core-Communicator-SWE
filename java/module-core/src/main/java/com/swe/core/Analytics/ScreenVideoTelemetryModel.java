/**
 *  Contributed by Jyoti.
 */

package com.swe.core.Analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScreenVideoTelemetryModel {
    private Long startTime;
    private Long endTime;
    private ArrayList<Double> fpsEvery3Seconds;

    private boolean withCamera;
    private boolean withScreen;

    // Metrics
    private Double avgFps;
    private Double maxFps;
    private Double minFps;
    private Double p95Fps; // 95th percentile (worst 5%)

    // --- Constructor ---
    public ScreenVideoTelemetryModel(Long startTime, Long endTime, ArrayList<Double> fpsEvery3Seconds,
            boolean withCamera, boolean withScreen) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.fpsEvery3Seconds = fpsEvery3Seconds != null ? new ArrayList<>(fpsEvery3Seconds) : new ArrayList<>();
        this.withCamera = withCamera;
        this.withScreen = withScreen;
        calculateMetrics(); // Calculate metrics from fpsEvery3Seconds
    }

    // --- Getters ---
    public boolean isWithCamera() {
        return withCamera;
    }

    public boolean isWithScreen() {
        return withScreen;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(final long val) {
        endTime = val;
    }

    public void setStartTime(final long val) {
        startTime = val;
    }

    public void setWithScreen(final boolean val) {
        withScreen = val;
    }

    public void setWithCamera(final boolean val) {
        withCamera = val;
    }

    public void addFps(Double fps) {
        fpsEvery3Seconds.add(fps);
    }

    public ArrayList<Double> getFpsEvery3Seconds() {
        return fpsEvery3Seconds;
    }

    public Double getAvgFps() {
        return avgFps;
    }

    public Double getMaxFps() {
        return maxFps;
    }

    public Double getMinFps() {
        return minFps;
    }

    public Double getP95Fps() {
        return p95Fps;
    }

    // --- Helper method to calculate metrics ---
    private void calculateMetrics() {
        if (fpsEvery3Seconds == null || fpsEvery3Seconds.isEmpty()) {
            avgFps = maxFps = minFps = p95Fps = 0.0;
            return;
        }

        // Compute average
        double sum = 0.0;
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;

        for (Double fps : fpsEvery3Seconds) {
            if (fps == null)
                continue;
            sum += fps;
            if (fps > max)
                max = fps;
            if (fps < min)
                min = fps;
        }

        avgFps = sum / fpsEvery3Seconds.size();
        maxFps = max;
        minFps = min;

        // Compute 95th percentile (worst 5%)
        List<Double> sorted = new ArrayList<>(fpsEvery3Seconds);
        Collections.sort(sorted);

        // 95th percentile index → floor(0.05 * n)
        int index = (int) Math.floor(0.05 * sorted.size());
        if (index < 0)
            index = 0;
        if (index >= sorted.size())
            index = sorted.size() - 1;

        p95Fps = sorted.get(index);
    }

}
