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

class CloudGetTest extends CloudTestBase {
    @BeforeEach
    void resetFactory() {
        // This clears the factory's singleton,
        // forcing it to create a new MockDbConnector
        DbConnectorFactory.resetInstance();
    }

    @Test
    void runCloudGetTest() throws Exception {
        HttpRequestMessage<Optional<String>> request = mockRequest("{\"module\":\"testModule\",\"table\":\"testTable\",\"id\":\"testId\"}");
        ExecutionContext context = mockContext();

        IdbConnector mockConnector = mock(IdbConnector.class);
        CloudResponse mockCloudResponse = new CloudResponse(200, "success", null);

        try (MockedStatic<DbConnectorFactory> factoryMock = Mockito.mockStatic(DbConnectorFactory.class)) {
            factoryMock.when(() -> DbConnectorFactory.getDbConnector(any())).thenReturn(mockConnector);
            when(mockConnector.getData(any(Entity.class))).thenReturn(mockCloudResponse);

            CloudGet cloudGet = new CloudGet();
            var response = cloudGet.runCloudGet(request, context);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatus());
        }
    }

    @Test
    void runCloudGetTestException() throws Exception {
        HttpRequestMessage<Optional<String>> request = mockRequest("invalid json");
        ExecutionContext context = mockContext();

        CloudGet cloudGet = new CloudGet();
        var response = cloudGet.runCloudGet(request, context);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }
}
