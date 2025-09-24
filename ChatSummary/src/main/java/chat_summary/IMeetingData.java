package chat_summary;


import java.util.List;

public interface IMeetingData {
    List<MeetingMessage> getMessages();
    List<String> getParticipants();
    void addMessage(String sender, String message);
    String getChatHistory();
    String getChatHistory(int maxMessages);
    int getMessageCount();
}