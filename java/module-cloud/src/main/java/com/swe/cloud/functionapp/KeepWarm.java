/******************************************************************************
 * Filename    = KeepWarm.java
 * Author      = Kallepally Sai Kiran
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Timer-triggered Azure Function to keep the Function App warm
 *****************************************************************************/

package functionapp;

import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.microsoft.azure.functions.ExecutionContext;
import java.time.Instant;

/**
 * Timer-triggered function that executes periodically to prevent cold start.
 *
 * <p>Keeps the Function App active, so HTTP-triggered functions respond faster.
 */
public class KeepWarm {

    /**
     * Executes every 5 minutes to keep the Function App alive.
     *
     * @param timerInfo Timer metadata from Azure Functions runtime (cron info)
     * @param context Execution context for logging
     * @return A String message confirming the function execution timestamp
     */
    @FunctionName("KeepWarm")
    public String runKeepWarm(
            final @TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo,
            final ExecutionContext context) {

        final String message = "[KeepWarm] Timer executed at: " + Instant.now();
        context.getLogger().info(message);
        return message;
    }
}
