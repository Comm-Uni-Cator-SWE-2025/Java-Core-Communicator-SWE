/*
 * -----------------------------------------------------------------------------
 *  File: OllamaAdapter.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.modeladapter
 * -----------------------------------------------------------------------------
 */

/**
 *
 * <p>
 * The OllamaAdapter converts generalised request data into Ollama specific format.
 * Converts it into format expected by
 * local Ollama models (e.g., Gemma3) and extracts the
 * plain-text response returned by the Ollama HTTP API.
 * </p>
 *
 * <p>
 * References:
 *     1. https://github.com/ollama/ollama/blob/main/docs/api.md
 *     2. https://www.ollama.com/library
 *     3. https://medium.com/@kapildevkhatik2/
 *       how-to-run-and-call-local-llms-with-ollama-a-developers-
 *          guide-to-building-offline-ai-apps-1161d9f3a0f8
 * </p>
 *
 * @author Abhirami R Iyer
*/


package com.swe.aiinsights.modeladapter;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import okhttp3.Response;
import java.io.IOException;

/**
 * Implements the ModelAdapter interface.
 * Converts the generalised request to Json specific to Ollama models.
 * Also gets the AI response
 */
public class OllamaAdapter implements ModelAdapter {

    /**
     * Get the log file path.
     */
    private static final SweLogger LOG =
            SweLoggerFactory.getLogger("AI-INSIGHTS");

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildRequest(final RequestGeneraliser request) throws JsonProcessingException {
        LOG.info("Building Ollama-specific request Json");
        final int maxPromptTokens = 16384;
        final double modelTemperature = 0.2;
        final double modelTop = 0.9;

        final ObjectMapper objectMapper = new ObjectMapper();
        final ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gemma3");
        root.put("prompt", request.getPrompt() + request.getTextData());
        final ObjectNode options = objectMapper.createObjectNode();
        options.put("num_ctx", maxPromptTokens);
        options.put("temperature", modelTemperature);
        options.put("top_p", modelTop);

        root.set("options", options);

        root.put("stream", false);

        final String imgData = request.getImgData();
        if (imgData != null) {
            LOG.info("Embedding images in the Ollama request");
            final ArrayNode images = root.putArray("images");
            images.add(request.getImgData());
        } else {
            final ArrayNode input = root.putArray("inputData");
            input.add(request.getTextData());
        }

        return objectMapper.writeValueAsString(root);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse(final Response response) throws IOException {
        assert response.body() != null;
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode responseJson =
                objectMapper.readTree(response.body().charStream());

        final JsonNode textNode = responseJson.get("response");

        if (textNode == null || !textNode.isTextual()) {
            LOG.error("Invalid Ollama response:" + responseJson.toPrettyString());
            throw new IOException("Invalid Ollama response: "
                    + responseJson.toPrettyString());
        }

        LOG.info("Response from Ollama received");

        return textNode.asText();
    }
}
