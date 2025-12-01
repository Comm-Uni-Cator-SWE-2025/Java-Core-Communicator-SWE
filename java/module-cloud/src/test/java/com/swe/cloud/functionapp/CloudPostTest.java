/******************************************************************************
 * Filename    = CloudPostTest.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Unit tests for CloudPost Function Endpoint.
 *****************************************************************************/

package functionapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpStatus;
import datastructures.CloudResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import cosmosoperations.DbConnectorFactory;
import datastructures.Entity;
import interfaces.IdbConnector;

import java.util.Optional;

/**
 * Tests for the CloudGet Azure Function endpoint.
 */
class CloudPostTest extends CloudTestBase {
    /** Reset factory before each test. */
    @BeforeEach
    void resetFactory() {
        // This clears the factory's singleton,
        // forcing it to create a new MockDbConnector
        DbConnectorFactory.resetInstance();
    }

    /** Verifies successful CloudPost execution. */
    @Test
    void runCloudPostTest() throws Exception {
        HttpRequestMessage<Optional<String>> request = mockRequest("{\"module\":\"testModule\",\"table\":\"testTable\",\"id\":\"testId\"}");
        ExecutionContext context = mockContext();

        IdbConnector mockConnector = mock(IdbConnector.class);
        CloudResponse mockCloudResponse = new CloudResponse(200, "success", null);

        try (MockedStatic<DbConnectorFactory> factoryMock = Mockito.mockStatic(DbConnectorFactory.class)) {
            // Factory returns mocked connector
            factoryMock.when(() -> DbConnectorFactory.getDbConnector(any())).thenReturn(mockConnector);
            when(mockConnector.postData(any(Entity.class))).thenReturn(mockCloudResponse);

            // Mock postData() behavior
            CloudPost cloudPost = new CloudPost();
            var response = cloudPost.runCloudPost(request, context);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatus());
        }
    }

    /** Verifies BAD_REQUEST is returned for invalid JSON. */
    @Test
    void runCloudPostTestException() throws Exception {
        HttpRequestMessage<Optional<String>> request = mockRequest("invalid json");
        ExecutionContext context = mockContext();

        CloudPost cloudPost = new CloudPost();
        var response = cloudPost.runCloudPost(request, context);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }
}
