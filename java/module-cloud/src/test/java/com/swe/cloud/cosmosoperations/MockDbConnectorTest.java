/******************************************************************************
 * Filename    = MockDbConnectorTest.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Unit tests for MockDbConnector
 *****************************************************************************/

package cosmosoperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datastructures.CloudResponse;
import datastructures.Entity;
import interfaces.IdbConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MockDbConnector}. Ensures that all CRUD operations
 * return the correct {@link CloudResponse} data-structure expected from the mock implementation.
 */
class MockDbConnectorTest {

    private final IdbConnector mockDbConnector = new MockDbConnector();
    private Entity testEntity;

    /**
     * Creates a reusable test entity before each test.
     */
    @BeforeEach
    void createTestEntity() {
        testEntity = new Entity("TestModule", "TestTable", "TestId", null, -1, null, null);
    }

    /**
     * Verifies that init() is invoked exactly once using a Mockito spy.
     */
    @Test
    void initTest() {
        MockDbConnector spyConnector = org.mockito.Mockito.spy(new MockDbConnector());
        spyConnector.init();
        org.mockito.Mockito.verify(spyConnector, org.mockito.Mockito.times(1)).init();
    }

    /**
     * Tests getData() to confirm the mock returns the correct CloudResponse payload.
     */
    @Test
    void getDataTest() {
        CloudResponse testCloudResponse = mockDbConnector.getData(testEntity);
        assertInstanceOf(CloudResponse.class, testCloudResponse);
        assertEquals(200, testCloudResponse.status_code());
        assertEquals("success", testCloudResponse.message());

        ObjectNode testObject = new ObjectMapper().createObjectNode();
        testObject.put("operation", "GET");
        testObject.put("module", "TestModule");
        testObject.put("table", "TestTable");
        testObject.put("id", "TestId");

        assertEquals(testObject, testCloudResponse.data());
    }

    /**
     * Tests postData() to confirm the mock returns the correct CloudResponse payload.
     */
    @Test
    void postDataTest() {

        CloudResponse testCloudResponse = mockDbConnector.postData(testEntity);
        assertInstanceOf(CloudResponse.class, testCloudResponse);
        assertEquals(200, testCloudResponse.status_code());
        assertEquals("success", testCloudResponse.message());

        ObjectNode testObject = new ObjectMapper().createObjectNode();
        testObject.put("operation", "POST");
        testObject.put("module", "TestModule");
        testObject.put("table", "TestTable");
        testObject.put("id", "TestId");

        assertEquals(testObject, testCloudResponse.data());
    }

    /**
     * Tests createData() to confirm the mock returns the correct CloudResponse payload.
     */
    @Test
    void createDataTest() {

        CloudResponse testCloudResponse = mockDbConnector.createData(testEntity);
        assertInstanceOf(CloudResponse.class, testCloudResponse);
        assertEquals(200, testCloudResponse.status_code());
        assertEquals("success", testCloudResponse.message());

        ObjectNode testObject = new ObjectMapper().createObjectNode();
        testObject.put("operation", "CREATE");
        testObject.put("module", "TestModule");
        testObject.put("table", "TestTable");
        testObject.put("id", "TestId");

        assertEquals(testObject, testCloudResponse.data());
    }

    /**
     * Tests deleteData() to confirm the mock returns the correct CloudResponse payload.
     */
    @Test
    void deleteDataTest() {

        CloudResponse testCloudResponse = mockDbConnector.deleteData(testEntity);
        assertInstanceOf(CloudResponse.class, testCloudResponse);
        assertEquals(200, testCloudResponse.status_code());
        assertEquals("success", testCloudResponse.message());

        ObjectNode testObject = new ObjectMapper().createObjectNode();
        testObject.put("operation", "DELETE");
        testObject.put("module", "TestModule");
        testObject.put("table", "TestTable");
        testObject.put("id", "TestId");

        assertEquals(testObject, testCloudResponse.data());
    }

    /**
     * Tests updateData() to confirm the mock returns the correct CloudResponse payload.
     */
    @Test
    void updateDataTest() {

        CloudResponse testCloudResponse = mockDbConnector.updateData(testEntity);
        assertInstanceOf(CloudResponse.class, testCloudResponse);
        assertEquals(200, testCloudResponse.status_code());
        assertEquals("success", testCloudResponse.message());

        ObjectNode testObject = new ObjectMapper().createObjectNode();
        testObject.put("operation", "UPDATE");
        testObject.put("module", "TestModule");
        testObject.put("table", "TestTable");
        testObject.put("id", "TestId");

        assertEquals(testObject, testCloudResponse.data());
    }
}