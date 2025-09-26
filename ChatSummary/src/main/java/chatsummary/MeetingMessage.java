package chatsummary;

import java.time.LocalDateTime;

/**
 * Represents one chat message with sender, text, and timestamp.
 */
public class MeetingMessage {
    private final String sender;
    private final String text;
    private final LocalDateTime timestamp;


    public MeetingMessage(final String senderName, final String messageText, final LocalDateTime messageTimestamp) {
        this.sender = senderName;
        this.text = messageText;
        this.timestamp = messageTimestamp;
    }


    public String getSender() {
        return sender;
    }


    public String getText() {
        return text;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return sender + " (" + timestamp + "): " + text;
    }
}