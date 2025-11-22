package com.swe.aiinsights.summarisationgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.requestprocessor.RequestProcessor;

import java.io.IOException;

/**
 * Builds request JSON for summarisation requests.
 */
public class SummarisationGenerator implements RequestProcessor {

    @Override
    public String processRequest(
            final ObjectMapper objectMapper,
            final AiRequestable request
    ) throws IOException {

        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contents = rootNode.putArray("contents");
        ObjectNode contentNode = contents.addObject();
        ArrayNode parts = contentNode.putArray("parts");

        // prompt
        parts.addObject().put("text", request.getContext());

        // chat data
        parts.addObject().put("text", request.getInput().toString());

        return objectMapper.writeValueAsString(rootNode);
    }
}
