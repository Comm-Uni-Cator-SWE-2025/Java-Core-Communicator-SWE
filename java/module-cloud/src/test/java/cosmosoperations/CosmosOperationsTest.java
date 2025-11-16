package cosmosoperations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datastructures.CloudResponse;
import datastructures.Entity;
import datastructures.TimeRange;
import interfaces.IdbConnector;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CosmosOperationsTest {

    private static IdbConnector cosmosDbConnector;
    private static Entity testEntity;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Entity deleteContainerEntity;

    @BeforeAll
    static void setupClass() {
        cosmosDbConnector = new CosmosOperations();
        cosmosDbConnector.init();

        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.put("data1", 0);
        dataNode.put("data2", 1);

        testEntity = new Entity(
                "TestModule",
                "TestTable",
                "TestId",
                null,
                -1,
                null,
                dataNode
        );
        deleteContainerEntity = new Entity(
                "TestModule",
                "TestTable",
                null, null, -1, null, null
        );
        cosmosDbConnector.createData(testEntity);
    }

    @AfterAll
    static void tearDownClass() {
        try {
            cosmosDbConnector.deleteData(deleteContainerEntity);
        } catch (Exception e) {
            // Ignore errors, we are just cleaning up
            System.out.println("Cleanup warning (AfterAll): " + e.getMessage());
        }
    }

    @AfterEach
    void tearDownTest() {
        try {
            cosmosDbConnector.deleteData(testEntity);
        } catch (Exception e) {
            // Ignore errors, we are just cleaning up
            System.out.println("Cleanup warning (AfterEach): " + e.getMessage());
        }
    }

    @BeforeEach
    void setupTest() {
        cosmosDbConnector.postData(testEntity);
    }

    @Test
    void getDataTest() {
        CloudResponse cloudResponse = cosmosDbConnector.getData(testEntity);

        assertInstanceOf(CloudResponse.class, cloudResponse);
        assertEquals(200, cloudResponse.status_code());
        assertTrue(
                cloudResponse.message().contains("retrieved successfully"),
                "Expected retrieval success message"
        );
    }

    @Test
    void postData_ConflictTest() {
        CloudResponse cloudResponse = cosmosDbConnector.postData(testEntity);

        assertEquals(409, cloudResponse.status_code());
        assertTrue(cloudResponse.message().contains("already exists"),
                "Expected conflict message for duplicate ID");
    }

    @Test
    void postDataTest() {
        cosmosDbConnector.deleteData(testEntity);

        CloudResponse cloudResponse = cosmosDbConnector.postData(testEntity);

        assertInstanceOf(CloudResponse.class, cloudResponse);
        assertEquals(200, cloudResponse.status_code());
        assertEquals("Document inserted successfully.", cloudResponse.message());
        assertNull(cloudResponse.data(), "Insert operation should return null data");
    }

    @Test
    void createDataTest() {
        CloudResponse cloudResponse = cosmosDbConnector.createData(testEntity);

        assertInstanceOf(CloudResponse.class, cloudResponse);
        assertEquals(200, cloudResponse.status_code());
        assertTrue(cloudResponse.message().contains("already exists"),
                "Expected container creation or existence message"
        );
    }

    @Test
    void deleteDataTest() {
        CloudResponse cloudResponse = cosmosDbConnector.deleteData(testEntity);

        assertInstanceOf(CloudResponse.class, cloudResponse);
        assertEquals(200, cloudResponse.status_code());
        assertTrue(
                cloudResponse.message().contains("deleted successfully"),
                "Expected deletion success message"
        );
    }

    @Test
    void deleteData_SpecificFieldTest() {
        Entity deleteFieldEntity = new Entity(
                testEntity.module(), testEntity.table(), testEntity.id(),
                "data1", -1, null, null
        );
        CloudResponse deleteCloudResponse = cosmosDbConnector.deleteData(deleteFieldEntity);
        assertEquals(200, deleteCloudResponse.status_code());
        assertTrue(deleteCloudResponse.message().contains("Field 'data1' deleted"));

        CloudResponse getCloudResponse = cosmosDbConnector.getData(testEntity);
        JsonNode fetchedData = getCloudResponse.data().get("data");

        assertFalse(fetchedData.has("data1"), "Field 'data1' should have been deleted");
        assertTrue(fetchedData.has("data2"), "Field 'data2' should still exist");
    }

    @Test
    void deleteContainerTest() {
        CloudResponse cloudResponse = cosmosDbConnector.deleteData(deleteContainerEntity);

        assertInstanceOf(CloudResponse.class, cloudResponse);
        assertEquals(200, cloudResponse.status_code());
        assertTrue(
                cloudResponse.message().contains("deleted successfully"),
                "Expected container deletion success message"
        );
        cosmosDbConnector.createData(testEntity);
    }

    @Test
    void updateDataTest() {
        ObjectNode updatedData = mapper.createObjectNode();
        updatedData.put("data1", 10);
        updatedData.put("data2", 20);

        Entity updateEntity = new Entity(
                "TestModule",
                "TestTable",
                "TestId",
                null,
                -1,
                null,
                updatedData
        );

        CloudResponse cloudResponse = cosmosDbConnector.updateData(updateEntity);

        assertInstanceOf(CloudResponse.class, cloudResponse);
        assertEquals(200, cloudResponse.status_code());
        assertEquals("Document updated successfully.", cloudResponse.message());
        assertNull(cloudResponse.data(), "Update operation should return null data");
    }

    @Test
    void updateData_SpecificFieldTest() {
        ObjectNode dataWrapper = mapper.createObjectNode();
        dataWrapper.put("data1", 999);

        Entity updateFieldEntity = new Entity(
                testEntity.module(), testEntity.table(), testEntity.id(),
                "data1", -1, null, dataWrapper
        );
        CloudResponse updateCloudResponse = cosmosDbConnector.updateData(updateFieldEntity);
        assertEquals(200, updateCloudResponse.status_code());

        CloudResponse getCloudResponse = cosmosDbConnector.getData(testEntity);
        JsonNode fetchedData = getCloudResponse.data().get("data");

        assertEquals(999, fetchedData.get("data1").asInt());
        assertEquals(1, fetchedData.get("data2").asInt());
    }

    @Test
    void updateData_AddNewFieldTest() {
        ObjectNode dataWrapper = mapper.createObjectNode();
        dataWrapper.put("data3", 888);

        Entity updateNewFieldEntity = new Entity(
                testEntity.module(), testEntity.table(), testEntity.id(),
                "data3", -1, null, dataWrapper
        );
        CloudResponse updateCloudResponse = cosmosDbConnector.updateData(updateNewFieldEntity);
        assertEquals(200, updateCloudResponse.status_code());

        CloudResponse getCloudResponse = cosmosDbConnector.getData(testEntity);
        JsonNode fetchedData = getCloudResponse.data().get("data");

        assertTrue(fetchedData.has("data3"));
        assertEquals(888, fetchedData.get("data3").asInt());
    }

    @Test
    void getDataTest_FetchById_WithType() {
        Entity entityWithType = new Entity(
                testEntity.module(),
                testEntity.table(),
                testEntity.id(),
                "data1",
                -1,
                null,
                null
        );

        CloudResponse cloudResponse = cosmosDbConnector.getData(entityWithType);
        assertEquals(200, cloudResponse.status_code());
        assertEquals(0, cloudResponse.data().asInt());
    }

    @Test
    void getDataTest_FetchAll_NoId() {
        Entity fetchAllEntity = new Entity(
                testEntity.module(),
                testEntity.table(),
                null,
                null, -1, null, null
        );

        CloudResponse cloudResponse = cosmosDbConnector.getData(fetchAllEntity);
        assertEquals(200, cloudResponse.status_code());
        assertTrue(cloudResponse.data().isArray());
        assertEquals(1, cloudResponse.data().size());
        assertEquals(testEntity.id(), cloudResponse.data().get(0).path("id").asText());
    }

    @Test
    void getDataTest_FetchAll_WithTimeRange() {
        Entity fetchAllEntity = new Entity(
                testEntity.module(), testEntity.table(),
                null, null, -1, null, null
        );
        CloudResponse getCloudResponse = cosmosDbConnector.getData(fetchAllEntity);
        double timestamp = getCloudResponse.data().get(0).path("timestamp").asDouble();

        TimeRange timeRange = new TimeRange(timestamp - 10000.0f, timestamp + 10000.0f);

        Entity queryEntity = new Entity(
                testEntity.module(), testEntity.table(),
                null, null, -1, timeRange, null
        );

        CloudResponse queryCloudResponse = cosmosDbConnector.getData(queryEntity);
        assertEquals(200, queryCloudResponse.status_code());
        assertTrue(queryCloudResponse.data().isArray());
        assertEquals(1, queryCloudResponse.data().size());
    }

    @Test
    void getDataTest_FetchAll_WithLastNAndType() throws InterruptedException {
        Thread.sleep(10);
        ObjectNode dataNode2 = mapper.createObjectNode().put("data1", 100);
        Entity testEntity2 = new Entity(
                testEntity.module(), "TestTable", "TestId2",
                null, -1, null, dataNode2
        );
        cosmosDbConnector.postData(testEntity2);

        Entity fetchAllEntity = new Entity(
                testEntity.module(), testEntity.table(),
                null, "data1", 1, null, null
        );

        CloudResponse cloudResponse = cosmosDbConnector.getData(fetchAllEntity);
        assertEquals(200, cloudResponse.status_code());
        assertTrue(cloudResponse.data().isArray());
        assertEquals(1, cloudResponse.data().size());
        assertEquals(100, cloudResponse.data().get(0).asInt());

        cosmosDbConnector.deleteData(testEntity2);
    }
}
