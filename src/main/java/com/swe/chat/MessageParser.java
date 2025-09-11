package com.swe.chat;

import com.google.gson.Gson;

public class MessageParser {
    private static final Gson gson = new Gson();

    // Convert ChatMessage -> String
    public static String serialize(ChatMessage message) {
        return gson.toJson(message);
    }

    // Convert String -> ChatMessage
    public static ChatMessage deserialize(String json_data) {
        return gson.fromJson(json_data,ChatMessage.class);
    }
}
