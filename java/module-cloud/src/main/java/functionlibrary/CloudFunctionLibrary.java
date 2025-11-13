/******************************************************************************
 * Filename    = CloudFunctionLibrary.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Function Library for calling Azure Function APIs
 *               created in the cloud module.
 *****************************************************************************/

package functionlibrary;

import com.fasterxml.jackson.databind.ObjectMapper;
import datastructures.Entity;
import datastructures.Response;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Function Library for calling Azure Cloud Function APIs.
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
     * @return Response body as string
     */
    private String callAPI(final String api, final String method, final String payload) throws IOException, InterruptedException {
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
        final HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return httpResponse.body();
    }

    /** Calls /cloudcreate endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type Response
     * */
    public Response cloudCreate(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/cloudcreate", "POST", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }

    /** Calls /clouddelete endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type Response
     * */
    public Response cloudDelete(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/clouddelete", "POST", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }

    /** Calls /cloudget endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type Response
     * */
    public Response cloudGet(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/cloudget", "POST", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }

    /** Calls /cloudpost endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type Response
     * */
    public Response cloudPost(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/cloudpost", "POST", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }

    /** Calls /cloudupdate endpoint.
     *
     * @param request Contains the request with type Entity
     * @return response from cloud function with type Response
     * */
    public Response cloudUpdate(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/cloudupdate", "PUT", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }

}
