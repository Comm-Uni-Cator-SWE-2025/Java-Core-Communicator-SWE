package com.swe.core.Analytics;

import java.util.List;

public interface ChatTelemetry {
    /**
     * Get all chats from the database.
     * @return List of all chats.
     */
    public List<ChatModel> getAllChats();

    /**
     * Get chats by user email.
     * @param email Email of the user.
     * @return List of chats by user.
     */
    public List<ChatModel> getChatsByUser(String email);
    
    /**
     * Get chats newer than a given timestamp.
     * @param timestamp Timestamp to filter by.
     * @return List of chats newer than the timestamp.
     */
    public List<ChatModel> getChatsNewerThan(Long timestamp);
}
