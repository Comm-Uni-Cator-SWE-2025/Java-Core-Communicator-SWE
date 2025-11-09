/**
 * Author : Abhirami R Iyer
 */
package regulariser;

import request.AiRequestable;
import requestprocessor.RequestProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;


/**
 * Class to create a request string specific to image regularization.
 */
public class ImageRegularize implements RequestProcessor {
    /**
     * Adds the request details to get the request string.
     *
     * @param objectMapper
     * the objectMapper for adding the details of the request
     * @param aiRequest
     * the AI request containing metadata(which would have the prompt)
     * @return a String
     * containing the json request specific to the kind of request
     * @throws IOException
     * if the HTTP request or response parsing fails
     */
    @Override
    public String processRequest(
            final ObjectMapper objectMapper, final AiRequestable aiRequest)
            throws IOException {

        // building the json request body(as expected by gemini api)
        final ObjectNode rootNode = objectMapper.createObjectNode();
        final ArrayNode contentsArray = rootNode.putArray("contents");
        final ObjectNode contentNode = contentsArray.addObject();
        final ArrayNode partsArray = contentNode.putArray("parts");

        // adds the prompt for the regularisation code.
        partsArray.addObject().put("text", aiRequest.getContext());

        // gets the json string for the api call.
        final String jsonRequestBody =
                objectMapper.writerWithDefaultPrettyPrinter(
                ).writeValueAsString(rootNode);

        return jsonRequestBody;
    }

}
