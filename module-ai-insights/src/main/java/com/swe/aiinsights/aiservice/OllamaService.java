/**
 * Author : Abhirami R Iyer
 */
package com.swe.aiinsights.aiservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.imageinterpreter.ImageInterpreter;
import com.swe.aiinsights.insightsgenerator.InsightsGenerator;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import com.swe.aiinsights.regulariser.ImageRegularize;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.requestprocessor.RequestProcessor;
import com.swe.aiinsights.requestprocessor.SummarisationProcessor;
import com.swe.aiinsights.response.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class OllamaService implements LlmService {

    private static Dotenv dotenv = Dotenv.load();

    private static final String OLLAMA_URL = dotenv.get("OLLAMA_URL");
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final HashMap<String, RequestProcessor> registry =
            new HashMap<>();

    public OllamaService() {
        final int timeout = 200;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();

        registry.put("REG", new ImageRegularize());
        registry.put("DESC", new ImageInterpreter());
        registry.put("INS", new InsightsGenerator());
        registry.put("SUMMARISE", new SummarisationProcessor());

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AiResponse runProcess(final AiRequestable aiRequest)
            throws IOException {

        AiResponse returnResponse = null;

        // ---- Build JSON using ObjectMapper ----
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gemma3");
        root.put("prompt", aiRequest.getContext());
        root.put("stream", false);

        switch (aiRequest.getReqType()) {
            case "DESC":
                returnResponse = new InterpreterResponse();
                ArrayNode images = root.putArray("images");
                images.add(aiRequest.getInput().toString()); // base64 string
                break;

            case "REG":
                returnResponse = new RegulariserResponse();
                break;

            case "INS":
                returnResponse = new InsightsResponse();
                break;

            case "SUMMARISE":
                returnResponse = new SummariserResponse();
                break;

            default:
                throw new IllegalArgumentException("Unsupported type: " + aiRequest.getReqType());
        }

        // ---- FIXED: Serialize JSON properly ----
        String jsonRequestBody = objectMapper.writeValueAsString(root);


        // ---- Send request to Ollama ----
        RequestBody body = RequestBody.create(jsonRequestBody, JSON);
        Request request = new Request.Builder()
                .url(OLLAMA_URL)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response
                        + " - " + response.body().string());
            }

            JsonNode responseJson =
                    objectMapper.readTree(response.body().charStream());

            JsonNode textNode = responseJson.get("response");

            if (textNode == null || !textNode.isTextual()) {
                throw new IOException("Invalid Ollama response: "
                        + responseJson.toPrettyString());
            }

            returnResponse.setResponse(textNode.asText());
            return returnResponse;
        }
    }
}
