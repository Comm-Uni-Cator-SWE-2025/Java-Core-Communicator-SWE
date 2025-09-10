package com.Comm_Uni_Cator.chat;

public interface IChatService {
    void sendMessage(ChatMessage message);

    void receiveMessage(String message);

    void deleteMessage(String messageId);
}
