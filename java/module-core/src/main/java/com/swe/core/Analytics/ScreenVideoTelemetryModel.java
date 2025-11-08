package com.swe.core.Analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScreenVideoTelemetryModel {
    private Long startTime;
    private Long endTime;
    private ArrayList<Double> fpsEvery3Seconds;

    // Metrics
    private Double avgFps;
    private Double maxFps;
    private Double minFps;
    private Double p95Fps; // 95th percentile (worst 5%)

    // --- Constructor ---
    public ScreenVideoTelemetryModel() {
        this.fpsEvery3Seconds = new ArrayList<>();
    }

    // --- Getters ---
    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
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

    // --- Setters ---
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setFpsEvery3Seconds(ArrayList<Double> fpsEvery3Seconds) {
        this.fpsEvery3Seconds = fpsEvery3Seconds;
        calculateMetrics(); // Recalculate when new data is set
    }

    public void setAvgFps(Double avgFps) {
        this.avgFps = avgFps;
    }

    public void setMaxFps(Double maxFps) {
        this.maxFps = maxFps;
    }

    public void setMinFps(Double minFps) {
        this.minFps = minFps;
    }

    public void setP95Fps(Double p95Fps) {
        this.p95Fps = p95Fps;
    }

    // --- Helper method to calculate metrics ---
    public void calculateMetrics() {
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

    // --- Add a single FPS sample ---
    public void addFpsSample(Double fps) {
        if (fps == null)
            return;
        fpsEvery3Seconds.add(fps);
        calculateMetrics();
    }
}
