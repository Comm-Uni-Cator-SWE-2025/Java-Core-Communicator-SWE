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

    /**
     * Queue of screen video telemetry models.
     */
    private final Queue<ScreenVideoTelemetryModel> screenVideoTelemetryModels;

    /**
     * Current screen video telemetry model.
     */
    private ScreenVideoTelemetryModel currentScreenVideoTelemetryModel;

    /**
     * Telemetry instance.
     */
    private static Telemetry telemetry;

    /**
     * Constructor for the Telemetry class.
     */
    private Telemetry() {
        this.screenVideoTelemetryModels = new LinkedList<>();
        this.currentScreenVideoTelemetryModel = null;
    }

    /**
     * Get the Telemetry instance.
     * @return The Telemetry instance.
     */
    public static Telemetry getTelemetry() {
        if (telemetry == null) {
            telemetry = new Telemetry();
        }
        return telemetry;
    }

    /**
     * Close the current screen video telemetry model.
     */
    public void closeModel() {
        if (currentScreenVideoTelemetryModel != null) {
            currentScreenVideoTelemetryModel.setEndTime(System.currentTimeMillis());
            screenVideoTelemetryModels.add(currentScreenVideoTelemetryModel);
        }
    }

    /**
     * Add a new screen video telemetry model.
     */
    public void addNewModel() {
        currentScreenVideoTelemetryModel = new ScreenVideoTelemetryModel(System.currentTimeMillis(),
                System.currentTimeMillis(), new ArrayList<>(), false, false);
        currentScreenVideoTelemetryModel.setWithCamera(false);
        currentScreenVideoTelemetryModel.setWithScreen(false);
        currentScreenVideoTelemetryModel.setStartTime(System.currentTimeMillis());
    }

    /**
     * Add a new FPS to the current screen video telemetry model.
     * @param fps The FPS to add.
     */
    public void addFps(final Double fps) {
        if (currentScreenVideoTelemetryModel == null) {
            addNewModel();
        }
        currentScreenVideoTelemetryModel.addFps(fps);
    }

    /**
     * Set the with camera flag for the current screen video telemetry model.
     * @param withCamera The with camera flag.
     */
    public void setWithCamera(final boolean withCamera) {
        if (currentScreenVideoTelemetryModel == null) {
            addNewModel();
        }
        currentScreenVideoTelemetryModel.setWithCamera(withCamera);
    }

    /**
     * Set the with screen flag for the current screen video telemetry model.
     * @param withScreen The with screen flag.
     */
    public void setWithScreen(final boolean withScreen) {
        if (currentScreenVideoTelemetryModel == null) {
            addNewModel();
        }
        currentScreenVideoTelemetryModel.setWithScreen(withScreen);
    }

    /**
     * Get all screen video telemetry models.
     * @return List of all screen video telemetry models.
     */
    @Override
    public List<ScreenVideoTelemetryModel> getAllScreenVideosTelemetry() {
        final ArrayList<ScreenVideoTelemetryModel> allScreenVideosTelemetry = new ArrayList<>();
        while (!screenVideoTelemetryModels.isEmpty()) {
            allScreenVideosTelemetry.add(screenVideoTelemetryModels.poll());
        }
        if (currentScreenVideoTelemetryModel != null) {
            allScreenVideosTelemetry.add(currentScreenVideoTelemetryModel);
        }
        return allScreenVideosTelemetry;
    }
}
