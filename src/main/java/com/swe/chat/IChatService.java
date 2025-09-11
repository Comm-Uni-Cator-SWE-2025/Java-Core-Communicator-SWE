package com.swe.chat;

public interface IChatService {
    void sendMessage(ChatMessage message);

    void receiveMessage(String message);

    void deleteMessage(String messageId);
}
