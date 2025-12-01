package cosmosoperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datastructures.CloudResponse;
import datastructures.Entity;
import interfaces.IdbConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockDbConnectorTest {

    private final IdbConnector mockDbConnector = new MockDbConnector();
    private Entity testEntity;

    @BeforeEach
    void createTestEntity() {
        testEntity  = new Entity("TestModule", "TestTable", "TestId", null, -1, null, null);
    }
    @Test
    void initTest() {
        MockDbConnector spyConnector = org.mockito.Mockito.spy(new MockDbConnector());
        spyConnector.init();
        org.mockito.Mockito.verify(spyConnector, org.mockito.Mockito.times(1)).init();
    }

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