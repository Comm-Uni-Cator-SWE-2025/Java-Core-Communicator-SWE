package chatsummary;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Real implementation of meeting data storage.
 */
public class MeetingData implements IMeetingData {
    private List<MeetingMessage> messages;
    private List<String> participants;


    public MeetingData() {
        this.messages = new ArrayList<>();
        this.participants = new ArrayList<>();
    }

    @Override
    public List<MeetingMessage> getMessages() {
        return messages;
    }

    @Override
    public List<String> getParticipants() {
        return participants;
    }

    @Override
    public void addMessage(final String sender, final String message) {
        // Add sender to participants if new
        if (!participants.contains(sender)) {
            participants.add(sender);
        }

        final MeetingMessage meetingMessage = new MeetingMessage(sender, message, LocalDateTime.now());
        messages.add(meetingMessage);
    }

    @Override
    public String getChatHistory() {
        // Convert all messages to "Sender: Text" format
        return messages.stream()
                .map(message -> message.getSender() + ": " + message.getText())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String getChatHistory(final int maxMessages) {
        // Get only the last 'maxMessages' messages
        return messages.stream()
                .skip(Math.max(0, messages.size() - maxMessages))
                .map(message -> message.getSender() + ": " + message.getText())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public int getMessageCount() {
        return messages.size();
    }
}