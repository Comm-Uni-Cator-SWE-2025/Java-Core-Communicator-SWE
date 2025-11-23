// module-chat/src/main/java/com/swe/chat/ChatProcessor.java

package com.swe.chat;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import com.swe.aiinsights.apiendpoints.AiClientService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IMPLEMENTATION: Contains all the core chat, file, and AI business logic.
 * Adheres to SRP by containing zero logic for RPC subscription/routing (the ChatManager's job).
 */
public class ChatProcessor implements IChatProcessor {

    private final AbstractRPC rpc;
    private final Networking network;
    private final AiClientService aiService;
    private final IChatFileHandler fileHandler;
    private final IChatFileCache fileCache;

    // AI and History State (Moved from ChatManager)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<ChatMessage> fullMessageHistory = Collections.synchronizedList(new ArrayList<>());
    private int lastSummarizedIndex = 0;

    // Map to hold sender names for reply lookup
    private final Map<String, String> messageIdToSender = new ConcurrentHashMap<>();

    public ChatProcessor(AbstractRPC rpc, Networking network, AiClientService aiService,
                         IChatFileHandler fileHandler, IChatFileCache fileCache) {
        this.rpc = rpc;
        this.network = network;
        this.aiService = aiService;
        this.fileHandler = fileHandler;
        this.fileCache = fileCache;

        startAiSummarizer();
    }

    // ============================================================================
    // AI LOGIC (Migrated from ChatManager)
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

    private String generateChatHistoryJson(List<ChatMessage> messages) {
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

    private void handleAiQuestion(String fullText) {
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


    // ============================================================================
    // IChatProcessor Implementation (The Core Logic)
    // ============================================================================

    @Override
    public byte[] processFrontendTextMessage(byte[] messageBytes) {
        // Text message processing logic migrated here
        try {
            ChatMessage message = ChatMessageSerializer.deserialize(messageBytes);
            fullMessageHistory.add(message);

            byte[] networkPacket = ChatProtocol.addProtocolFlag(messageBytes, ChatProtocol.FLAG_TEXT_MESSAGE);
            this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);

            String content = message.getContent().trim();
            if (content.startsWith("@AI")) {
                processIncrementalSummary();
                handleAiQuestion(content);
            }

            return new byte[0];
        } catch (Exception e) {
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] processFrontendFileMessage(byte[] messageBytes) {
        // File message (PATH mode) processing logic migrated here
        try {
            FileMessage pathModeMsg = FileMessageSerializer.deserialize(messageBytes);

            // DELEGATE I/O and Compression
            IChatFileHandler.FileResult result = fileHandler.processFileForSending(pathModeMsg.getFilePath());
            byte[] compressedData = result.compressedData();

            // 1. Cache the compressed file
            fileCache.put(pathModeMsg.getMessageId(), pathModeMsg.getFileName(), compressedData);

            // 2. Prepare Metadata for local UI
            FileMessage metadataMsg = new FileMessage(
                    pathModeMsg.getMessageId(), pathModeMsg.getUserId(), pathModeMsg.getSenderDisplayName(),
                    pathModeMsg.getCaption(), pathModeMsg.getFileName(), null, pathModeMsg.getReplyToMessageId());
            byte[] metadataBytes = FileMessageSerializer.serialize(metadataMsg);

            // 3. Prepare Content for network peers
            FileMessage contentModeMsg = new FileMessage(
                    pathModeMsg.getMessageId(), pathModeMsg.getUserId(), pathModeMsg.getSenderDisplayName(),
                    pathModeMsg.getCaption(), pathModeMsg.getFileName(), compressedData,
                    System.currentTimeMillis() / 1000, pathModeMsg.getReplyToMessageId());
            byte[] contentModeBytes = FileMessageSerializer.serialize(contentModeMsg);
            byte[] networkPacket = ChatProtocol.addProtocolFlag(contentModeBytes, ChatProtocol.FLAG_FILE_MESSAGE);

            this.rpc.call("chat:file-metadata-received", metadataBytes);
            this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);

            return new byte[0];

        } catch (Exception e) {
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] processFrontendSaveRequest(byte[] messageIdBytes) {
        // File save logic migrated here
        String messageId = new String(messageIdBytes, StandardCharsets.UTF_8).trim();

        try {
            IChatFileCache.FileCacheEntry cacheEntry = fileCache.get(messageId)
                    .orElseThrow(() -> new Exception("File not found in cache: " + messageId));

            // DELEGATE Decompression and File Write
            fileHandler.decompressAndSaveFile(messageId, cacheEntry.fileName(), cacheEntry.compressedData());

            String successMsg = "File saved successfully!";
            this.rpc.call("chat:file-saved-success", successMsg.getBytes(StandardCharsets.UTF_8));

            return successMsg.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            String errorMsg = "Failed to save file: " + e.getMessage();
            this.rpc.call("chat:file-saved-error", errorMsg.getBytes(StandardCharsets.UTF_8));
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] processFrontendDelete(byte[] messageIdBytes) {
        // Delete message logic migrated here
        String messageId = new String(messageIdBytes, StandardCharsets.UTF_8).trim();

        fileCache.remove(messageId);

        byte[] cleanIdBytes = messageId.getBytes(StandardCharsets.UTF_8);
        byte[] networkPacket = ChatProtocol.addProtocolFlag(cleanIdBytes, ChatProtocol.FLAG_DELETE_MESSAGE);
        this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);

        this.rpc.call("chat:message-deleted", cleanIdBytes);

        return new byte[0];
    }

    @Override
    public void processNetworkMessage(byte[] networkPacket) {
        // Network message handler logic migrated here
        if (networkPacket == null || networkPacket.length == 0) return;

        byte flag = networkPacket[0];
        byte[] messageBytes = Arrays.copyOfRange(networkPacket, 1, networkPacket.length);

        try {
            switch (flag) {
                case ChatProtocol.FLAG_TEXT_MESSAGE:
                    ChatMessage msg = ChatMessageSerializer.deserialize(messageBytes);
                    fullMessageHistory.add(msg);
                    this.rpc.call("chat:new-message", messageBytes);
                    break;

                case ChatProtocol.FLAG_FILE_MESSAGE:
                    FileMessage fileMsg = FileMessageSerializer.deserialize(messageBytes);
                    fileCache.put(fileMsg.getMessageId(), fileMsg.getFileName(), fileMsg.getFileContent());

                    FileMessage metadataMsg = new FileMessage(
                            fileMsg.getMessageId(), fileMsg.getUserId(), fileMsg.getSenderDisplayName(),
                            fileMsg.getCaption(), fileMsg.getFileName(), null, fileMsg.getReplyToMessageId());
                    byte[] metadataBytes = FileMessageSerializer.serialize(metadataMsg);
                    this.rpc.call("chat:file-metadata-received", metadataBytes);
                    break;

                case ChatProtocol.FLAG_DELETE_MESSAGE:
                    String remoteId = new String(messageBytes, StandardCharsets.UTF_8).trim();
                    fileCache.remove(remoteId);
                    this.rpc.call("chat:message-deleted", remoteId.getBytes(StandardCharsets.UTF_8));
                    break;

                default:
                    System.err.println("[Core] Unknown network message type: " + flag);
            }
        } catch (Exception e) {
            System.err.println("[Core] Error handling network message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}