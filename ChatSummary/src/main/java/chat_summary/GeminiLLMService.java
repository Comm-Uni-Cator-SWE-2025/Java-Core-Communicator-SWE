package chat_summary;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class GeminiLLMService implements ILLMService {

    private static final String API_KEY = "AIzaSyA2Av1dTg9DWmlKDlNskffj0nGn9fisjoI";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private HttpClient httpClient;

    public GeminiLLMService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String generateContent(String prompt) {
        try {
            String requestBody = String.format(
                    "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                    escapeJson(prompt)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", API_KEY)
                    .header("Accept", "application/json")
                    .POST(BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractTextFromResponse(response.body());
            } else {
                throw new RuntimeException("HTTP request failed with code: " + response.statusCode() +
                        ". Response: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String extractTextFromResponse(String response) {
        try {
            int textStart = response.indexOf("\"text\": \"");
            if (textStart == -1) return "Could not find text field in response.";

            textStart += 9;
            int textEnd = textStart;
            boolean inEscape = false;

            while (textEnd < response.length()) {
                char c = response.charAt(textEnd);
                if (inEscape) inEscape = false;
                else if (c == '\\') inEscape = true;
                else if (c == '"') break;
                textEnd++;
            }

            String text = response.substring(textStart, textEnd);
            return text.replace("\\\"", "\"").replace("\\\\", "\\")
                    .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}