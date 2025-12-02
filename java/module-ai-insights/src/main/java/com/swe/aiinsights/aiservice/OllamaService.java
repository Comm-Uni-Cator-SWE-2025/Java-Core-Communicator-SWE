/*
 * -----------------------------------------------------------------------------
 *  File: OllamaService.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.aiservice
 * -----------------------------------------------------------------------------
 */

/**
 * Service module for Ollama.
 *
 *<p>
 *     References
 *      1. https://docs.ollama.com/api/usage
 *      2. https://ollama.readthedocs.io/en/api/
 *</p>
 *
 * @author Abhirami R Iyer
 * @editedby Nandhana Sunil
 *
 */

package com.swe.aiinsights.aiservice;

import com.swe.aiinsights.getkeys.GeminiKeyManager;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.modeladapter.ModelAdapter;
import com.swe.aiinsights.modeladapter.OllamaAdapter;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import com.swe.aiinsights.response.AiResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Ollama Service builds the request and calls the AI api.
 * Receives the AI response.
 */
public class OllamaService implements LlmService {

    /**
     * Get the log file path.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("AI-INSIGHTS");


    /**
     * gets the OLLAMA_URL from the .env file.
     */
    private static final String OLLAMA_URL = GeminiKeyManager.getOllamaUrl();
    /**
     * Sets the Media type used for JSON requests.
     */
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");
    /**
     * http client for the requests.
     */
    private final OkHttpClient httpClient;


    public OllamaService() {
        final int timeout = 200;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
        LOG.info("OllamaService initialized with timeout: " + timeout + " seconds");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AiResponse runProcess(final RequestGeneraliser aiRequest)
            throws IOException {

        final AiResponse returnResponse = aiRequest.getAiResponse();

        final ModelAdapter adapter = new OllamaAdapter();
        final String jsonRequestBody = adapter.buildRequest(aiRequest);

        LOG.debug("RequestString recieved");

        // ---- Send request to Ollama ----
        final RequestBody body = RequestBody.create(jsonRequestBody, JSON);
        final Request request = new Request.Builder()
                .url(OLLAMA_URL)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            LOG.debug("Response code: " + response.code());

            if (!response.isSuccessful()) {
                assert response.body() != null;
                final String errorBody = response.body().string();
                LOG.error("Ollama API failed - Code: " + response.code() + ", Error: " + errorBody);
                throw new IOException("Unexpected code " + response
                        + " - " + response.body().string());
            }

            final String textResponse = adapter.getResponse(response);

            returnResponse.setResponse(textResponse);
            LOG.info("Ollama API completed successfully");

            return returnResponse;
        }
    }
}
