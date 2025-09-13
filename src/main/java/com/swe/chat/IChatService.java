package com.swe.chat;

/**
 * Defines the contract for chat services including sending,
 * receiving, and deleting messages.
 */
public interface IChatService {

    /**
     * Sends a chat message.
     *
     * @param message the message to send
     */
    void sendMessage(ChatMessage message);

    /**
     * Receives a raw incoming message.
     *
     * @param message the incoming message text
     */
    void receiveMessage(String message);

    /**
     * Deletes a message by its unique ID.
     *
     * @param messageId the ID of the message to delete
     */
    void deleteMessage(String messageId);
}
