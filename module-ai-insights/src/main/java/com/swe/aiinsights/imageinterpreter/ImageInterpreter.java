/**
 * Author : Abhirami R Iyer
 */
package com.swe.aiinsights.imageinterpreter;

import java.io.IOException;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.requestprocessor.RequestProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class to create a request string specific to image interpretation.
 */
public class ImageInterpreter implements RequestProcessor {
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
        final ObjectNode rootNode =
                objectMapper.createObjectNode();
        final ArrayNode contentsArray =
                rootNode.putArray("contents");
        final ObjectNode contentNode =
                contentsArray.addObject();
        final ArrayNode partsArray =
                contentNode.putArray("parts");

        // to add the prompt to the request,
        // describe image as this is for image interpretation
        partsArray.addObject().put("text", aiRequest.getContext());
        final ObjectNode inlineDataNode =
                partsArray.addObject().putObject("inlineData");

        // add the image into the request body
        inlineDataNode.put("mimeType", "image/png");
        inlineDataNode.put("data", aiRequest.getInput().toString());

        // Convert it to a json string
        final String jsonRequestBody =
                objectMapper.writerWithDefaultPrettyPrinter(
                ).writeValueAsString(rootNode);

        return jsonRequestBody;
    }

}
