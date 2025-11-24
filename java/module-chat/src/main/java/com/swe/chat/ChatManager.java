package com.swe.chat;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.Context;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.core.logging.SweLogger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*; // Fixes List, ArrayList, Collections, Iterator
import java.util.concurrent.*;
import java.util.zip.Deflater;

/**
 * ============================================================================
 * BACKEND - ChatManager
 * ============================================================================
 *
 * Responsibilities:
 *   1. Receive PATH-mode file messages from frontend
 *   2. Read and compress files from disk
 *   3. Cache compressed data in memory
 *   4. Send to remote peers via network
 *   5. Decompress and save files when user clicks "Save"
 *
 * The frontend NEVER sees compressed file data.
 */
public class ChatManager implements IChatService {

    // --- 1. NEW: Restore the Helper Class ---
    private static class FileCacheEntry {
        public final String fileName;
        public final byte[] compressedData;

        public FileCacheEntry(String fileName, byte[] compressedData) {
            this.fileName = fileName;
            this.compressedData = compressedData;
        }
    }



    private static final byte FLAG_TEXT_MESSAGE = (byte) 0x01;
    private static final byte FLAG_FILE_MESSAGE = (byte) 0x02;
    private static final byte FLAG_FILE_METADATA = (byte) 0x03;
    private static final byte FLAG_DELETE_MESSAGE = (byte) 0x04;

    private final AbstractRPC rpc;
    private final Networking network;
    private final SweLogger logger;

    private final AiClientService aiService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final AiClientService aiService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * FILE CACHE - Stores compressed files temporarily
     * Key: messageId
     * Value: compressed file bytes
     */
    private final Map<String, FileCacheEntry> fileCache = new ConcurrentHashMap<>();

    public final static List<ChatMessage> fullMessageHistory = Collections.synchronizedList(new ArrayList<>());
    private int lastSummarizedIndex = 0;

