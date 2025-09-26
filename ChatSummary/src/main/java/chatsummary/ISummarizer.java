package chatsummary;

/**
 * Interface for summary generation.
 */
public interface ISummarizer {
    /**
     * Generate a summary from meeting data and request.
     *
     * @param meetingData the meeting data
     * @param request the AI request
     * @return generated summary
     */
    String generateSummary(IMeetingData meetingData, AIRequest request);
}
