package cosmosoperations;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datastructures.CloudResponse;
import datastructures.Entity;
import interfaces.IdbConnector;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Provides CRUD operations for Azure Cosmos DB.
 * Each container represents a table, and each document represents a record.
 */
public class CosmosOperations implements IdbConnector {

    /** Cosmos DB client. */
    private CosmosClient client;

    /** Cosmos DB database instance. */
    private CosmosDatabase database;

    /** JSON object mapper for serialization. */
    private final ObjectMapper mapper = new ObjectMapper();

    /** Endpoint URL for the Cosmos DB account. */
    private String endpoint;

    /** Authorization key for Cosmos DB. */
    private String key;

    /** Database name in Cosmos DB. */
    private String databaseName;

    /** Success status code constant. */
    private static final int HTTP_OK = 200;

    /** Conflict status code constant. */
    private static final int HTTP_CONFLICT = 409;

    /** Not found status code constant. */
    private static final int HTTP_NOT_FOUND = 404;

    /** Bad request status code constant. */
    private static final int HTTP_BAD_REQUEST = 400;

    /**
     * Initializes the Cosmos client using credentials from the .env file.
     */
    @Override
    public void init() {
        try {
            endpoint = System.getenv("COSMOS_ENDPOINT");
            key = System.getenv("COSMOS_KEY");
            databaseName = System.getenv("COSMOS_DATABASE");

            // Fallback to Dotenv for local testing if variables are null
            if (endpoint == null || key == null || databaseName == null) {
                System.out.println("Warning: Environment variables not found. Falling back to .env file...");
                final Dotenv dotenv = Dotenv.load();
                if (endpoint == null) {
                    endpoint = dotenv.get("COSMOS_ENDPOINT");
                }
                if (key == null) {
                    key = dotenv.get("COSMOS_KEY");
                }
                if (databaseName == null) {
                    databaseName = dotenv.get("COSMOS_DATABASE");
                }
            }

            client = new CosmosClientBuilder()
                    .endpoint(endpoint)
                    .key(key)
                    .consistencyLevel(ConsistencyLevel.EVENTUAL)
                    .contentResponseOnWriteEnabled(true)
                    .buildClient();

            database = client.getDatabase(databaseName);
        } catch (final Exception e) {
            System.out.println("Error initializing Cosmos client: " + e.getMessage());
        }
    }

    /**
     * Retrieves data from a specified container.
     * @param request The entity containing the query details.
     * @return A CloudResponse containing matching documents or fields.
     */
    @Override
    public CloudResponse getData(final Entity request) {
        final CosmosContainer container = database.getContainer(request.module() + "_" + request.table());
        if (hasId(request)) {
            return fetchById(container, request);
        }
        return fetchAll(container, request);
    }

    /**
     * Checks if the given request has a valid ID.
     * @param request The entity to check.
     * @return True if the ID is not null or empty, false otherwise.
     */
    private boolean hasId(final Entity request) {
        return request.id() != null && !request.id().isEmpty();
    }

    /**
     * Fetches a single document by its ID.
     * @param container The Cosmos container.
     * @param request The entity containing ID and type.
     * @return CloudResponse with the document or specific field.
     */
    private CloudResponse fetchById(final CosmosContainer container, final Entity request) {
        try {
            final CosmosItemResponse<JsonNode> itemResponse =
                    container.readItem(request.id(), new PartitionKey(request.id()), JsonNode.class);
            final JsonNode doc = itemResponse.getItem();

            if (request.type() != null && !request.type().isEmpty()) {
                final JsonNode data = doc.path("data").path(request.type());
                if (data.isMissingNode()) {
                    return new CloudResponse(HTTP_NOT_FOUND, "Field '" + request.type() + "' not found.", null);
                }
                return new CloudResponse(HTTP_OK, "Field retrieved successfully.", data);
            }

            return new CloudResponse(HTTP_OK, "Document retrieved successfully.", doc);
        } catch (CosmosException e) {
            if (e.getStatusCode() == HTTP_NOT_FOUND) {
                return new CloudResponse(HTTP_NOT_FOUND, "Document with ID '" + request.id() + "' not found.", null);
            }
            throw e;
        }
    }

    /**
     * Fetches multiple documents with optional filters.
     * @param container The Cosmos container.
     * @param request The entity with query parameters.
     * @return CloudResponse with list of retrieved documents.
     */
    private CloudResponse fetchAll(final CosmosContainer container, final Entity request) {
        final String query = buildQuery(request);

        List<JsonNode> results = container.queryItems(query, new CosmosQueryRequestOptions(), JsonNode.class)
                .stream()
                .sorted((a, b) -> Double.compare(b.path("timestamp").asDouble(), a.path("timestamp").asDouble()))
                .collect(Collectors.toList());

        if (request.lastN() > 0 && results.size() > request.lastN()) {
            results = results.subList(0, request.lastN());
        }

        if (request.type() != null && !request.type().isEmpty()) {
            results = results.stream()
                    .map(r -> r.path("data").path(request.type()))
                    .collect(Collectors.toList());
        }

        return new CloudResponse(HTTP_OK, "Documents retrieved successfully.", mapper.valueToTree(results));
    }

