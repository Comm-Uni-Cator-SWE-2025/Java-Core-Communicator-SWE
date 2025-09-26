package chatsummary;

/**
 * Interface for processing AI requests.
 */
public interface IAIRequest {
    /**
     * Process an AI request with meeting data.
     *
     * @param request the AI request
     * @param meetingData the meeting data
     * @return processed result
     */
    String processRequest(AIRequest request, IMeetingData meetingData);
}
