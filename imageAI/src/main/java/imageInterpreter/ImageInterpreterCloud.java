package imageinterpreter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cdimascio.dotenv.Dotenv;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

/**
 * Class to implement the gemini api service.
 */
public class ImageInterpreterCloud implements IImageInterpreter {
    /**
     * Loads environment variables from the .env file.
     */
    private static Dotenv dotenv = Dotenv.load();
    /**
     * gets the GEMINI_URL_TEMPLATE from the .env file.
     */
    private static final String GEMINI_API_URL_TEMPLATE = dotenv.get("GEMINI_URL");
    /**
     *  Sets the Media type used for JSON requests.
     */
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    /**
     * The Gemini API key used for request authentication.
     */
    private final String geminiApiKey;
    /**
     * Json parser -- serialising and deserialising.
     */
    private final ObjectMapper objectMapper;
    /**
     * http client for the requests.
     */
    private final OkHttpClient httpClient;

    /**
     * Constructs the ImageInterpreterCloud with a Gemini API key.
     *
     * @param apiKey the API key for authentication
     */
    public ImageInterpreterCloud(final String apiKey) {
        this.geminiApiKey = apiKey;
        final int timeout = 30;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();

    }

    /**
     * Sends an AI request with request data and receives a response.
     *
     * @param aiRequest the AI request containing metadata(which would have the prompt)
     * @param whiteboardData the image data
     * @return an IAIResponse containing AI-generated description
     * @throws IOException if the HTTP request or response parsing fails
     */
    @Override
    public IAIResponse describeImage(final IAIRequest aiRequest, final WhiteBoardData whiteboardData)
            throws IOException {

        final IAIResponse returnResponse = new InterpreterResponse();

        // building the json request body(as expected by gemini api)
        final ObjectNode rootNode = objectMapper.createObjectNode();
        final ArrayNode contentsArray = rootNode.putArray("contents");
        final ObjectNode contentNode = contentsArray.addObject();
        final ArrayNode partsArray = contentNode.putArray("parts");

        partsArray.addObject().put("text", aiRequest.getContext());

        final ObjectNode inlineDataNode = partsArray.addObject().putObject("inlineData");
        inlineDataNode.put("mimeType", "image/png");
        inlineDataNode.put("data", whiteboardData.getContent());

        final String jsonRequestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

        final String apiUrl = GEMINI_API_URL_TEMPLATE + geminiApiKey;
        final RequestBody body = RequestBody.create(jsonRequestBody, JSON);
        final Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .build();

        System.out.println("Sending request to GEMINI API");

        // sending the request as an http post
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code" + response + " - " + response.body().string());
            }

            // extracting the response and add to the response object
            final JsonNode responseJson = objectMapper.readTree(response.body().charStream());
            final JsonNode textNode = responseJson.at("/candidates/0/content/parts/0/text");

            if (textNode.isTextual()) {
                returnResponse.setResponse(textNode.asText());
                return returnResponse;
            } else {
                throw new IOException("Could not find text in Gemini api response : " + responseJson.toPrettyString());
            }
        }

    }

}
