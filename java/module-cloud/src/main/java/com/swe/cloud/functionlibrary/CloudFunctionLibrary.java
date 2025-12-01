/******************************************************************************
 * Filename    = CloudFunctionLibrary.java
 * Author      = kallepally sai kiran
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = ASYNC Function Library for calling Azure Function APIs
 *****************************************************************************/

package functionlibrary;

import com.fasterxml.jackson.databind.ObjectMapper;
import datastructures.CloudResponse;
import datastructures.Entity;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;
/**
 * Function Library for calling Azure Cloud Function APIs asynchronously.
 */

public class CloudFunctionLibrary {

    /** Base URL of the Cloud Functions. */
    private String baseUrl;

    /** HTTP client for requests. */
    private final HttpClient httpClient;

    /** JSON serializer/deserializer. */
    private final ObjectMapper objectMapper;

    /** Constructor loads base URL from .env and initializes client/mapper. */
    public CloudFunctionLibrary() {
        baseUrl = System.getenv("CLOUD_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            final Dotenv dotenv = Dotenv.load();
            baseUrl = dotenv.get("CLOUD_BASE_URL");
        }
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
    }

    /**
     * Generic function to make HTTP calls.
     *
     * @param api Endpoint after base URL
     * @param method HTTP method ("POST" or "PUT")
     * @param payload JSON payload
     * @return CloudResponse body as string
     */
    private CompletableFuture<String> callAPIAsync(final String api, final String method, final String payload) {
        final HttpRequest.Builder httpBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + api))
                .header("Content-Type", "application/json");

        switch (method.toUpperCase()) {
            case "POST":
                httpBuilder.POST(HttpRequest.BodyPublishers.ofString(payload));
                break;
            case "PUT":
                httpBuilder.PUT(HttpRequest.BodyPublishers.ofString(payload));
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        final HttpRequest httpRequest = httpBuilder.build();
        return httpClient
                .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /** Convert JSON to CloudResponse.
     *
     * @param  json Contains the Response with the type string
     * @return convert the json in to type CloudResponse
     * */
    private CloudResponse convertToResponse(final String json) {
        try {
            return objectMapper.readValue(json, CloudResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CloudResponse JSON: " + json, e);
        }
    }

    /** Calls /cloudcreate endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type CloudResponse
     * */
    public CompletableFuture<CloudResponse> cloudCreate(final Entity request) {
        try {
            final String payload = objectMapper.writeValueAsString(request);
            return callAPIAsync("/cloudcreate", "POST", payload)
                    .thenApply(this::convertToResponse);
        } catch (Exception e) {
            final CompletableFuture<CloudResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /** Calls /clouddelete endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type CloudResponse
     * */
    public CompletableFuture<CloudResponse> cloudDelete(final Entity request) {
        try {
            final String payload = objectMapper.writeValueAsString(request);
            return callAPIAsync("/clouddelete", "POST", payload)
                    .thenApply(this::convertToResponse);
        } catch (Exception e) {
            final CompletableFuture<CloudResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /** Calls /cloudget endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type CloudResponse
     * */
    public CompletableFuture<CloudResponse> cloudGet(final Entity request) {
        try {
            final String payload = objectMapper.writeValueAsString(request);
            return callAPIAsync("/cloudget", "POST", payload)
                    .thenApply(this::convertToResponse);
        } catch (Exception e) {
            final CompletableFuture<CloudResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /** Calls /cloudpost endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type CloudResponse
     * */
    public CompletableFuture<CloudResponse> cloudPost(final Entity request) {
        try {
            final String payload = objectMapper.writeValueAsString(request);
            return callAPIAsync("/cloudpost", "POST", payload)
                    .thenApply(this::convertToResponse);
        } catch (Exception e) {
            final CompletableFuture<CloudResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /** Calls /cloudupdate endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type CloudResponse
     * */
    public CompletableFuture<CloudResponse> cloudUpdate(final Entity request) {
        try {
            final String payload = objectMapper.writeValueAsString(request);
            return callAPIAsync("/cloudupdate", "PUT", payload)
                    .thenApply(this::convertToResponse);
        } catch (Exception e) {
            final CompletableFuture<CloudResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Sends a telemetry log to the cloud asynchronously.
     *
     * @param moduleName The name of the module sending the log.
     * @param severity   The severity level of the log (e.g., INFO, ERROR).
     * @param message    The log message.
     * @return A CompletableFuture that completes when the log is sent.
     */
    public CompletableFuture<Void> sendLog(final String moduleName, final String severity, final String message) {
        try {
            final Map<String, String> logData = new HashMap<>();
            logData.put("moduleName", moduleName);
            logData.put("severity", severity);
            logData.put("message", message);

            final String payload = objectMapper.writeValueAsString(logData);

            return callAPIAsync("/telemetry/log", "POST", payload)
                    .thenAccept(response -> {
                        // Optionally handle response or log success
                    });

        } catch (final Exception e) {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }


}