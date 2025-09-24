package chat_summary;


import java.util.List;

/**
 * Interface for meeting data
 */
public interface IMeetingData {
    List<MeetingMessage> getMessages();
    List<String> getParticipants();
    void addMessage(String sender, String message);
    String getChatHistory();
    String getChatHistory(int maxMessages);
    int getMessageCount();
}