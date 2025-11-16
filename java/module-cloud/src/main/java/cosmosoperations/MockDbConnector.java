/******************************************************************************
 * Filename    = MockDbConnector.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Mock implementation of IdbConnector for testing CRUD operations.
 *****************************************************************************/

package cosmosoperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datastructures.CloudResponse;
import datastructures.Entity;
import interfaces.IdbConnector;

/** Mock database connector for testing. */
public class MockDbConnector implements IdbConnector {
    /**Status code for success. */
    private static final int STATUS_OK = 200;

    /** Initialize mock connector. */
    public void init() { }

    /** Mock GET operation. */
    @Override
    public CloudResponse getData(final Entity request) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonData = mapper.createObjectNode();

        jsonData.put("operation", "GET");
        jsonData.put("module", request.module());
        jsonData.put("table", request.table());
        jsonData.put("id", request.id());

        return new CloudResponse(STATUS_OK, "success", jsonData);
    }

    /** Mock POST operation. */
    @Override
    public CloudResponse postData(final Entity request) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonData = mapper.createObjectNode();

        jsonData.put("operation", "POST");
        jsonData.put("module", request.module());
        jsonData.put("table", request.table());
        jsonData.put("id", request.id());

        return new CloudResponse(STATUS_OK, "success", jsonData);
    }

    /** Mock CREATE operation. */
    @Override
    public CloudResponse createData(final Entity request) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonData = mapper.createObjectNode();

        jsonData.put("operation", "CREATE");
        jsonData.put("module", request.module());
        jsonData.put("table", request.table());
        jsonData.put("id", request.id());

        return new CloudResponse(STATUS_OK, "success", jsonData);
    }

    /** Mock DELETE operation. */
    @Override
    public CloudResponse deleteData(final Entity request) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonData = mapper.createObjectNode();

        jsonData.put("operation", "DELETE");
        jsonData.put("module", request.module());
        jsonData.put("table", request.table());
        jsonData.put("id", request.id());

        return new CloudResponse(STATUS_OK, "success", jsonData);
    }

    /** Mock UPDATE operation. */
    @Override
    public CloudResponse updateData(final Entity request) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonData = mapper.createObjectNode();

        jsonData.put("operation", "UPDATE");
        jsonData.put("module", request.module());
        jsonData.put("table", request.table());
        jsonData.put("id", request.id());

        return new CloudResponse(STATUS_OK, "success", jsonData);
    }
}
