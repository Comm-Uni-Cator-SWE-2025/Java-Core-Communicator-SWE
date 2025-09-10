package imageInterpreter;

import java.util.HashMap;
import java.util.Map;

public class InterpreterResponse implements IAIResponse {
    String type;
    Map<String, String> metaData;

    public InterpreterResponse(){
        type = "Description Response";
        metaData = new HashMap<>();
    }

    @Override
    public String getResponse() {
        return metaData.get("Content");
    }

    @Override
    public void setResponse(String content) {
        metaData.put("Content", content);
    }
}
