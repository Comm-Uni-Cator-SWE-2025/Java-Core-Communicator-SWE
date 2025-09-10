package imageInterpreter;

import java.io.File;
import java.io.IOException;

public interface IImageInterpreter {
    IAIResponse describeImage(IAIRequest aiRequest, WhiteBoardData whiteboardData) throws IOException;
}
