package chatsummary;

/**
 * Interface for any AI service (Gemini, OpenAI, etc.).
 */
public interface ILLMService {
    String generateContent(String prompt);
}