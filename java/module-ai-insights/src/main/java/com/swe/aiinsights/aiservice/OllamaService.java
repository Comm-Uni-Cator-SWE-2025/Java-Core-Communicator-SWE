/**
 * Author : Abhirami R Iyer
 * Edited by : Nandhana Sunil
 *
 *<p>
 *     References
 *      1. https://docs.ollama.com/api/usage
 *      2. https://ollama.readthedocs.io/en/api/
 *</p>
 */
package com.swe.aiinsights.aiservice;

import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.modeladapter.ModelAdapter;
import com.swe.aiinsights.modeladapter.OllamaAdapter;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import com.swe.aiinsights.response.*;



import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OllamaService implements LlmService {

    private static Dotenv dotenv = Dotenv.load();

    private static final String OLLAMA_URL = dotenv.get("OLLAMA_URL");
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;


    public OllamaService() {
        final int timeout = 200;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public AiResponse runProcess(final RequestGeneraliser aiRequest)
            throws IOException {

        AiResponse returnResponse = aiRequest.getAiResponse();

        ModelAdapter adapter = new OllamaAdapter();
        String jsonRequestBody = adapter.buildRequest(aiRequest);

        System.out.println("DEBUG >>> RequestString: " + jsonRequestBody);


        // ---- Send request to Ollama ----
        RequestBody body = RequestBody.create(jsonRequestBody, JSON);
        Request request = new Request.Builder()
                .url(OLLAMA_URL)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                assert response.body() != null;
                throw new IOException("Unexpected code " + response
                        + " - " + response.body().string());
            }

            String textResponse = adapter.getResponse(response);

            returnResponse.setResponse(textResponse);

            return returnResponse;
        }
    }
}
