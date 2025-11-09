/**
 * Does the sentiment analysis and generates insights.
 * <p>
 * This module is for generating insights from the chats.
 * Chats send the messages to the AI's api end point every 10 mins.
 * Based on the chats received, AI generates insights graph.
 * </p>
 * <p>
 *     References :
 *          1. https://medium.com/google-cloud/
 *          generating-request-body-for-apis-using-gemini-43977961ca2a
 *          2. https://github.com/tanaikech/
 *          Generating-Request-Body-for-APIs-using-Gemini
 * </p>
 *
 * @author Nandhana Sunil
 * @version 1.0.0
 * @since 1.0.0
 */
package insightsgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import request.AiRequestable;
import requestprocessor.RequestProcessor;

import java.io.IOException;

public class InsightsGenerator implements RequestProcessor {
    /**
     * Adds the request details to get the request string.
     *
     * @param objectMapper
     * the objectMapper for adding the details of the request
     * @param request
     * the AI request containing metadata(which would have the prompt)
     * @return a String
     * containing the json request specific to the kind of request
     * @throws IOException
     * if the HTTP request or response parsing fails
     */
    @Override
    public String processRequest(
            final ObjectMapper objectMapper,
            final AiRequestable request
    )throws IOException {
        // Creates a tree-like json structure to send to LLM
        final ObjectNode rootNode = objectMapper.createObjectNode();
//      // Root node will be "contents"
        final ArrayNode arrayNode = rootNode.putArray("contents");
        final ObjectNode contentNode = arrayNode.addObject();
        // Its child will be "parts"
        final ArrayNode partsArray = contentNode.putArray("parts");

        // Add  the  prompt
        partsArray.addObject().put("text", request.getContext());

        // Add the chat data as text
        String content = request.getInput().toString();
        partsArray.addObject().put("text", content);

        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(rootNode);

    }
}
