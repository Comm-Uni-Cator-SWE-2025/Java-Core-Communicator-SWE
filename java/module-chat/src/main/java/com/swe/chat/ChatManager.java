/**
 * Contributed by : Sachin(112101052)
 */

package com.swe.chat;

// 1. IMPORT THE REAL NETWORKING FILES
import com.swe.RPC.AbstractRPC;
import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.SimpleNetworking;
import com.swe.chat.Utilities;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.time.ZoneOffset;
/**
 * Manages chat functionality between the networking layer and the UI.
 * Handles sending, receiving, and notifying listeners about chat messages.
 * This class IMPLEMENTS the IChatService contract.
 */
// 2. RE-ADD THE IChatService INTERFACE
public class ChatManager implements IChatService {

    private static final byte FLAG_TEXT_MESSAGE = (byte) 0x01;
    private static final byte FLAG_FILE_MESSAGE = (byte) 0x02;
    /** 3. THE FIELD IS THE REAL SimpleNetworking OBJECT */
    private final AbstractRPC rpc;
    private final SimpleNetworking network;

    /** Listener that gets notified when a new message is received. */
    private Consumer<ChatMessage> onMessageReceivedListener; // Listener for the UI

    /**
     * 4. THE CONSTRUCTOR NOW TAKES THE REAL SimpleNetworking OBJECT
     *
     * @param networkingService the networking service used for communication
     */
    public ChatManager(SimpleNetworking network, AbstractRPC rpc) {
        this.rpc = rpc;
        this.network = network;

        // 1. Subscribe to calls from the Frontend (Strategy Pattern)
        this.rpc.subscribe("chat:send-text", this::handleFrontendTextMessage);
        this.rpc.subscribe("chat:send-file", this::handleFrontendFileMessage); // NEW
        this.rpc.subscribe("chat:delete-message", this::handleDeleteMessage);

        // 2. Subscribe to calls from the Network
        // This one handler will now route all message types
        this.network.subscribe(ModuleType.CHAT, this::handleNetworkMessage);
    }

    /**
     * Handles an incoming message from the Frontend via RPC.
     * Its job is to send this message to the network.
     */
    private byte[] handleFrontendTextMessage(byte[] messageBytes) {
        System.out.println("[Core] Received Text Message from Frontend.");

        // 1. Add the protocol flag for the network
        byte[] networkPacket = addProtocolFlag(messageBytes, FLAG_TEXT_MESSAGE);

        // 2. Send to the network
        // TODO: Get real destinations
        ClientNode[] dests = { new ClientNode("127.0.0.1", 1234) }; // Send to User 1
        this.network.sendData(networkPacket, dests, ModuleType.CHAT, 0);

        return new byte[0];
    }