    public ChatManager(Networking network, SweLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
        Context context = Context.getInstance();
        this.rpc = context.getRpc();
        this.network = network;

        // 1. Initialize AI Service
        this.aiService = AiInstance.getInstance();

        // 2. Start Periodic Summarization (Every 20 seconds)
        startAiSummarizer();

        // Subscribe to frontend RPC calls
        this.rpc.subscribe("chat:send-text", this::handleFrontendTextMessage);
        this.rpc.subscribe("chat:send-file", this::handleFrontendFileMessage);
        this.rpc.subscribe("chat:delete-message", this::handleDeleteMessage);

        // ⭐ NEW: Handle save-to-disk requests from frontend
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

            logger.info("Sending " + newMessages.size() + " new messages for summarization");

            aiService.summariseText(historyJson)
                    .thenAccept(summary -> logger.info("Incremental summary: " + summary))
                    .exceptionally(e -> {
                        logger.error("Incremental summarization failed", e);
                        return null;
                    });

        } catch (Exception e) {
            logger.error("Failed to process incremental summary", e);
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

    /**
     * ============================================================================
     * HANDLER 1: Text Message from Frontend
     * ============================================================================
     */
    private byte[] handleFrontendTextMessage(byte[] messageBytes) {
        logger.info("Received text message from frontend");

        try {
            // 1. Deserialize to inspect content
            ChatMessage message = ChatMessageSerializer.deserialize(messageBytes);

            logger.debug("Received text: " + message.getContent());

            // 2. Add to Backend History (for summarization)
            fullMessageHistory.add(message);

            byte[] networkPacket = addProtocolFlag(messageBytes, FLAG_TEXT_MESSAGE);
            // ClientNode[] dests = { new ClientNode("127.0.0.1", 1234) };
            this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);

            // 3. Check for @AI
            String content = message.getContent().trim();
            if (content.startsWith("@AI")) {
                // Force an update of context BEFORE asking the question
                // This ensures the AI knows about the messages leading up to this question
                processIncrementalSummary();

                handleAiQuestion(content);
            }

            return new byte[0];  // Empty array with brackets
        } catch (Exception e) {
            logger.error("Error sending text message", e);
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Handles the AI Question logic.
     */
    private void handleAiQuestion(String fullText) {
        String question = fullText.substring(3).trim(); // Remove "@AI"
        if (question.isEmpty()) return;

        logger.info("Processing AI question: " + question);

        aiService.answerQuestion(question)
                .thenAccept(answer -> {
                    broadcastAiResponse(answer);
                })
                .exceptionally(e -> {
                    broadcastAiResponse("I'm sorry, I couldn't process that request.");
                    return null;
                });
    }

    private void broadcastAiResponse(String answer) {
        try {
            String aiMsgId = UUID.randomUUID().toString();
            ChatMessage aiMsg = new ChatMessage(
                    aiMsgId, "AI-SYSTEM-ID", "AI_Bot", answer, null
            );

            // Add AI response to history too
            fullMessageHistory.add(aiMsg);

            byte[] aiBytes = ChatMessageSerializer.serialize(aiMsg);
            byte[] networkPacket = addProtocolFlag(aiBytes, FLAG_TEXT_MESSAGE);
            this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);
            this.rpc.call("chat:new-message", aiBytes); // Update local UI

        } catch (Exception e) {
            logger.error("Failed to broadcast AI response", e);
        }
    }

    /**
     * ============================================================================
     * HANDLER 2: File Message from Frontend (PATH MODE)
     * ============================================================================
     *
     * Frontend sends FileMessage with PATH (no file bytes)
     * Backend will:
     *   1. Read file from disk
     *   2. Compress it
     *   3. Cache compressed data
     *   4. Send metadata to frontend
     *   5. Send compressed data to network
     */
    private byte[] handleFrontendFileMessage(byte[] messageBytes) {
        logger.info("Received file message from frontend (PATH mode)");

        try {
            // 1. Deserialize PATH mode message
            FileMessage pathModeMsg = FileMessageSerializer.deserialize(messageBytes);
            String filePath = pathModeMsg.getFilePath();

            if (filePath == null || filePath.isEmpty()) {
                throw new IllegalArgumentException("File path is null or empty");
            }

            // Sanitize path
            filePath = filePath.trim();
            if (filePath.startsWith("*")) {
                filePath = filePath.substring(1).trim();
            }

            logger.debug("Reading file: " + filePath);

            // Check if file exists
            if (!Files.exists(Paths.get(filePath))) {
                throw new IllegalArgumentException("File does not exist: " + filePath);
            }

            // 2. Read and compress
            byte[] uncompressedData = Files.readAllBytes(Paths.get(filePath));
            logger.debug("File size: " + uncompressedData.length + " bytes");

            byte[] compressedData = Utilities.Compress(uncompressedData, Deflater.BEST_SPEED);
            if (compressedData == null) {
                throw new IOException("Failed to compress file");
            }

            logger.debug("Compressed payload from " + uncompressedData.length + " to " + compressedData.length + " bytes");

            // ===== STEP 1: Send METADATA-ONLY to frontend =====
            FileMessage metadataMsg = new FileMessage(
                    pathModeMsg.getMessageId(),
                    pathModeMsg.getUserId(),
                    pathModeMsg.getSenderDisplayName(),
                    pathModeMsg.getCaption(),
                    pathModeMsg.getFileName(),
                    null,  // NO file path
                    pathModeMsg.getReplyToMessageId()
            );
            byte[] metadataBytes = FileMessageSerializer.serialize(metadataMsg);
            this.rpc.call("chat:file-metadata-received", metadataBytes);


            logger.debug("Sent metadata to frontend");

            // ===== STEP 2: Cache the compressed file =====
            fileCache.put(pathModeMsg.getMessageId(), new FileCacheEntry(pathModeMsg.getFileName(), compressedData));
            logger.debug("Cached compressed file: " + pathModeMsg.getMessageId());

            // ===== STEP 3: Send to remote peers (with compressed data) =====
            FileMessage contentModeMsg = new FileMessage(
                    pathModeMsg.getMessageId(),
                    pathModeMsg.getUserId(),
                    pathModeMsg.getSenderDisplayName(),
                    pathModeMsg.getCaption(),
                    pathModeMsg.getFileName(),
                    compressedData,  // ⭐ Compressed bytes for network
                    System.currentTimeMillis() / 1000,
                    pathModeMsg.getReplyToMessageId()
            );

            byte[] contentModeBytes = FileMessageSerializer.serialize(contentModeMsg);
            byte[] networkPacket = addProtocolFlag(contentModeBytes, FLAG_FILE_MESSAGE);

            // ClientNode[] dests = { new ClientNode("127.0.0.1", 1234) };
            this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);

            logger.info("Sent file to network");

            return new byte[0];

        } catch (Exception e) {
            logger.error("Error processing file message", e);
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * ============================================================================
     * HANDLER 3: Save File to Disk Request from Frontend
     * ============================================================================
     */
    private byte[] handleSaveFileToDisk(byte[] messageIdBytes) {
        String messageId = new String(messageIdBytes, StandardCharsets.UTF_8);
        logger.info("User requested save for message " + messageId);

        try {
            // Retrieve compressed file from cache
            FileCacheEntry cacheEntry = fileCache.get(messageId);
            if (cacheEntry == null) {
                throw new Exception("File not found in cache: " + messageId);
            }

            logger.debug("Retrieved compressed file (" + cacheEntry.compressedData.length + " bytes)");

            byte[] decompressedData = Utilities.Decompress(cacheEntry.compressedData);
            if (decompressedData == null) {
                throw new Exception("Failed to decompress file");
            }


            // Save to Downloads folder
            // 1. Get User's Home Directory
            String homeDir = System.getProperty("user.home");

            // 2. Construct path using Paths.get (OS-agnostic)
            java.nio.file.Path downloadsDir = Paths.get(homeDir, "Downloads");

            if (!Files.exists(downloadsDir)) {
                Files.createDirectories(downloadsDir);
            }

            // 3. Use the ORIGINAL filename from the cache entry
            // Do NOT append "_file". Use the original name (e.g., "fees.pdf")
            // If you must add an ID to avoid conflicts, put it BEFORE the extension.
            String originalName = cacheEntry.fileName;
            String finalName = originalName;
            java.nio.file.Path savePath = downloadsDir.resolve(finalName);
            int counter = 1;
            while (Files.exists(savePath)) {
                // Split name and extension
                String namePart = originalName;
                String extPart = "";
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex > 0) {
                    namePart = originalName.substring(0, dotIndex);
                    extPart = originalName.substring(dotIndex); // includes dot
                }
                finalName = namePart + " (" + counter + ")" + extPart;
                savePath = downloadsDir.resolve(finalName);
                counter++;
            }

            // 4. Save file
            Files.write(savePath, decompressedData);
            logger.info("Saved file to " + savePath);

            // Notify frontend of success
            String successMsg = "File saved to: " + savePath;
            this.rpc.call("chat:file-saved-success", successMsg.getBytes(StandardCharsets.UTF_8));

            return successMsg.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            logger.error("Error saving file", e);

            String errorMsg = "Failed to save file: " + e.getMessage();
            this.rpc.call("chat:file-saved-error", errorMsg.getBytes(StandardCharsets.UTF_8));

            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * ============================================================================
     * HANDLER 4: Delete Message
     * ============================================================================
     */
    private byte[] handleDeleteMessage(byte[] messageIdBytes) {
        // 1. Sanitize ID (Crucial for the newline issue we discussed)
        String messageId = new String(messageIdBytes, StandardCharsets.UTF_8).trim();

        logger.info("Deleting message " + messageId);

        // 2. Remove from local file cache if exists
        fileCache.remove(messageId);

        // 3. BROADCAST TO NETWORK (This was missing!)
        // We wrap the ID bytes with the DELETE flag and send it to peers.
        try {
            // Ensure we send the clean, trimmed bytes
            byte[] cleanIdBytes = messageId.getBytes(StandardCharsets.UTF_8);

            byte[] networkPacket = addProtocolFlag(cleanIdBytes, FLAG_DELETE_MESSAGE);
            this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);

            logger.debug("Broadcasted delete signal to network");
        } catch (Exception e) {
            logger.error("Failed to broadcast delete", e);
        }

        return new byte[0];
    }

    /**
     * ============================================================================
     * HANDLER 5: Network Message (from Remote Peer)
     * ============================================================================
     */
    private void handleNetworkMessage(byte[] networkPacket) {
        if (networkPacket == null || networkPacket.length == 0) return;

        byte flag = networkPacket[0];
        byte[] messageBytes = Arrays.copyOfRange(networkPacket, 1, networkPacket.length);

        try {
            switch (flag) {
                case FLAG_TEXT_MESSAGE:
                    logger.debug("Received text from network");
                    ChatMessage msg = ChatMessageSerializer.deserialize(messageBytes);
                    fullMessageHistory.add(msg);
                    this.rpc.call("chat:new-message", messageBytes);
                    break;

                case FLAG_FILE_MESSAGE:
                    logger.debug("Received file from network");
                    FileMessage fileMsg = FileMessageSerializer.deserialize(messageBytes);

                    // Cache the received compressed file
                    fileCache.put(fileMsg.getMessageId(), new FileCacheEntry(fileMsg.getFileName(), fileMsg.getFileContent()));

                    // Send ONLY metadata to frontend
                    FileMessage metadataMsg = new FileMessage(
                            fileMsg.getMessageId(),
                            fileMsg.getUserId(),
                            fileMsg.getSenderDisplayName(),
                            fileMsg.getCaption(),
                            fileMsg.getFileName(),
                            null,  // NO file data
                            fileMsg.getReplyToMessageId()
                    );
                    byte[] metadataBytes = FileMessageSerializer.serialize(metadataMsg);
                    this.rpc.call("chat:file-metadata-received", metadataBytes);
                    break;

                case FLAG_DELETE_MESSAGE:
                    logger.debug("Received delete signal from network");

                    // 1. Clean the ID
                    String remoteId = new String(messageBytes, StandardCharsets.UTF_8).trim();

                    // 2. Remove from our cache just in case
                    fileCache.remove(remoteId);

                    // 3. Tell the Frontend to update the UI
                    // This triggers 'handleBackendDelete' in ChatViewModel
                    this.rpc.call("chat:message-deleted", remoteId.getBytes(StandardCharsets.UTF_8));
                    break;

                default:
                    logger.warn("Unknown message type: " + flag);
            }
        } catch (Exception e) {
            logger.error("Error handling network message", e);
        }
    }

    /**
     * ============================================================================
     * UTILITY METHODS
     * ============================================================================
     */
    private byte[] addProtocolFlag(byte[] data, byte flag) {
        byte[] flaggedPacket = new byte[data.length + 1];
        flaggedPacket[0] = flag;
        System.arraycopy(data, 0, flaggedPacket, 1, data.length);
        return flaggedPacket;
    }

    @Override
    public void sendMessage(ChatMessage message) {
        // Implemented via RPC
    }

    @Override
    public void receiveMessage(String json) {
        // Implemented via RPC
    }

    @Override
    public void deleteMessage(String messageId) {
        byte[] messageIdBytes = messageId.getBytes(StandardCharsets.UTF_8);
        this.rpc.call("chat:message-deleted", messageIdBytes);
    }
}
