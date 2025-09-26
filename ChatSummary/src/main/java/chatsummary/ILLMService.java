package chatsummary;

/**
 * Interface for any AI service (Gemini, OpenAI, etc.).
 */
public interface ILLMService {
    /**
     * Generate content based on a prompt.
     *
     * @param prompt the input prompt
     * @return generated content
     */
    String generateContent(String prompt);
}