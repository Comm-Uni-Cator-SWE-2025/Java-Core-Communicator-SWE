package chat_summary;

/**
 * Interface for Summarizer as shown in UML diagram
 */
public interface ISummarizer {
    String generateSummary(IMeetingData meetingData, AIRequest request);
}