    private byte[] handleFrontendFileMessage(byte[] messageBytes) {
        System.out.println("[Core] Received File Message (with path) from Frontend.");
        try {
            // 1. Deserialize Path-Mode message
            FileMessage fileMsg = FileMessageSerializer.deserialize(messageBytes);

            // 2. Validate file path
            String filePath = fileMsg.getFilePath();
            if (filePath == null || filePath.isEmpty()) {
                System.err.println("[Core] ERROR: File path is null or empty.");
                System.err.println("[Core] Message ID: " + fileMsg.getMessageId());
                System.err.println("[Core] File Name: " + fileMsg.getFileName());
                throw new IllegalArgumentException("File path is null or empty");
            }

            // 3. Sanitize the path (defensive)
            filePath = filePath.trim();
            if (filePath.startsWith("*")) {
                filePath = filePath.substring(1).trim();
            }

            System.out.println("[Core] Reading file: " + filePath);

            // 4. Check if file exists
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            if (!java.nio.file.Files.exists(path)) {
                throw new IllegalArgumentException("File does not exist: " + filePath);
            }

            // 5. Read and compress the file
            byte[] uncompressedData = java.nio.file.Files.readAllBytes(path);
            byte[] compressedData = Utilities.Compress(uncompressedData, Deflater.BEST_SPEED);
            if (compressedData == null) {
                throw new IOException("Failed to compress file.");
            }
            System.out.println("[Core] File compressed: " + uncompressedData.length + " → " + compressedData.length + " bytes");

            // 6. Create Content-Mode message (with compressed bytes)
            FileMessage contentModeMsg = new FileMessage(
                    fileMsg.getMessageId(),
                    fileMsg.getUserId(),
                    fileMsg.getSenderDisplayName(),
                    fileMsg.getCaption(),
                    fileMsg.getFileName(),
                    compressedData,
                    System.currentTimeMillis() / 1000,  // Epoch seconds
                    fileMsg.getReplyToMessageId()
            );

            // 7. Serialize Content-Mode message
            byte[] contentModeBytes = FileMessageSerializer.serialize(contentModeMsg);
            byte[] networkPacket = addProtocolFlag(contentModeBytes, FLAG_FILE_MESSAGE);

            // ===== CRITICAL: REMOVE THE LOCAL BROADCAST =====
            // System.out.println("[Core] Broadcasting file to local frontends via RPC.");
            // this.rpc.call("chat:file-received", contentModeBytes); // <--- DELETE THIS LINE

            // 8. Also send to network for remote users
            System.out.println("[Core] Sending to network for remote users.");
            ClientNode[] dests = { new ClientNode("127.0.0.1", 1234) };
            this.network.sendData(networkPacket, dests, ModuleType.CHAT, 0);

            System.out.println("[Core] File processed successfully.");

            // ===== NEW: RETURN THE CONTENT MESSAGE AS THE RESPONSE =====
            return contentModeBytes; // <--- THIS IS THE FIX

        } catch (Exception e) {
            System.err.println("[Core] FAILED to process file message: " + e.getMessage());
            e.printStackTrace();
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    // NEW: Helper method to notify all subscribed frontends
    private void notifyAllFrontends(String topic, byte[] data) {
        // This broadcasts to all connected clients subscribed to this topic
        // Your Socketry RPC likely has a method for this
        // Check your AbstractRPC interface for broadcast/publish methods
        this.rpc.call(topic, data); // For now, this may only notify one client
        // You may need to implement a proper pub/sub broadcast method
    }


    /**
     * Handles an incoming delete request from the Frontend via RPC.
     * Its job is to broadcast this deletion.
     */
    private byte[] handleDeleteMessage(byte[] messageIdBytes) {
        System.out.println("Core: Broadcasting deletion to all frontends.");
        this.rpc.call("chat:message-deleted", messageIdBytes);
        return new byte[0];
    }

    // --- Helper for adding the protocol flag ---
    private byte[] addProtocolFlag(byte[] data, byte flag) {
        byte[] flaggedPacket = new byte[data.length + 1];
        flaggedPacket[0] = flag;
        System.arraycopy(data, 0, flaggedPacket, 1, data.length);
        return flaggedPacket;
    }

    /**
     * The UI Controller will call this method to listen for new messages.
     * @param listener A function that accepts a ChatMessage.
     */
    public void setOnMessageReceived(final Consumer<ChatMessage> listener) {
        this.onMessageReceivedListener = listener;
    }

    /**
     * Sends a chat message through the network.
     * (This method correctly implements the IChatService contract)
     *
     * @param message the chat message to send
     */
    @Override
    public void sendMessage(final ChatMessage message) {
        // "Dummy send message";
    }

    /**
     * 7. RE-ADD receiveMessage TO FULFILL THE CONTRACT
     * Receives a chat message (from JSON), deserializes it,
     * and notifies the listener if present.
     * (This method correctly implements the IChatService contract)
     *
     * @param json serialized chat message
     */
    @Override
    public void receiveMessage(final String json) {
        // dummy receiver message
    }

    /**
     * Handles an incoming message from the Network.
     * Its job is to broadcast this message to all Frontends via RPC.
     */
    private void handleNetworkMessage(final byte[] networkPacket) {
        if (networkPacket == null || networkPacket.length == 0) return;

        // 1. Read the protocol flag
        byte flag = networkPacket[0];

        // 2. Strip the flag to get the original message bytes
        byte[] messageBytes = Arrays.copyOfRange(networkPacket, 1, networkPacket.length);

        // 3. Route to the correct broadcast
        switch (flag) {
            case FLAG_TEXT_MESSAGE:
                System.out.println("[Core] Received Text from Network, broadcasting to Frontends.");
                notifyAllFrontends("chat:new-message", messageBytes);
                break;
            case FLAG_FILE_MESSAGE:
                System.out.println("[Core] Received File from Network, broadcasting to Frontends.");
                notifyAllFrontends("chat:file-received", messageBytes);
                break;
            default:
                System.err.println("[Core] Received unknown message type: " + flag);
        }
    }


    /**
     * This is the private callback from the network.
     * It calls the public receiveMessage, just like your original code.
     *
     * @param data raw byte array received from the network
     */
    private void receiveFromNetwork(final byte[] data) {
        // Dummy try function;
    }

    /**
     * 8. RE-ADD deleteMessage TO FULFILL THE CONTRACT
     * Deletes a message by its unique ID.
     * (This method correctly implements the IChatService contract)
     *
     * @param messageId the ID of the message to delete
     */
    @Override
    public void deleteMessage(final String messageId) {
        System.out.println("Core: Broadcasting deletion of " + messageId + " to all frontends.");

        // TODO: In a real system, you would ALSO send a delete packet
        // over the SimpleNetworking to other Core servers.
        // network.sendData(deletePacket, ...);

        // Broadcast the delete command to all local frontends
        byte[] messageIdBytes = messageId.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.rpc.call("chat:message-deleted", messageIdBytes);
    }
}


