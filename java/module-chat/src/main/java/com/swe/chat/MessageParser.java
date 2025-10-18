package com.swe.chat;

import com.google.gson.Gson;


/**
 * Utility class for serializing and deserializing {@link ChatMessage}
 * objects to and from JSON.
 */
public class MessageParser {

    /** Shared Gson instance for JSON conversion. */
    private static final Gson GSON = new Gson();

    /**
     * Converts a {@link ChatMessage} object into its JSON string form.
     *
     * @param message the message to serialize
     * @return the JSON representation of the message
     */
    public static String serialize(final ChatMessage message) {
        return GSON.toJson(message);
    }

    /**
     * Converts a JSON string into a {@link ChatMessage} object.
     *
     * @param jsondata the JSON string to deserialize
     * @return the deserialized {@link ChatMessage} object
     */
    public static ChatMessage deserialize(final String jsondata) {
        return GSON.fromJson(jsondata, ChatMessage.class);
    }
}
