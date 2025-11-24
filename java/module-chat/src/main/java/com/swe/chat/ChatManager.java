package com.swe.chat;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.Context;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.aiinsights.apiendpoints.AiClientService;

import java.nio.charset.StandardCharsets;

/**
 * ============================================================================
 * BACKEND - ChatManager (Final Router/Adapter)
 * ============================================================================
 *
 * Responsibility (SRP): Route incoming events to the appropriate processor
 * and act as the IChatService Adapter. ZERO business logic or I/O/Caching.
 * All core tasks are delegated via DIP.
 */
public class ChatManager implements IChatService { // Adapter for IChatService

    private final AbstractRPC rpc;
    private final IChatProcessor processor; // DIP: Message logic delegated

    /**
     * CONSTRUCTOR: Injects all necessary services and wires events.
     * @param network The networking service.
     */
    private final Map<String, FileCacheEntry> fileCache = new ConcurrentHashMap<>();

    public final static List<ChatMessage> fullMessageHistory = Collections.synchronizedList(new ArrayList<>());
    private int lastSummarizedIndex = 0;

    public ChatManager(Networking network) {
        Context context = Context.getInstance();
        this.rpc = context.rpc;

        // 1. Initialize Delegated Services
        AiClientService aiService = AiInstance.getInstance();
        IChatFileHandler fileHandler = new LocalFileHandler();
        IChatFileCache fileCache = new InMemoryFileCache();

        // 2. Initialize the History/AI Service (Stateful Layer - SRP)
        IAiAnalyticsService aiAnalyticsService = new AiAnalyticsService(this.rpc, network, aiService);

        // 3. Initialize the Core Processor (Execution Layer - SRP)
        this.processor = new ChatProcessor(this.rpc, network, fileHandler, fileCache, aiAnalyticsService);

        // 4. Subscribe & Route (Router's job: Wiring RPC/Network events to the Processor)
        this.rpc.subscribe("chat:send-text", this::handleFrontendTextMessage);
        this.rpc.subscribe("chat:send-file", this::handleFrontendFileMessage);
        this.rpc.subscribe("chat:delete-message", this::handleDeleteMessage);
        this.rpc.subscribe("chat:save-file-to-disk", this::handleSaveFileToDisk);

        // Subscribe to network messages
        this.network.subscribe(ModuleType.CHAT.ordinal(), this::handleNetworkMessage);
    }

    /**
     * ⭐ AI FEATURE: Periodic Summarization
     * Runs every 10 minutes.
     */
    private void startAiSummarizer() {
        scheduler.scheduleAtFixedRate(() -> {
            processIncrementalSummary();
        }, 10, 10, TimeUnit.MINUTES); // 10 Minutes delay, 10 Minutes period
    }

    /**
     * Helper to process only NEW messages since last run.
     */
    private void processIncrementalSummary() {
        List<ChatMessage> newMessages = new ArrayList<>();

        synchronized (fullMessageHistory) {
            if (lastSummarizedIndex >= fullMessageHistory.size()) {
                return; // No new messages
            }
            // Sublist from last index to current end
            newMessages.addAll(fullMessageHistory.subList(lastSummarizedIndex, fullMessageHistory.size()));
            // Update index so we don't summarize these again next time
            lastSummarizedIndex = fullMessageHistory.size();
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

    public static List<ChatMessage> getFullMessageHistory() {
        return fullMessageHistory;
    }
    /**
     * ⭐ AI FEATURE: JSON Generator
     * Generates JSON with correct 'from' and 'to' usernames.
     */
    public static String generateChatHistoryJson(List<ChatMessage> messages) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"messages\": [\n");

        // 1. Create a quick lookup map to find Reply Targets
        // Map<MessageID, SenderName>
        Map<String, String> messageIdToSender = new HashMap<>();
        // We might need to look in full history for replies, not just the new batch
        synchronized (fullMessageHistory) {
            for (ChatMessage m : fullMessageHistory) {
                messageIdToSender.put(m.getMessageId(), m.getSenderDisplayName());
            }
        }

        Iterator<ChatMessage> it = messages.iterator();
        while (it.hasNext()) {
            ChatMessage msg = it.next();

            // A. Resolve "FROM"
            // Backend stores raw names (e.g. "Alice"), so this is safe. No "You" here.
            String fromUser = escapeJson(msg.getSenderDisplayName());

            // B. Resolve "TO"
            String toUser = "ALL"; // Default broadcast
            String replyId = msg.getReplyToMessageId();

            if (replyId != null && !replyId.isEmpty()) {
                // Look up the name of the person who sent the original message
                String originalSender = messageIdToSender.get(replyId);
                if (originalSender != null) {
                    toUser = escapeJson(originalSender);
                }
            }

            // C. Format Timestamp (ISO 8601 preferred by AI)
            String time = msg.getTimestamp().toString(); // e.g., 2025-11-07T10:00:00

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

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", " ");
    }

    private byte[] handleFrontendTextMessage(byte[] messageBytes) {
        try {
            return processor.processFrontendTextMessage(messageBytes);
        } catch (Exception e) {
            System.err.println("[Core.Router] Fatal error processing text: " + e.getMessage());
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] handleFrontendFileMessage(byte[] messageBytes) {
        try {
            return processor.processFrontendFileMessage(messageBytes);
        } catch (Exception e) {
            System.err.println("[Core.Router] Fatal error processing file message: " + e.getMessage());
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] handleSaveFileToDisk(byte[] messageIdBytes) {
        try {
            return processor.processFrontendSaveRequest(messageIdBytes);
        } catch (Exception e) {
            System.err.println("[Core.Router] Fatal error processing save request: " + e.getMessage());
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] handleDeleteMessage(byte[] messageIdBytes) {
        try {
            return processor.processFrontendDelete(messageIdBytes);
        } catch (Exception e) {
            System.err.println("[Core.Router] Fatal error processing delete: " + e.getMessage());
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    // ============================================================================
    // NETWORK HANDLER (Simple Delegation)
    // ============================================================================

    private void handleNetworkMessage(byte[] networkPacket) {
        processor.processNetworkMessage(networkPacket);
    }

    // ============================================================================
    // IChatService Implementation (Adapter Pattern)
    // ============================================================================

    @Override
    public void sendMessage(ChatMessage message) {
        // Adapter: Ensures external API is satisfied by wrapping and calling RPC
        try {
            byte[] messageBytes = ChatMessageSerializer.serialize(message);
            this.rpc.call("chat:send-text", messageBytes);
        } catch (Exception e) {
            System.err.println("[ChatService] Failed to send message via RPC: " + e.getMessage());
        }
    }

    @Override
    public void receiveMessage(String json) {
        // Adapter: Ensures external API is satisfied by wrapping and calling RPC
        try {
            this.rpc.call("chat:new-message", json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("[ChatService] Failed to inject message via RPC: " + e.getMessage());
        }
    }

    @Override
    public void deleteMessage(String messageId) {
        // Adapter: Ensures external API is satisfied by wrapping and calling RPC
        try {
            byte[] messageIdBytes = messageId.getBytes(StandardCharsets.UTF_8);
            this.rpc.call("chat:delete-message", messageIdBytes);
        } catch (Exception e) {
            System.err.println("[ChatService] Failed to send delete via RPC: " + e.getMessage());
        }
    }
}