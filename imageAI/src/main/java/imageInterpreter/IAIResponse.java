package imageInterpreter;

import java.util.Map;

public interface IAIResponse {
    String type = "";
    Map<String, String> metaData = Map.of();

    String getResponse();
    void setResponse(String content);
}
