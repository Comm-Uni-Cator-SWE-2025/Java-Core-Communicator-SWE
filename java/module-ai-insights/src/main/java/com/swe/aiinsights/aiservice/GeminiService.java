/**
 * Service module for Gemini.
 *
 * <p>
 * References
 *      1. https://ai.google.dev/gemini-api/docs/rate-limits
 * </p>
 *
 * @author Abhirami R Iyer
 * @editedby Nandhana Sunil, Berelli Gouthami
 *
 */

package com.swe.aiinsights.aiservice;

import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.modeladapter.GeminiAdapter;
import com.swe.aiinsights.modeladapter.ModelAdapter;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import com.swe.aiinsights.response.AiResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.swe.aiinsights.customexceptions.RateLimitException;

// import com.swe.cloud.datastructures.TimeRange;
// import com.swe.cloud.datastructures.Entity;
// import com.swe.cloud.functionlibrary.CloudFunctionLibrary;
// import com.swe.cloud.datastructures.CloudResponse;

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
     * Sets the Media type used for JSON requests.
     */
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");
    /**
     * The Gemini API key used for request authentication.
     */
//    private final String geminiApiKey;

    /**
     * List of Gemini API Keys.
     */
    private List<String> geminiApiKeyList;

    /**
     * Index of the next API Key from the list.
     */
    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);
    /**
     * http client for the requests.
     */
    private final OkHttpClient httpClient;

    /**
     * This method is used to get the list of API Keys.
     * @return list of Gemini API KEYS
     */
    private List<String> getKeyList() {
        final String keys = dotenv.get("GEMINI_API_KEY_LIST");
        if (keys == null || keys.trim().isEmpty()) {
            throw new RuntimeException("GEMINI_API_KEY_LIST is empty or missing");
        }
        // Splits by comma and removes whitespace around keys
        return Arrays.asList(keys.split("\\s*,\\s*"));
    }

    /**
     * Get the next key available.
     * @return thw next key available
     */
    private String getCurrentKey() {
        final int index = apiKeyIndex.get();
        return geminiApiKeyList.get(Math.abs(index));
    }

    /**
     * Using compare and swap, get the currently used keys index.
     * @param expiredKey the expired key - max token count reached
     */
    private void setKeyIndex(final String expiredKey) {
        final int currentIndex = apiKeyIndex.get();
        final String currentKey = geminiApiKeyList.get(Math.abs(currentIndex));
        if (currentKey.equals(expiredKey)) {
            apiKeyIndex.compareAndSet(currentIndex, currentIndex + 1);
            System.out.println(apiKeyIndex);
        }
    }

    /**
     * Constructor for initialising the http client for making the request.
     */

    public GeminiService() {
        //fetched the api key from the
        // env file (to be changed to fetch from cloud)
        /** cloud functions to get key
         CloudFunctionLibrary cloud = new CloudFunctionLibrary();
         Entity req = new Entity("AI_INSIGHT", "credentials", "gemini", "key", -1, new TimeRange(0, 0),null);
         // Response response =testCloudFunctionLibrary.cloudPost(testEntity)
         String key_from_cloud;
         cloud.cloudGet(req).thenAccept(response -> {
         // Object cleanedData = response.data;   // <- NO getData()
         key_from_cloud = response.data();
         });*/
//        this.geminiApiKey = dotenv.get("GEMINI_API_KEY"); //change this in production
        this.geminiApiKeyList = getKeyList();
        final int timeout = 200;
        final int readMul = 6;
        // creating an http client
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout * readMul, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AiResponse runProcess(final RequestGeneraliser aiRequest)
            throws IOException {

        final ModelAdapter adapter = new GeminiAdapter();

        final String requestBody = adapter.buildRequest(aiRequest);

        final int maxRetries = geminiApiKeyList.size();
        System.out.println(maxRetries);
        int attempt = 0;
        while (attempt < maxRetries) {
            System.out.println("Attempt");
            System.out.println(attempt);
            final String currentKey = getCurrentKey();
            final String apiUrl = GEMINI_API_URL_TEMPLATE + currentKey;

            final Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            final int keyLimitCode = 429;

            try (Response response = httpClient.newCall(request).execute()) {
                System.out.println("trying to get response");
                if (response.isSuccessful()) {
                    final AiResponse returnResponse = aiRequest.getAiResponse();
                    final String textResponse = adapter.getResponse(response);
                    returnResponse.setResponse(textResponse);
                    return returnResponse;
                }
                if (response.code() == keyLimitCode) {
                    System.out.println("==========================================================key hit !!!!");
                    setKeyIndex(currentKey);
                    attempt++; // Increment attempt and loop again to try next key
                    System.out.println(attempt);
                    continue;  // Skip the rest and restart loop
                }
            }
        }
        throw new RateLimitException("All available API keys used");
    }
}