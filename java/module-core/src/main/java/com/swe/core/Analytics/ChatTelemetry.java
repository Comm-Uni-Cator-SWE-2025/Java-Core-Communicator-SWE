/**
 *  Contributed by Kishore.
 */

package com.swe.core.Analytics;

import java.util.List;

/**
* ChatTelemetry interface
*/
public interface ChatTelemetry {
    /**
     * Get all chats from the database.
     * @return List of all chats.
     */
    List<ChatModel> getAllChats();

    /**
     * Get chats by user email.
     * 
     * @param email Email of the user.
     * @return List of chats by user.
     */
    List<ChatModel> getChatsByUser(String email);

    /**
     * Get chats newer than a given timestamp.
     * 
     * @param timestamp Timestamp to filter by.
     * @return List of chats newer than the timestamp.
     */
    List<ChatModel> getChatsNewerThan(Long timestamp);
}
