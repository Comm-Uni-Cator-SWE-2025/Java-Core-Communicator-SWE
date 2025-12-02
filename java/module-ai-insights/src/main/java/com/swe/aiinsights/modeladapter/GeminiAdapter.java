/*
 * -----------------------------------------------------------------------------
 *  File: GeminiAdapter.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.modeladapter
 * -----------------------------------------------------------------------------
 */

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

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

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
     * Get the log file path.
     */
    private static final SweLogger LOG =
            SweLoggerFactory.getLogger("AI-INSIGHTS");

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildRequest(final RequestGeneraliser request)
            throws JsonProcessingException {

        LOG.info("Building request in Gemini format");
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
            LOG.info("Embedding image in Gemini request");
            final ObjectNode inlineDataNode =
                    partsArray.addObject().putObject("inlineData");

            // add the image into the request body
            inlineDataNode.put("mimeType", "image/png");
            inlineDataNode.put("data", request.getImgData());
        } else if (request.getTextData() != null) {
            LOG.info("Embedding text input data in the Gemini request");
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
            LOG.info("Response is textual..Extracting text!");
            return textNode.asText();
        } else {
            LOG.error("No text in api response : "
                    + responseJson.toPrettyString());
            throw new
                    IOException("No text in api response : "
                    + responseJson.toPrettyString());
        }
    }

}
