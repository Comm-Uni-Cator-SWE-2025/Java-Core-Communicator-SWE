/******************************************************************************
 * Filename    = CloudFunctionLibrary.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Backend Function Library for calling Azure Function APIs
 *               and exposing them via RPC for remote access.
 *****************************************************************************/

package functionlibrary;

import com.controller.RPCinterface.AbstractRPC;
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
 * Backend Function Library — connects to Azure Function endpoints
 * and exposes RPC handlers for frontend or other module access.
 */
public class CloudFunctionLibrary {

    /** Base URL of the Cloud Functions (from .env). */
    private final String baseUrl;

    /** HTTP client for Azure requests. */
    private final HttpClient httpClient;

    /** JSON serializer/deserializer. */
    private final ObjectMapper objectMapper;

    /** Reference to the RPC instance (for subscription). */
    private AbstractRPC rpc;

    /**
     * Constructor: loads environment variables and initializes utilities.
     */
    public CloudFunctionLibrary() {
        final Dotenv dotenv = Dotenv.load();
        this.baseUrl = dotenv.get("CLOUD_BASE_URL");
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // -----------------------------------------------------
    // 🔹 Initialize RPC Subscriptions
    // -----------------------------------------------------

    /**
     * Initializes the RPC and subscribes backend methods for remote calls.
     * Each subscription maps an RPC call name to a handler that executes
     * the corresponding Azure Function API.
     *
     * @param rpcInstance the initialized RPC instance to bind to.
     */
    public void init(final AbstractRPC rpcInstance) {
        this.rpc = rpcInstance;

        rpc.subscribe("cloudCreate", (byte[] data) -> handleRPCRequest("cloudcreate", "POST", data));
        rpc.subscribe("cloudDelete", (byte[] data) -> handleRPCRequest("clouddelete", "POST", data));
        rpc.subscribe("cloudGet", (byte[] data) -> handleRPCRequest("cloudget", "POST", data));
        rpc.subscribe("cloudPost", (byte[] data) -> handleRPCRequest("cloudpost", "POST", data));
        rpc.subscribe("cloudUpdate", (byte[] data) -> handleRPCRequest("cloudupdate", "PUT", data));

        System.out.println("[CloudFunctionLibrary] RPC Handlers Initialized ✅");
    }

    // -----------------------------------------------------
    // 🔹 Helper: Handle an incoming RPC call
    // -----------------------------------------------------

    /**
     * Handles incoming RPC requests by deserializing data, invoking
     * the corresponding Azure Function API, and returning the response.
     *
     * @param endpoint the Azure Function endpoint.
     * @param method the HTTP method (POST/PUT).
     * @param data the serialized Entity request as byte array.
     * @return serialized Response as byte array.
     */
    private byte[] handleRPCRequest(final String endpoint, final String method, final byte[] data) {
        try {
            final Entity request = objectMapper.readValue(data, Entity.class);

            final String payload = objectMapper.writeValueAsString(request);
            final String jsonResponse = callAPI("/" + endpoint, method, payload);

            final Response response = objectMapper.readValue(jsonResponse, Response.class);
            return objectMapper.writeValueAsBytes(response);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                final Response error = new Response(500, "FAILED: " + e.getMessage(), null);
                error.setStatus("FAILED");
                error.setMessage(e.getMessage());
                return objectMapper.writeValueAsBytes(error);
            } catch (IOException ex) {
                return new byte[0];
            }
        }
    }

    // -----------------------------------------------------
    // 🔹 Core Azure HTTP Caller
    // -----------------------------------------------------

    /**
     * Makes a REST API call to the Azure Function backend.
     *
     * @param api the API endpoint path (e.g., "/cloudcreate").
     * @param method the HTTP method (POST or PUT).
     * @param payload the JSON payload to send.
     * @return raw JSON response as string.
     * @throws IOException if network or serialization fails.
     * @throws InterruptedException if the request is interrupted.
     */
    private String callAPI(final String api, final String method, final String payload)
            throws IOException, InterruptedException {

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

        final HttpResponse<String> httpResponse =
                httpClient.send(httpBuilder.build(), HttpResponse.BodyHandlers.ofString());

        return httpResponse.body();
    }

    // -----------------------------------------------------
    // 🔹 Direct Backend Methods (No RPC)
    // -----------------------------------------------------

    /**
     * Calls the Azure cloudCreate endpoint directly (no RPC).
     *
     * @param request the Entity request object.
     * @return deserialized Response object.
     * @throws IOException if network or JSON serialization fails.
     * @throws InterruptedException if the request is interrupted.
     */
    public Response cloudCreate(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/cloudcreate", "POST", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }

    /**
     * Calls the Azure cloudDelete endpoint directly (no RPC).
     *
     * @param request the Entity request object.
     * @return deserialized Response object.
     * @throws IOException if network or JSON serialization fails.
     * @throws InterruptedException if the request is interrupted.
     */
    public Response cloudDelete(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/clouddelete", "POST", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }

    /**
     * Calls the Azure cloudGet endpoint directly (no RPC).
     *
     * @param request the Entity request object.
     * @return deserialized Response object.
     * @throws IOException if network or JSON serialization fails.
     * @throws InterruptedException if the request is interrupted.
     */
    public Response cloudGet(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/cloudget", "POST", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }

    /**
     * Calls the Azure cloudPost endpoint directly (no RPC).
     *
     * @param request the Entity request object.
     * @return deserialized Response object.
     * @throws IOException if network or JSON serialization fails.
     * @throws InterruptedException if the request is interrupted.
     */
    public Response cloudPost(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/cloudpost", "POST", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }

    /**
     * Calls the Azure cloudUpdate endpoint directly (no RPC).
     *
     * @param request the Entity request object.
     * @return deserialized Response object.
     * @throws IOException if network or JSON serialization fails.
     * @throws InterruptedException if the request is interrupted.
     */
    public Response cloudUpdate(final Entity request) throws IOException, InterruptedException {
        final String payload = objectMapper.writeValueAsString(request);
        final String jsonResponse = callAPI("/cloudupdate", "PUT", payload);
        return objectMapper.readValue(jsonResponse, Response.class);
    }
}
