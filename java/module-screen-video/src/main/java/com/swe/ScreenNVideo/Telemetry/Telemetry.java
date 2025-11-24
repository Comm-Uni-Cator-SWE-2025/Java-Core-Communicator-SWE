package com.swe.ScreenNVideo.Telemetry;

import com.swe.core.Analytics.ScreenVideoTelemetry;
import com.swe.core.Analytics.ScreenVideoTelemetryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Telemetry class for the ScreenNVideo module.
 */
public class Telemetry implements ScreenVideoTelemetry {

    private Queue<ScreenVideoTelemetryModel> screenVideoTelemetryModels;

    private ScreenVideoTelemetryModel currentScreenVideoTelemetryModel;

    private static Telemetry telemetry;

    private Telemetry() {
        this.screenVideoTelemetryModels = new LinkedList<>();
        this.currentScreenVideoTelemetryModel = null;
    }

    public static Telemetry getTelemetry() {
        if (telemetry == null) {
            telemetry = new Telemetry();
        }
        return telemetry;
    }

    public void closeModel() {
        if (currentScreenVideoTelemetryModel != null) {
            currentScreenVideoTelemetryModel.setEndTime(System.currentTimeMillis());
            screenVideoTelemetryModels.add(currentScreenVideoTelemetryModel);
        }
    }

    public void addNewModel() {
        currentScreenVideoTelemetryModel = new ScreenVideoTelemetryModel(System.currentTimeMillis(), System.currentTimeMillis(), new ArrayList<>(), false, false);
        currentScreenVideoTelemetryModel.setWithCamera(false);
        currentScreenVideoTelemetryModel.setWithScreen(false);
        currentScreenVideoTelemetryModel.setStartTime(System.currentTimeMillis());
    }

    public void addFps(Double fps) {
        if (currentScreenVideoTelemetryModel == null) {
            addNewModel();
        }
        currentScreenVideoTelemetryModel.addFps(fps);
    }

    public void setWithCamera(boolean withCamera) {
        if (currentScreenVideoTelemetryModel == null) {
            addNewModel();
        }
        currentScreenVideoTelemetryModel.setWithCamera(withCamera);
    }

    public void setWithScreen(boolean withScreen) {
        if (currentScreenVideoTelemetryModel == null) {
            addNewModel();
        }
        currentScreenVideoTelemetryModel.setWithScreen(withScreen);
    }

    @Override
    public List<ScreenVideoTelemetryModel> getAllScreenVideosTelemetry() {
        ArrayList<ScreenVideoTelemetryModel> allScreenVideosTelemetry = new ArrayList<>();
        while (!screenVideoTelemetryModels.isEmpty()) {
            allScreenVideosTelemetry.add(screenVideoTelemetryModels.poll());
        }
        if (currentScreenVideoTelemetryModel != null) {
            allScreenVideosTelemetry.add(currentScreenVideoTelemetryModel);
        }
        return allScreenVideosTelemetry;
    }
}
