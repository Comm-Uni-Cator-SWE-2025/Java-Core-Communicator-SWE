package imageInterpreter;

import java.util.Map;

public interface IAIRequest {

    Map<String, String> metaData = Map.of();

    String getContext();
}
