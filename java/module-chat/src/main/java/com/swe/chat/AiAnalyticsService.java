package com.swe.chat;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import com.swe.aiinsights.apiendpoints.AiClientService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * IMPLEMENTATION: Manages chat history, schedules summarization, and handles AI question logic.
 * Adheres to SRP by containing all state and scheduling related to chat history and AI.
 */
public class AiAnalyticsService implements IAiAnalyticsService {

    private final AbstractRPC rpc;
    private final Networking network;
    private final AiClientService aiService;

    // AI and History State (Migrated from old ChatManager)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<ChatMessage> fullMessageHistory = Collections.synchronizedList(new ArrayList<>());
    private int lastSummarizedIndex = 0;

    // Map to hold sender names for reply lookup
    private final Map<String, String> messageIdToSender = new ConcurrentHashMap<>();

    public AiAnalyticsService(AbstractRPC rpc, Networking network, AiClientService aiService) {
        this.rpc = rpc;
        this.network = network;
        this.aiService = aiService;

        startAiSummarizer();
    }

    // ============================================================================
    // SERVICE LIFECYCLE
    // ============================================================================

    private void startAiSummarizer() {
        // AI Schedule logic migrated here
        scheduler.scheduleAtFixedRate(this::processIncrementalSummary, 10, 10, TimeUnit.MINUTES);
    }

    private void processIncrementalSummary() {
        List<ChatMessage> newMessages = new ArrayList<>();

        synchronized (fullMessageHistory) {
            if (lastSummarizedIndex >= fullMessageHistory.size()) {
                return;
            }
            newMessages.addAll(fullMessageHistory.subList(lastSummarizedIndex, fullMessageHistory.size()));
            lastSummarizedIndex = fullMessageHistory.size();

            // Update reply lookup map for ALL history messages
            for (ChatMessage m : fullMessageHistory) {
                messageIdToSender.put(m.getMessageId(), m.getSenderDisplayName());
            }
        }

        if (newMessages.isEmpty()) return;

        try {
            String historyJson = generateChatHistoryJson(newMessages);

            System.out.println("[AI-Backend] Sending " + newMessages.size() + " new messages for summarization...");

            aiService.summariseText(historyJson)
                    .thenAccept(summary -> {
                        System.out.println("[AI-Backend] Incremental Summary: " + summary);
                    })
                    .exceptionally(e -> {
                        System.err.println("[AI-Backend] Summarization failed: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generateChatHistoryJson(List<ChatMessage> messages) {
        // JSON Generation logic migrated here
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"messages\": [\n");

        Iterator<ChatMessage> it = messages.iterator();
        while (it.hasNext()) {
            ChatMessage msg = it.next();

            String fromUser = escapeJson(msg.getSenderDisplayName());
            String toUser = "ALL";
            String replyId = msg.getReplyToMessageId();

            if (replyId != null && !replyId.isEmpty()) {
                String originalSender = messageIdToSender.get(replyId);
                if (originalSender != null) {
                    toUser = escapeJson(originalSender);
                }
            }

            String time = msg.getTimestamp().toString();

            json.append("    {\n");
            json.append("      \"from\": \"").append(fromUser).append("\",\n");
            json.append("      \"to\": \"").append(toUser).append("\",\n");
            json.append("      \"timestamp\": \"").append(time).append("\",\n");
            json.append("      \"message\": \"").append(escapeJson(msg.getContent())).append("\"\n");
            json.append("    }");

            if (it.hasNext()) json.append(",\n");
        }

        json.append("\n  ]\n}");
        return json.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", " ");
    }


    // ============================================================================
    // PUBLIC INTERFACE (IAiAnalyticsService)
    // ============================================================================

    @Override
    public List<ChatMessage> getFullMessageHistory() {
        synchronized (fullMessageHistory) {
            return new ArrayList<>(fullMessageHistory);
        }
    }

    @Override
    public void addMessageToHistory(ChatMessage message) {
        // Logic for adding message to history and updating context
        fullMessageHistory.add(message);
        messageIdToSender.put(message.getMessageId(), message.getSenderDisplayName());

        // Check if message should trigger an immediate AI response
        if (message.getContent().trim().startsWith("@AI")) {
            // Trigger incremental summary for context BEFORE handling the question
            processIncrementalSummary();
            handleAiQuestion(message.getContent());
        }
    }

    @Override
    public void handleAiQuestion(String fullText) {
        String question = fullText.substring(3).trim();
        if (question.isEmpty()) return;

        System.out.println("[Core] Processing AI Question: " + question);

        aiService.answerQuestion(question)
                .thenAccept(this::broadcastAiResponse)
                .exceptionally(e -> {
                    broadcastAiResponse("I'm sorry, I couldn't process that request.");
                    return null;
                });
    }

    private void broadcastAiResponse(String answer) {
        // AI response broadcast logic migrated here
        try {
            String aiMsgId = UUID.randomUUID().toString();
            ChatMessage aiMsg = new ChatMessage(aiMsgId, "AI-SYSTEM-ID", "AI_Bot", answer, null);

            // Add AI response to history too (for context)
            fullMessageHistory.add(aiMsg);

            byte[] aiBytes = ChatMessageSerializer.serialize(aiMsg);
            byte[] networkPacket = ChatProtocol.addProtocolFlag(aiBytes, ChatProtocol.FLAG_TEXT_MESSAGE);

            // Broadcast to peers AND update local UI via RPC
            this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);
            this.rpc.call("chat:new-message", aiBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}