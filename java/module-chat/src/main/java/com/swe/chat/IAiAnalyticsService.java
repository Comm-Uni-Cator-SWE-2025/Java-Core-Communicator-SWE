package com.swe.chat;

/**
 * ABSTRACTION: Contract for managing chat history state and AI execution.
 * Isolates all stateful logic (history, scheduler) and AI interaction.
 */
public interface IAiAnalyticsService {

    /** Records a message into history for summarization/reply context. */
    void addMessageToHistory(ChatMessage message);

    /** Handles the logic for a user asking a question (e.g., "@AI what are the main points?"). */
    void handleAiQuestion(String fullText);
}