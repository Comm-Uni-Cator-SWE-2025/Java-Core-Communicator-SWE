package com.swe.chat;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * IMPLEMENTATION: Contains the synchronous message execution flow.
 * Adheres to SRP by delegating all I/O, caching, and AI tasks to specialized services.
 */
public class ChatProcessor implements IChatProcessor {

    private final AbstractRPC rpc;
    private final Networking network;
    private final IChatFileHandler fileHandler;
    private final IChatFileCache fileCache;
    private final IAiAnalyticsService aiService;

    public ChatProcessor(AbstractRPC rpc, Networking network,
                         IChatFileHandler fileHandler, IChatFileCache fileCache,
                         IAiAnalyticsService aiService) {
        this.rpc = rpc;
        this.network = network;
        this.fileHandler = fileHandler;
        this.fileCache = fileCache;
        this.aiService = aiService;
    }

    // ============================================================================
    // IChatProcessor Implementation (The Core Logic)
    // ============================================================================

    @Override
    public byte[] processFrontendTextMessage(byte[] messageBytes) {
        try {
            ChatMessage message = ChatMessageSerializer.deserialize(messageBytes);

            // 1. DELEGATE History (AI Trigger is now handled in ChatManager)
            aiService.addMessageToHistory(message);

            // 2. Execution: Broadcast
            byte[] networkPacket = ChatProtocol.addProtocolFlag(messageBytes, ChatProtocol.FLAG_TEXT_MESSAGE);
            this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);

            return new byte[0];
        } catch (Exception e) {
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] processFrontendFileMessage(byte[] messageBytes) {
        try {
            FileMessage pathModeMsg = FileMessageSerializer.deserialize(messageBytes);

            // 1. DELEGATE I/O and Compression (DIP: FileHandler)
            IChatFileHandler.FileResult result = fileHandler.processFileForSending(pathModeMsg.getFilePath());
            byte[] compressedData = result.compressedData();

            // 2. Cache the compressed file (DIP: FileCache - Writes to Disk internally)
            fileCache.put(pathModeMsg.getMessageId(), pathModeMsg.getFileName(), compressedData);

            // 3. Prepare Metadata for local UI (Coordinator/Adapter)
            FileMessage metadataMsg = new FileMessage(
                    pathModeMsg.getMessageId(), pathModeMsg.getUserId(), pathModeMsg.getSenderDisplayName(),
                    pathModeMsg.getCaption(), pathModeMsg.getFileName(), null, pathModeMsg.getReplyToMessageId());
            byte[] metadataBytes = FileMessageSerializer.serialize(metadataMsg);

            // 4. Prepare Content for network peers
            // Note: We still use the byte[] here to send over network, which is unavoidable.
            // But the local storage is safely on disk now.
            FileMessage contentModeMsg = new FileMessage(
                    pathModeMsg.getMessageId(), pathModeMsg.getUserId(), pathModeMsg.getSenderDisplayName(),
                    pathModeMsg.getCaption(), pathModeMsg.getFileName(), compressedData,
                    System.currentTimeMillis() / 1000, pathModeMsg.getReplyToMessageId());
            byte[] contentModeBytes = FileMessageSerializer.serialize(contentModeMsg);
            byte[] networkPacket = ChatProtocol.addProtocolFlag(contentModeBytes, ChatProtocol.FLAG_FILE_MESSAGE);

            // 5. Route Messages
            this.rpc.call("chat:file-metadata-received", metadataBytes);
            this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);

            return new byte[0];

        } catch (Exception e) {
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] processFrontendSaveRequest(byte[] messageIdBytes) {
        String messageId = new String(messageIdBytes, StandardCharsets.UTF_8).trim();

        try {
            // 1. Retrieve from injected cache
            // UPDATED: Now returns a cacheEntry containing a Path, not bytes
            IChatFileCache.FileCacheEntry cacheEntry = fileCache.get(messageId)
                    .orElseThrow(() -> new Exception("File not found in cache (expired or missing): " + messageId));

            // 2. DELEGATE Decompression and File Write
            // UPDATED: Passes the Path (tempFilePath) instead of compressedData()
            fileHandler.decompressAndSaveFile(messageId, cacheEntry.fileName(), cacheEntry.tempFilePath());

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
        String messageId = new String(messageIdBytes, StandardCharsets.UTF_8).trim();

        // 1. Clean up: Remove from local file cache (also deletes temp file on disk)
        fileCache.remove(messageId);

        // 2. Coordinate: Broadcast to network
        byte[] cleanIdBytes = messageId.getBytes(StandardCharsets.UTF_8);
        byte[] networkPacket = ChatProtocol.addProtocolFlag(cleanIdBytes, ChatProtocol.FLAG_DELETE_MESSAGE);
        this.network.broadcast(networkPacket, ModuleType.CHAT.ordinal(), 0);

        // 3. Coordinate: Update local UI
        this.rpc.call("chat:message-deleted", cleanIdBytes);

        return new byte[0];
    }

    @Override
    public void processNetworkMessage(byte[] networkPacket) {
        if (networkPacket == null || networkPacket.length == 0) return;

        byte flag = networkPacket[0];
        byte[] messageBytes = Arrays.copyOfRange(networkPacket, 1, networkPacket.length);

        try {
            switch (flag) {
                case ChatProtocol.FLAG_TEXT_MESSAGE:
                    ChatMessage msg = ChatMessageSerializer.deserialize(messageBytes);
                    aiService.addMessageToHistory(msg);
                    this.rpc.call("chat:new-message", messageBytes);
                    break;

                case ChatProtocol.FLAG_FILE_MESSAGE:
                    FileMessage fileMsg = FileMessageSerializer.deserialize(messageBytes);

                    // Writes received bytes to Disk immediately via Cache Implementation
                    fileCache.put(fileMsg.getMessageId(), fileMsg.getFileName(), fileMsg.getFileContent());

                    // Send ONLY metadata to frontend (Coordinator/Adapter)
                    FileMessage metadataMsg = new FileMessage(
                            fileMsg.getMessageId(), fileMsg.getUserId(), fileMsg.getSenderDisplayName(),
                            fileMsg.getCaption(), fileMsg.getFileName(), null, fileMsg.getReplyToMessageId());
                    byte[] metadataBytes = FileMessageSerializer.serialize(metadataMsg);
                    this.rpc.call("chat:file-metadata-received", metadataBytes);
                    break;

                case ChatProtocol.FLAG_DELETE_MESSAGE:
                    String remoteId = new String(messageBytes, StandardCharsets.UTF_8).trim();
                    fileCache.remove(remoteId); // Deletes temp file
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