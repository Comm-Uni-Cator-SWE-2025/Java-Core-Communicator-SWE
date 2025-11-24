/**
 *  Contributed by Jyoti.
 */

package com.swe.core.Analytics;

import java.util.List;

public interface ScreenVideoTelemetry {
    /**
     * Get all telemetry data for each time the screen was captured.
     * The implementer of this class will have to keep track of all the telemetry models in some internal data structure.
     * @return List of all telemetry data.
     */
    public List<ScreenVideoTelemetryModel> getAllScreenVideosTelemetry();
}
