package chatsummary;

import java.util.List;

/**
 * Interface defining what a meeting data class must do.
 */
public interface IMeetingData {
    List<MeetingMessage> getMessages();

    List<String> getParticipants();

    void addMessage(String sender, String message);


    String getChatHistory();

    String getChatHistory(int maxMessages);

    int getMessageCount();
}