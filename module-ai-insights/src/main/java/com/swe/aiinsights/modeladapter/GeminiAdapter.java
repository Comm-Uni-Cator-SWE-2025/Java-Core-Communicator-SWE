/**
 * <p>
 * The GeminiAdapter converts generalised request data into the JSON format.
 * required by the Gemini API and extracts the textual response from the
 * API output.
 * </p>
 *
 * <p>
 * References:
 *     1. https://ai.google.dev/gemini-api/docs/api-key
 *     2. https://ai.google.dev/gemini-api/docs#rest
 * </p>
 *
 * @author Abhirami R Iyer
 *
 *
 */

package com.swe.aiinsights.modeladapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import okhttp3.Response;

import java.io.IOException;

/**
 * Implements the ModelAdapter interface.
 * Converts the generalised request to Json specific to Gemini.
 * Also gets the AI response
 */
public class GeminiAdapter implements ModelAdapter {

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildRequest(final RequestGeneraliser request)
            throws JsonProcessingException {

        final ObjectMapper objectMapper = new ObjectMapper();
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
        partsArray.addObject().put("text", request.getPrompt());
        if (request.getImgData() != null) {
            final ObjectNode inlineDataNode =
                    partsArray.addObject().putObject("inlineData");

            // add the image into the request body
            inlineDataNode.put("mimeType", "image/png");
            inlineDataNode.put("data", request.getImgData());
        } else if (request.getTextData() != null) {
            partsArray.addObject().put("text", request.getTextData());
        }

        // Convert it to a json string
        return objectMapper.writerWithDefaultPrettyPrinter(
        ).writeValueAsString(rootNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse(final Response response)
            throws IOException {
        assert response.body() != null;
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode responseJson =
                objectMapper.readTree(response.body().charStream());
        final JsonNode textNode =
                responseJson.at("/candidates/0/content/parts/0/text");

        // if the response is a text
        if (textNode.isTextual()) {
            System.out.println("DEBUG >>> ResponseString: Recieved");
            return textNode.asText();
        } else {
            throw new
                    IOException("No text in api response : "
                    + responseJson.toPrettyString());
        }
    }

}
