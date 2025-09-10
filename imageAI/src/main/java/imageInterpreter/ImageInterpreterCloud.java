package imageInterpreter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.api.client.json.JsonFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

public class ImageInterpreterCloud implements IImageInterpreter {

    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String geminiApiKey;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public ImageInterpreterCloud(String geminiApiKey){
        this.geminiApiKey = geminiApiKey;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public IAIResponse describeImage(IAIRequest aiRequest, WhiteBoardData whiteboardData) throws IOException {
        String apiUrl = GEMINI_API_URL_TEMPLATE + geminiApiKey;

        IAIResponse returnResponse = new InterpreterResponse();

        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contentsArray = rootNode.putArray("contents");
        ObjectNode contentNode = contentsArray.addObject();
        ArrayNode partsArray = contentNode.putArray("parts");

        partsArray.addObject().put("text", aiRequest.getContext());

        ObjectNode inlineDataNode = partsArray.addObject().putObject("inlineData");
        inlineDataNode.put("mimeType", "image/png");
        inlineDataNode.put("data", whiteboardData.getContent());

        String jsonRequestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

        RequestBody body = RequestBody.create(jsonRequestBody, JSON);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .build();

        System.out.println("Sending request to GEMINI API");

        try (Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code" + response + " - " + response.body().string());
            }

            JsonNode responseJson = objectMapper.readTree(response.body().charStream());

            JsonNode textNode = responseJson.at("/candidates/0/content/parts/0/text");

            if (textNode.isTextual()){
                returnResponse.setResponse(textNode.asText());
                return returnResponse;
            }
            else{
                throw new IOException("Could not find text in Gemini api response : " + responseJson.toPrettyString());
            }
        }

    }

}
