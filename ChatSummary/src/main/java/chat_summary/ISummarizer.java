package chat_summary;


public interface ISummarizer {
    String generateSummary(IMeetingData meetingData, AIRequest request);
}