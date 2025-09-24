package chat_summary;


public interface  IAIRequest {
    String processRequest(AIRequest request, IMeetingData meetingData);
}