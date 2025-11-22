/**
* <p>
 *     Reference:
 *      1. https://medium.com/@kapildevkhatik2/
 *       how-to-run-and-call-local-llms-with-ollama-a-developers-
 *          guide-to-building-offline-ai-apps-1161d9f3a0f8
        * </p>
*/


package com.swe.aiinsights.modeladapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import okhttp3.Response;

import java.io.IOException;

public class OllamaAdapter implements ModelAdapter{

    @Override
    public String buildRequest(RequestGeneraliser request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gemma3");
        root.put("prompt", request.getPrompt());
        root.put("stream", false);

        String imgData = request.getImgData();
        if (imgData != null){
            ArrayNode images = root.putArray("images");
            images.add(request.getImgData());
        } else {
            ArrayNode input = root.putArray("inputData");
            input.add(request.getTextData());
        }

        return objectMapper.writeValueAsString(root);

    }

    @Override
    public String getResponse(Response response) throws IOException {
        assert response.body() != null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJson =
                objectMapper.readTree(response.body().charStream());

        JsonNode textNode = responseJson.get("response");

        if (textNode == null || !textNode.isTextual()) {
            throw new IOException("Invalid Ollama response: "
                    + responseJson.toPrettyString());
        }

        System.out.println("DEBUG >>> ResponseString: Recieved");

        return textNode.asText();
    }
}
