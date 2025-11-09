/**
 * Author Berelli Gouthami
 */
package requestprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import request.AiRequestable;

/**
 * Handles the creation of the JSON body used for chat summarisation requests.
 * This processor takes chat data provided in the request and structures it
 * into the JSON format expected by the Gemini API. The goal is to include both
 * the summarisation instruction and the conversation content
 * in a single payload.
 */
public class SummarisationProcessor implements RequestProcessor {

    /**
     * Converts the summarisation request into a Gemini-compatible JSON string.
     * The resulting JSON contains a clear prompt asking the AI to summarise
     * the provided chat content.
     *
     * @param objectMapper the Jackson mapper used for JSON creation
     * @param aiRequest the summarisation request containing the chat data
     * @return JSON string representing the request body
     */
    @Override
    public String processRequest(final ObjectMapper objectMapper,
                                 final AiRequestable aiRequest) {
        try {
            // Build the text prompt for the model
            final String prompt =
                    "Summarize the following chat:\n" + aiRequest.getInput();

            // Create JSON objects in Gemini's expected structure
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode content = objectMapper.createObjectNode();
            ObjectNode part = objectMapper.createObjectNode();

            // Add the summarisation prompt to the request
            part.put("text", prompt);
            content.set("parts",
                    objectMapper.createArrayNode().add(part));
            root.set("contents",
                    objectMapper.createArrayNode().add(content));

            // Return the final JSON as a string
            return objectMapper.writeValueAsString(root);

        } catch (Exception e) {
            throw new RuntimeException("Error building"
                    + " summarisation request JSON", e);
        }
    }
}
