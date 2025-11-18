/**
 * Author : Abhirami R Iyer
 * Edited by : Nandhana Sunil
 *             Berelli Gouthami
 */
package com.swe.aiinsights.aiservice;

import com.swe.aiinsights.actionitems.ActionItemsGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.imageinterpreter.ImageInterpreter;
import com.swe.aiinsights.insightsgenerator.InsightsGenerator;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import com.swe.aiinsights.regulariser.ImageRegularize;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.requestprocessor.RequestProcessor;
import com.swe.aiinsights.response.ActionItemsResponse;
import com.swe.aiinsights.response.AiResponse;
import com.swe.aiinsights.response.InsightsResponse;
import com.swe.aiinsights.response.InterpreterResponse;
import com.swe.aiinsights.response.RegulariserResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import com.swe.aiinsights.requestprocessor.SummarisationProcessor;
import com.swe.aiinsights.response.SummariserResponse;


/**
 * Gemini Service builds the request and calls the AI api.
 * Receives the AI response.
 */
public final class GeminiService implements LlmService {
    /**
     * Loads environment variables from the .env file.
     */
    private static Dotenv dotenv = Dotenv.load();
    /**
     * gets the GEMINI_URL_TEMPLATE from the .env file.
     */
    private static final String GEMINI_API_URL_TEMPLATE =
            dotenv.get("GEMINI_URL");
    /**
     *  Sets the Media type used for JSON requests.
     */
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");
    /**
     * The Gemini API key used for request authentication.
     */
    private final String geminiApiKey;
    /**
     * Json parser -- serialising and deserialising.
     */
    private ObjectMapper objectMapper;
    /**
     * http client for the requests.
     */
    private final OkHttpClient httpClient;
    /**
     * registry to hold the request processor against request types.
     */
    private final HashMap<String, RequestProcessor> registry =
            new HashMap<>();

    /**
     * Constructor for initialising the http client for making the request.
     */
    public GeminiService() {
        //fetched the api key from the
        // env file (to be changed to fetch from cloud)
        this.geminiApiKey = dotenv.get("GEMINI_API_KEY");

        final int timeout = 200;
        final int readMul = 6;
        // creating an http client
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout * readMul, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();

        // initialises the request builders to redirect to specific requests
        registry.put("REG", new ImageRegularize());
        registry.put("DESC", new ImageInterpreter());
        registry.put("INS", new InsightsGenerator());
        registry.put("SUMMARISE", new SummarisationProcessor());
        registry.put("ACTION", new ActionItemsGenerator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AiResponse runProcess(final AiRequestable aiRequest)
            throws IOException {
        this.objectMapper = new ObjectMapper();
        AiResponse returnResponse = null;

        if (Objects.equals(aiRequest.getReqType(), "DESC")) {
            // if the request is of image interpretation,
            // the request for it is built.
            returnResponse = new InterpreterResponse();
        } else if (Objects.equals(aiRequest.getReqType(), "REG")) {
            // the request is of image regularization,
            // the request builder for regularization is called
            returnResponse = new RegulariserResponse();
        } else if (Objects.equals(aiRequest.getReqType(), "INS")) {
            // the request is for insights generation
            returnResponse = new InsightsResponse();
        } else if (Objects.equals(aiRequest.getReqType(), "SUMMARISE")) {
            returnResponse = new SummariserResponse();
        } else if (Objects.equals(aiRequest.getReqType(), "ACTION")) {
                returnResponse = new ActionItemsResponse();
        }

        // from the registry we will get the requestProcessor
        // according to the request type.
        System.out.println("DEBUG >>> ReqType: " + aiRequest.getReqType());
        System.out.println("DEBUG >>> Registered keys: " + registry.keySet());

        RequestProcessor processor =
                registry.get(aiRequest.getReqType());
        if (processor == null) {
            throw new IllegalArgumentException("No processor found "
                    +  "for request type: "
                    + aiRequest.getReqType());
        }



        // We get the json request string to send to
        // the api from the request processor.
        String jsonRequestBody =
                processor.processRequest(this.objectMapper, aiRequest);
        // the api url is created concatenating
        // the url template and the api key
        final String apiUrl =
                GEMINI_API_URL_TEMPLATE + geminiApiKey;

        // the request body is created using the json string.
        final RequestBody body =
                RequestBody.create(jsonRequestBody, JSON);

        // the post request is created.
        final Request request =
                new Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        .build();

        // http post
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new
                        IOException("Unexpected code"
                        + response + " - " + response.body().string());
            }

            // extracting the response and add to the response object
            final JsonNode responseJson =
                    objectMapper.readTree(response.body().charStream());
            final JsonNode textNode =
                    responseJson.at("/candidates/0/content/parts/0/text");

            // if the response is a text
            if (textNode.isTextual()) {
                if (returnResponse != null) {
                    // create the return response with the string given by AI
                    String responseToreturn = textNode.asText();
                    responseToreturn = responseToreturn
                                                .replaceAll("```json", "")
                                                .replaceAll("```", "")
                                                .trim();
                    returnResponse.setResponse(responseToreturn);
                }
                return returnResponse;
            } else {
                throw new
                        IOException("No text in api response : "
                        + responseJson.toPrettyString());
            }
        }

    }
}
