package imageinterpreter;

import java.io.IOException;

/**
 * Interface for the image interpreter.
 */
public interface IImageInterpreter {
    /**
     * Sends a request to the AI with the given prompt and whiteboard data,
     * and returns the AI's response.
     *
     * @param aiRequest the AI request containing metadata and prompt
     * @param whiteboardData the image or whiteboard data to interpret
     * @return an IAIResponse containing the AI-generated description or other content
     * @throws IOException if an I/O error occurs during communication with the AI
     */
    IAIResponse describeImage(IAIRequest aiRequest, WhiteBoardData whiteboardData) throws IOException;
}
