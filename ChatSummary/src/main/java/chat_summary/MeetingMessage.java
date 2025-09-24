package chat_summary;


import java.time.LocalDateTime;

/**
 * Represents a single message in a meeting
 */
public class MeetingMessage {
    private String sender;
    private String text;
    private LocalDateTime timestamp;

    public MeetingMessage(String sender, String text, LocalDateTime timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSender() { return sender; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return sender + " (" + timestamp + "): " + text;
    }
}