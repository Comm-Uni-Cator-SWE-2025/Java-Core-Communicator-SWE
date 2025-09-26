package chatsummary;

import java.util.List;

/**
 * Interface defining what a meeting data class must do.
 */
public interface IMeetingData {
    /**
     * Get all messages.
     *
     * @return list of messages
     */
    List<MeetingMessage> getMessages();

    /**
     * Get all participants.
     *
     * @return list of participants
     */
    List<String> getParticipants();

    /**
     * Add a message to the meeting.
     *
     * @param sender the sender
     * @param message the message text
     */
    void addMessage(String sender, String message);

    /**
     * Get chat history as string.
     *
     * @return formatted chat history
     */
    String getChatHistory();

    /**
     * Get limited chat history.
     *
     * @param maxMessages maximum messages to include
     * @return formatted chat history
     */
    String getChatHistory(int maxMessages);

    /**
     * Get total message count.
     *
     * @return number of messages
     */
    int getMessageCount();
}