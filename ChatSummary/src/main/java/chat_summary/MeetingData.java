package chat_summary;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MeetingData class - implements IMeetingData interface as per UML diagram
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
    public void addMessage(String sender, String message) {
        // Add sender to participants if not already present
        if (!participants.contains(sender)) {
            participants.add(sender);
        }

        // Create new MeetingMessage with current timestamp
        MeetingMessage meetingMessage = new MeetingMessage(sender, message, LocalDateTime.now());
        messages.add(meetingMessage);
    }

    @Override
    public String getChatHistory() {
        return messages.stream()
                .map(message -> message.getSender() + ": " + message.getText())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String getChatHistory(int maxMessages) {
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