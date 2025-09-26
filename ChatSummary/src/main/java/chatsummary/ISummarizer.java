package chatsummary;

/**
 * Interface for summary generation.
 */
public interface ISummarizer {
    String generateSummary(IMeetingData meetingData, AIRequest request);
}
