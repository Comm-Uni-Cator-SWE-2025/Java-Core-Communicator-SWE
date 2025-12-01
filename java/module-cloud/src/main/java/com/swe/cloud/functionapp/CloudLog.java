package functionapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.telemetry.SeverityLevel;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Azure Function to handle Cloud Logging and Telemetry.
 */
public class CloudLog {

    /** Telemetry client for Application Insights. */
    private static TelemetryClient telemetryClient;

    /** Object mapper for JSON processing. */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Handles the logging request and sends telemetry to Azure Application Insights.
     *
     * @param request The HTTP request containing the log data.
     * @param context The execution context.
     * @return The HTTP response.
     */
    @FunctionName("CloudLog")
    public HttpResponseMessage cloudLog(
            @HttpTrigger(
                    name = "req",
                    methods = HttpMethod.POST,
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "telemetry/log"
            ) final HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        try {
            if (telemetryClient == null) {
                final String connStr = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");

                if (connStr == null || connStr.isEmpty()) {
                    context.getLogger().severe("CRITICAL ERROR: Connection String is NULL!");
                } else {
                    context.getLogger().info("Initializing TelemetryClient...");

                    // Create a specific configuration for this client
                    final TelemetryConfiguration configuration = TelemetryConfiguration.createDefault();
                    try {
                        configuration.setConnectionString(connStr);
                    } catch (Exception ex) {
                        context.getLogger().warning("Could not set connection string directly: " + ex.getMessage());
                    }
                    telemetryClient = new TelemetryClient(configuration);
                }
            }
            final String jsonBody = request.getBody().orElse(null);
            if (jsonBody == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Empty body").build();
            }

            final JsonNode rootNode = mapper.readTree(jsonBody);
            final String moduleName = rootNode.path("moduleName").asText("Unknown");
            final String severity = rootNode.path("severity").asText("INFO");
            final String message = rootNode.path("message").asText("");

            final Map<String, String> properties = new HashMap<>();
            properties.put("ReportingModule", moduleName);

            if ("ERROR".equalsIgnoreCase(severity)) {
                telemetryClient.trackTrace(message, SeverityLevel.Error, properties);
            } else if ("WARNING".equalsIgnoreCase(severity)) {
                telemetryClient.trackTrace(message, SeverityLevel.Warning, properties);
            } else {
                telemetryClient.trackTrace(message, SeverityLevel.Information, properties);
            }
            telemetryClient.flush();
            return request.createResponseBuilder(HttpStatus.OK).build();
        } catch (final Exception e) {
            context.getLogger().severe("Telemetry failed: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}