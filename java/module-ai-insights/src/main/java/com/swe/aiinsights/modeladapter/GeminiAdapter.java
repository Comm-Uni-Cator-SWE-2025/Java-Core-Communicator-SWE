package com.swe.aiinsights.modeladapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import okhttp3.Response;

import java.io.IOException;

public class GeminiAdapter implements ModelAdapter{

    @Override
    public String buildRequest(RequestGeneraliser request) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
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
        } else {
            partsArray.addObject().put("text", request.getTextData());
        }

        // Convert it to a json string
        return objectMapper.writerWithDefaultPrettyPrinter(
        ).writeValueAsString(rootNode);
    }

    @Override
    public String getResponse(Response response) throws IOException {
        assert response.body() != null;
        ObjectMapper objectMapper = new ObjectMapper();
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
