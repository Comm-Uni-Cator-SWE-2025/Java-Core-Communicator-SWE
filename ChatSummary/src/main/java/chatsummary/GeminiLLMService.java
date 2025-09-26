package chatsummary;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Connects to Google's Gemini AI API.
 */
public class GeminiLLMService implements ILLMService {

    /** The API key for Gemini service. */
    private final String apiKey;
    /** The API URL for Gemini service. */
    private final String apiUrl;
    /** Connection timeout in seconds. */
    private final int connectTimeoutSeconds;
    /** Request timeout in seconds. */
    private final int requestTimeoutSeconds;
    /** HTTP client for making requests. */
    private HttpClient httpClient;

    /** Default connect timeout. */
    private static final int DEFAULT_CONNECT_TIMEOUT = 10;
    /** Default request timeout. */
    private static final int DEFAULT_REQUEST_TIMEOUT = 30;
    /** HTTP OK status code. */
    private static final int HTTP_OK = 200;
    /** Text field start offset. */
    private static final int TEXT_FIELD_OFFSET = 9;
    /** Minimum key length for masking. */
    private static final int MIN_KEY_LENGTH = 8;
    /** Masked portion length. */
    private static final int MASK_PORTION = 4;

    /**
     * Constructor that initializes the Gemini LLM service.
     */
    public GeminiLLMService() {
        // Load configuration from environment
        EnvConfig.loadEnv();

        // Get configuration values with validation
        this.apiKey = EnvConfig.getEnv("GEMINI_API_KEY");
        this.apiUrl = EnvConfig.getEnv("GEMINI_API_URL",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent");
        this.connectTimeoutSeconds = EnvConfig.getEnvAsInt("HTTP_CONNECT_TIMEOUT_SECONDS", DEFAULT_CONNECT_TIMEOUT);
        this.requestTimeoutSeconds = EnvConfig.getEnvAsInt("HTTP_REQUEST_TIMEOUT_SECONDS", DEFAULT_REQUEST_TIMEOUT);

        // Validate required configuration
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("GEMINI_API_KEY environment variable is required");
        }

        // Create HTTP client with configured timeout
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();

        System.out.println("GeminiLLMService initialized with:");
        System.out.println("  API URL: " + apiUrl);
        System.out.println("  Connect timeout: " + connectTimeoutSeconds + " seconds");
        System.out.println("  Request timeout: " + requestTimeoutSeconds + " seconds");
        System.out.println("  API Key: " + maskApiKey(apiKey));
    }

    @Override
    public String generateContent(final String prompt) {
        try {
            // Prepare request body for Gemini API
            final String requestBody = String.format(
                    "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                    escapeJson(prompt)
            );

            // Create HTTP request
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", apiKey)
                    .header("Accept", "application/json")
                    .POST(BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .build();

            // Send request and get response
            final HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                return extractTextFromResponse(response.body());
            } else {
                throw new RuntimeException("HTTP request failed with code: " + response.statusCode()
                        + ". Response: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to format text for JSON.
     *
     * @param text the text to escape
     * @return escaped JSON text
     */
    private String escapeJson(final String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Extract the actual text from Gemini's JSON response.
     *
     * @param response the JSON response
     * @return extracted text
     */
    private String extractTextFromResponse(final String response) {
        try {
            int textStart = response.indexOf("\"text\": \"");
            if (textStart == -1) {
                return "Could not find text field in response.";
            }

            textStart += TEXT_FIELD_OFFSET;
            int textEnd = textStart;
            boolean inEscape = false;

            while (textEnd < response.length()) {
                final char c = response.charAt(textEnd);
                if (inEscape) {
                    inEscape = false;
                } else if (c == '\\') {
                    inEscape = true;
                } else if (c == '"') {
                    break;
                }
                textEnd++;
            }

            final String text = response.substring(textStart, textEnd);
            return text.replace("\\\"", "\"").replace("\\\\", "\\")
                    .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    /**
     * Helper method to mask API key for logging.
     *
     * @param key the API key
     * @return masked key
     */
    private String maskApiKey(final String key) {
        if (key == null || key.length() < MIN_KEY_LENGTH) {
            return "***";
        }
        return key.substring(0, MASK_PORTION) + "***" + key.substring(key.length() - MASK_PORTION);
    }
}