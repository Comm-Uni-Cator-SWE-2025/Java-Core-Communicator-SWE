package functionapp;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.SeverityLevel;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import java.util.Map;

class CloudLogTest extends CloudTestBase{

    private TelemetryClient mockTelemetryClient;
    private CloudLog cloudLog;

    @BeforeEach
    void setUp() throws Exception {
        cloudLog = new CloudLog();
        mockTelemetryClient = mock(TelemetryClient.class);
        setStaticTelemetryClient(mockTelemetryClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        setStaticTelemetryClient(null);
    }

    private void setStaticTelemetryClient(TelemetryClient client) throws Exception {
        Field field = CloudLog.class.getDeclaredField("telemetryClient");
        field.setAccessible(true);
        field.set(null, client);
    }

    @Test
    void testCloudLog_Success_Info() {
        String jsonBody = "{\"moduleName\": \"UserAuth\", \"severity\": \"INFO\", \"message\": \"User logged in\"}";
        HttpRequestMessage<Optional<String>> request = mockRequest(jsonBody);
        ExecutionContext context = mockContext();
        var response = cloudLog.cloudLog(request, context);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());

        // Verify TelemetryClient interaction
        verify(mockTelemetryClient).trackTrace(eq("User logged in"), eq(SeverityLevel.Information), any(Map.class));
        verify(mockTelemetryClient).flush();
    }

    @Test
    void testCloudLog_Success_Warning() {
        String jsonBody = "{\"moduleName\": \"System\", \"severity\": \"WARNING\", \"message\": \"Disk space low\"}";
        HttpRequestMessage<Optional<String>> request = mockRequest(jsonBody);
        ExecutionContext context = mockContext();
        var response = cloudLog.cloudLog(request, context);

        assertEquals(HttpStatus.OK, response.getStatus());

        verify(mockTelemetryClient).trackTrace(eq("Disk space low"), eq(SeverityLevel.Warning), any(Map.class));
        verify(mockTelemetryClient).flush();
    }

    @Test
    void testCloudLog_Success_Error() {
        String jsonBody = "{\"moduleName\": \"Database\", \"severity\": \"ERROR\", \"message\": \"Connection failed\"}";
        HttpRequestMessage<Optional<String>> request = mockRequest(jsonBody);
        ExecutionContext context = mockContext();
        var response = cloudLog.cloudLog(request, context);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("ReportingModule", "Database");

        verify(mockTelemetryClient).trackTrace(
                eq("Connection failed"),
                eq(SeverityLevel.Error),
                eq(expectedMap) // Checks if the actual map equals this expected map
        );
    }

    @Test
    void testCloudLog_BadRequest_EmptyBody() {
        HttpRequestMessage<Optional<String>> request = mockRequest(null); // Empty Optional
        ExecutionContext context = mockContext();
        var response = cloudLog.cloudLog(request, context);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Empty body", response.getBody());
    }

    @Test
    void testCloudLog_InternalServerError_InvalidJson() {
        String invalidJson = "{ broken_json : ";
        HttpRequestMessage<Optional<String>> request = mockRequest(invalidJson);
        ExecutionContext context = mockContext();
        var response = cloudLog.cloudLog(request, context);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());

        // Verify that no telemetry was tracked for this payload
        verify(mockTelemetryClient, never()).trackTrace(anyString(), any(), any());
    }
}