package imageInterpreter;

import java.util.HashMap;
import java.util.Map;

public class AIDescriptionRequest implements IAIRequest{
    Map<String, String> metaData;

    public AIDescriptionRequest(){
        metaData = new HashMap<>();
        metaData.put("RequestPrompt", "Describe this image in detail");
    }

    @Override
    public String getContext(){
        return metaData.get("RequestPrompt");
    }
}
