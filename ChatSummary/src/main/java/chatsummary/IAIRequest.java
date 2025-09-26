package chatsummary;

/**
 * Interface for processing AI requests.
 */
public interface IAIRequest {
    String processRequest(AIRequest request, IMeetingData meetingData);
}
