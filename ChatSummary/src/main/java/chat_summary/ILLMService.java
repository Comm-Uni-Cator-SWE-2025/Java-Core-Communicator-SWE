package chat_summary;

/**
 * Interface for LLM Service as shown in UML diagram
 */
public interface ILLMService {
    String generateContent(String prompt);
}