/******************************************************************************
 * Filename    = CloudHelper.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Defines helper functions for handling HTTP requests and errors.
 *****************************************************************************/

package functionapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import datastructures.CloudResponse;

import java.util.Optional;

/**
 * Provides common helper methods for Azure Function HTTP handlers,
 * including JSON serialization and standardized success/error responses.
 */
public class CloudHelper {
    /**
     * Jackson ObjectMapper instance used for JSON serialization/deserialization.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    protected HttpResponseMessage handleError(final HttpRequestMessage<Optional<String>> request) {
        final CloudResponse errorCloudResponse = new CloudResponse(400, "bad request", NullNode.getInstance());
        try {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(objectMapper.writeValueAsString(errorCloudResponse))
                    .header("Content-Type", "application/json")
                    .build();
        } catch (Exception ex) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid request")
                    .build();
        }
    }

    protected HttpResponseMessage handleResponse(final CloudResponse cloudResponse,
                                                 final HttpRequestMessage<Optional<String>> request) {
        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(objectMapper.writeValueAsString(cloudResponse))
                    .header("Content-Type", "application/json")
                    .build();
        } catch (Exception e) {
            return handleError(request);
        }
    }
}
