/*
 * -----------------------------------------------------------------------------
 *  File: GeminiService.java
 *  Owner: Abhirami R Iyer, Nandhana Sunil
 *  Roll Number : 112201001, 112201008
 *  Module : com.swe.aiinsights.aiservice
 * -----------------------------------------------------------------------------
 */

/**
 * Service module for Gemini.
 *
 * <p>
 * References
 *      1. https://ai.google.dev/gemini-api/docs/rate-limits
 *      2. https://medium.com/google-cloud/
 *          generating-request-body-for-apis-using-gemini-43977961ca2a
 *      3. https://github.com/tanaikech/
 *          Generating-Request-Body-for-APIs-using-Gemini
 * </p>
 *
 * @author Abhirami R Iyer
 * @editedby Nandhana Sunil, Berelli Gouthami
 *
 */

package com.swe.aiinsights.aiservice;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.modeladapter.GeminiAdapter;
import com.swe.aiinsights.modeladapter.ModelAdapter;
import com.swe.aiinsights.getkeys.EncryptKeys;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import com.swe.aiinsights.response.AiResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.swe.aiinsights.customexceptions.RateLimitException;

/**
 * Gemini Service builds the request and calls the AI api.
 * Receives the AI response.
 */
public final class GeminiService implements LlmService {

    /**
     * Get the log file path.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("AI-INSIGHTS");


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
     * key manager which handles the functions related to keys.
     */
    private final GeminiKeyManager keyManager;

    /**
     * Constructor for initialising the http client for making the request.
     */

    public GeminiService() {

        keyManager = new GeminiKeyManager();
        LOG.info("Initializing GeminiService");
        final int timeout = 200;
        final int readMul = 6;
        // creating an http client
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout * readMul, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
        LOG.info("GeminiService initialized with timeout: " + timeout + " seconds");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AiResponse runProcess(final RequestGeneraliser aiRequest)
            throws Exception {

        final ModelAdapter adapter = new GeminiAdapter();

        final String requestBody = adapter.buildRequest(aiRequest);

        final int maxRetries = keyManager.getNumberOfKeys();
//        LOG.info(maxRetries);
        int attempt = 0;
        while (attempt < maxRetries) {
//            LOG.info("Attempt");
//            LOG.info(attempt);
            final String currentKey = EncryptKeys.decrypt(keyManager.getCurrentKey());
            final String apiUrl = GEMINI_API_URL_TEMPLATE + currentKey;

            final Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            final int keyLimitCode = 429;
            final int permissionDenied = 403;

            try (Response response = httpClient.newCall(request).execute()) {
                LOG.info("trying to get response");
                if (response.isSuccessful()) {
                    final AiResponse returnResponse = aiRequest.getAiResponse();
                    final String textResponse = adapter.getResponse(response);
                    returnResponse.setResponse(textResponse);
                    LOG.debug("Response received from adapter");
                    return returnResponse;
                }
                if (response.code() == keyLimitCode) {
                    LOG.debug("Key limit hit\n");
                    keyManager.setKeyIndex(currentKey);
                    attempt++; // Increment attempt and loop again to try next key
//                    LOG.info(attempt);
                    continue;  // Skip the rest and restart loop
                }
                if (response.code() == permissionDenied) {
                    // this part wouldn't be reachable in tests
                    LOG.debug("Permission denied for the key\n");
                    keyManager.setKeyIndex(currentKey);
                    attempt++; // Increment attempt and loop again to try next key
//                    LOG.info(attempt);
                    continue;  // Skip the rest and restart loop
                }
            }
            LOG.debug("Some other error but trying to switch model\n");
            throw new RateLimitException("Some other error but trying to switch model");
        }
        LOG.debug("Some other error but trying to switch model");
        throw new RateLimitException("All available API keys used");
    }
}