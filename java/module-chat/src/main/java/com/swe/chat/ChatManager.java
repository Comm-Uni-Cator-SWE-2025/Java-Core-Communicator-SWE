// module-chat/src/main/java/com/swe/chat/ChatManager.java (Final Router/Adapter)

package com.swe.chat;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.Context;
import com.swe.networking.Networking;
import com.swe.networking.ModuleType;
import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.aiinsights.apiendpoints.AiClientService;

import java.nio.charset.StandardCharsets;

// IMPORTS REMOVED: Files, Paths, Arrays, List, Map, ConcurrentHashMap, Collections, Deflater, ScheduledExecutorService, TimeUnit, etc.

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
    public ChatManager(Networking network) {
        Context context = Context.getInstance();
        this.rpc = context.rpc;

        // 1. Initialize Delegated Services
        AiClientService aiService = AiInstance.getInstance();
        IChatFileHandler fileHandler = new LocalFileHandler();
        IChatFileCache fileCache = new InMemoryFileCache();

        // 2. Initialize the Core Processor (SRP: All business logic here)
        this.processor = new ChatProcessor(this.rpc, network, aiService, fileHandler, fileCache);

        // 3. Subscribe & Route (Router's job: Wiring RPC/Network events to the Processor)
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
            // DELEGATE ALL PROCESSING to the IChatProcessor
            return processor.processFrontendTextMessage(messageBytes);
        } catch (Exception e) {
            System.err.println("[Core.Router] Fatal error processing text: " + e.getMessage());
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] handleFrontendFileMessage(byte[] messageBytes) {
        try {
            // DELEGATE ALL PROCESSING to the IChatProcessor
            return processor.processFrontendFileMessage(messageBytes);
        } catch (Exception e) {
            System.err.println("[Core.Router] Fatal error processing file message: " + e.getMessage());
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] handleSaveFileToDisk(byte[] messageIdBytes) {
        try {
            // DELEGATE ALL PROCESSING to the IChatProcessor
            return processor.processFrontendSaveRequest(messageIdBytes);
        } catch (Exception e) {
            System.err.println("[Core.Router] Fatal error processing save request: " + e.getMessage());
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] handleDeleteMessage(byte[] messageIdBytes) {
        try {
            // DELEGATE ALL PROCESSING to the IChatProcessor
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
        // DELEGATE ALL PROCESSING to the IChatProcessor
        processor.processNetworkMessage(networkPacket);
    }

    // ============================================================================
    // IChatService Implementation (Adapter Pattern)
    // ============================================================================

    @Override
    public void sendMessage(ChatMessage message) {
        // Adapter: Wraps external API calls into the internal RPC format
        try {
            byte[] messageBytes = ChatMessageSerializer.serialize(message);
            this.rpc.call("chat:send-text", messageBytes);
        } catch (Exception e) {
            System.err.println("[ChatService] Failed to send message via RPC: " + e.getMessage());
        }
    }

    @Override
    public void receiveMessage(String json) {
        // Adapter: Wraps external API calls into the internal RPC format
        try {
            this.rpc.call("chat:new-message", json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("[ChatService] Failed to inject message via RPC: " + e.getMessage());
        }
    }

    @Override
    public void deleteMessage(String messageId) {
        // Adapter: Wraps external API calls into the internal RPC format
        try {
            byte[] messageIdBytes = messageId.getBytes(StandardCharsets.UTF_8);
            this.rpc.call("chat:delete-message", messageIdBytes);
        } catch (Exception e) {
            System.err.println("[ChatService] Failed to send delete via RPC: " + e.getMessage());
        }
    }
}