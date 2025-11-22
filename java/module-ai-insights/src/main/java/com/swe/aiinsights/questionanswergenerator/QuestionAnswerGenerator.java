package com.swe.aiinsights.questionanswergenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.requestprocessor.RequestProcessor;

import java.io.IOException;

/**
 * Builds JSON request for Q&A.
 */
public class QuestionAnswerGenerator implements RequestProcessor {

    @Override
    public String processRequest(
            final ObjectMapper objectMapper,
            final AiRequestable request
    ) throws IOException {

        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contentsArray = rootNode.putArray("contents");
        ObjectNode contentNode = contentsArray.addObject();
        ArrayNode partsArray = contentNode.putArray("parts");

        // Prompt (contains summary inside it)
        partsArray.addObject().put("text", request.getContext().toString());

        // Question by user
        partsArray.addObject().put("text", request.getInput().toString());

        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(rootNode);
    }
}