    /**
     * Builds a Cosmos DB SQL query based on time range.
     * @param request The entity containing the time range.
     * @return The generated SQL query.
     */
    private String buildQuery(final Entity request) {
        if (request.timeRange() != null) {
            return String.format(
                    "SELECT * FROM c WHERE c.timestamp BETWEEN %f AND %f",
                    request.timeRange().fromTime(),
                    request.timeRange().toTime()
            );
        }
        return "SELECT * FROM c";
    }

    /**
     * Inserts a new document into the container.
     * @param request The entity containing the data to insert.
     * @return CloudResponse indicating insertion status.
     */
    @Override
    public CloudResponse postData(final Entity request) {
        final CosmosContainer container = database.getContainer(request.module() + "_" + request.table());
        final ObjectNode document = mapper.createObjectNode();

        document.put("id", request.id());
        document.put("timestamp", (double) System.currentTimeMillis());
        document.set("data", request.data());

        try {
            container.createItem(document);
            return new CloudResponse(HTTP_OK, "Document inserted successfully.", null);
        } catch (CosmosException e) {
            if (e.getStatusCode() == HTTP_CONFLICT) {
                return new CloudResponse(HTTP_CONFLICT, "Document with ID '" + request.id() + "' already exists.", null);
            }
            throw e;
        }
    }

    /**
     * Creates a new container if it doesn't exist.
     * @param request The entity specifying the table name.
     * @return CloudResponse indicating creation status.
     */
    @Override
    public CloudResponse createData(final Entity request) {
        final String tableName = request.module() + "_" + request.table();
        final boolean exists = database.readAllContainers()
                .stream()
                .anyMatch(c -> c.getId().equals(tableName));

        if (exists) {
            return new CloudResponse(HTTP_OK, "Container '" + tableName + "' already exists.", null);
        }

        final CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(tableName, "/id");

        database.createContainerIfNotExists(containerProperties);
        return new CloudResponse(HTTP_OK, "Container '" + tableName + "' created successfully.", null);
    }

    /**
     * Deletes a specific document or an entire container.
     * @param request The entity containing table and optional ID.
     * @return CloudResponse indicating deletion result.
     */
    @Override
    public CloudResponse deleteData(final Entity request) {
        final String tableName = request.module() + "_" + request.table();
        final CosmosContainer container = database.getContainer(tableName);

        // Case 1: Delete specific field if both ID and Type are present
        if (hasId(request) && request.type() != null && !request.type().isEmpty()) {
            final CosmosItemResponse<ObjectNode> response = container.readItem(request.id(), new PartitionKey(request.id()), ObjectNode.class);
            final ObjectNode document = response.getItem();

            final JsonNode dataNode = document.get("data");
            if (dataNode != null && dataNode.isObject()) {
                ((ObjectNode) dataNode).remove(request.type());
                document.put("timestamp", (double) System.currentTimeMillis());
                container.replaceItem(document, request.id(), new PartitionKey(request.id()), new CosmosItemRequestOptions());
                return new CloudResponse(HTTP_OK, "Field '" + request.type() + "' deleted successfully.", null);
            }
            return new CloudResponse(HTTP_OK, "Field '" + request.type() + "' not found.", null);
        }

        // Case 2: Delete entire document if ID is present but Type is NOT
        if (hasId(request)) {
            container.deleteItem(request.id(), new PartitionKey(request.id()), new CosmosItemRequestOptions());
            return new CloudResponse(HTTP_OK, "Document deleted successfully.", null);
        }

        // Case 3: Delete entire container if ID is missing
        container.delete();
        return new CloudResponse(HTTP_OK, "Container '" + tableName + "' deleted successfully.", null);
    }

    /**
     * Updates a document with new data.
     * @param request The entity containing ID and updated data.
     * @return CloudResponse indicating update result.
     */
    @Override
    public CloudResponse updateData(final Entity request) {
        final CosmosContainer container = database.getContainer(request.module() + "_" + request.table());

        if (request.type() != null && !request.type().isEmpty()) {
            final CosmosItemResponse<ObjectNode> response = container.readItem(request.id(), new PartitionKey(request.id()), ObjectNode.class);
            final ObjectNode document = response.getItem();

            JsonNode dataNode = document.get("data");
            if (dataNode == null || !dataNode.isObject()) {
                dataNode = mapper.createObjectNode();
                document.set("data", dataNode);
            }

            final JsonNode newValue = request.data().get(request.type());

            if (newValue == null) {
                return new CloudResponse(HTTP_BAD_REQUEST, "New value was not provided for data to be updated: "
                        + request.type(), null);
            }

            ((ObjectNode) dataNode).set(request.type(), newValue);
            document.put("timestamp", (double) System.currentTimeMillis());

            container.replaceItem(document, request.id(), new PartitionKey(request.id()), new CosmosItemRequestOptions());
            return new CloudResponse(HTTP_OK, "Field '" + request.type() + "' updated successfully.", null);
        }

        final ObjectNode document = mapper.createObjectNode();
        document.put("id", request.id());
        document.put("timestamp", (double) System.currentTimeMillis());
        document.set("data", request.data());

        container.replaceItem(document, request.id(), new PartitionKey(request.id()), new CosmosItemRequestOptions());
        return new CloudResponse(HTTP_OK, "Document updated successfully.", null);
    }
}
