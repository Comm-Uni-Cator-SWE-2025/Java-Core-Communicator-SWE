package com.swe.chat;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.Context;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.chat.IChatFileCache.FileCacheEntry;
import com.swe.core.logging.SweLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

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
    private final Networking network;
    private final SweLogger logger;

    private final AiClientService aiService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final IChatProcessor processor; // DIP: Message logic delegated
    private final IAiAnalyticsService aiAnalyticsService;

    /**
     * CONSTRUCTOR: Injects all necessary services and wires events.
     * @param network The networking service.
     */
    public ChatManager(Networking network, SweLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
        Context context = Context.getInstance();
        this.rpc = context.getRpc();

        // 1. Initialize Delegated Services
        AiClientService aiService = AiInstance.getInstance();
        IChatFileHandler fileHandler = new LocalFileHandler();
        IChatFileCache fileCache = new InMemoryFileCache();

        // 2. Initialize the History/AI Service (Stateful Layer - SRP)
        this.aiAnalyticsService = new AiAnalyticsService(this.rpc, network, aiService);

        // 3. Initialize the Core Processor (Execution Layer - SRP)
        this.processor = new ChatProcessor(this.rpc, network, fileHandler, fileCache, aiAnalyticsService);

        // 4. Subscribe & Route (Router's job: Wiring RPC/Network events to the Processor)
        this.rpc.subscribe("chat:send-text", this::handleFrontendTextMessage);
        this.rpc.subscribe("chat:send-file", this::handleFrontendFileMessage);
        this.rpc.subscribe("chat:delete-message", this::handleDeleteMessage);
        this.rpc.subscribe("chat:save-file-to-disk", this::handleSaveFileToDisk);

        // Network events are routed to the Processor's handler
        network.subscribe(ModuleType.CHAT.ordinal(), this::handleNetworkMessage);
    }

    // ============================================================================
    // RPC HANDLERS (Simple Delegation)
    // ============================================================================

    private byte[] handleFrontendTextMessage(byte[] messageBytes) {
        try {
            return processor.processFrontendTextMessage(messageBytes);
        } catch (Exception e) {
            logger.error("Error sending text message", e);
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] handleFrontendFileMessage(byte[] messageBytes) {
        try {
            return processor.processFrontendFileMessage(messageBytes);
        } catch (Exception e) {
            logger.error("[Core] Error processing file: " + e.getMessage());
            e.printStackTrace();
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] handleSaveFileToDisk(byte[] messageIdBytes) {
        try {
            return processor.processFrontendSaveRequest(messageIdBytes);
        } catch (Exception e) {
            logger.error("Error saving file", e);

            String errorMsg = "Failed to save file: " + e.getMessage();
            this.rpc.call("chat:file-saved-error", errorMsg.getBytes(StandardCharsets.UTF_8));

            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] handleDeleteMessage(byte[] messageIdBytes) {
        try {
            return processor.processFrontendDelete(messageIdBytes);
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

    public IAiAnalyticsService getAnalyticsService() {
        return this.aiAnalyticsService;
    }
}