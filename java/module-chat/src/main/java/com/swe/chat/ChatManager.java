package com.swe.chat;

// 1. IMPORT THE REAL NETWORKING FILES
import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Manages chat functionality between the networking layer and the UI.
 * Handles sending, receiving, and notifying listeners about chat messages.
 * This class IMPLEMENTS the IChatService contract.
 */
// 2. RE-ADD THE IChatService INTERFACE
public class ChatManager implements IChatService {

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
    public ChatManager(AbstractRPC rpc, SimpleNetworking network) {
        this.rpc = rpc;
        this.network = network;

        // 5. THE SUBSCRIBE CALL IS UPDATED
// 1. Subscribe to calls from the Frontend
        this.rpc.subscribe("chat:send-message", this::handleFrontendMessage);

        // 2. Subscribe to calls from the Network
        this.network.subscribe(ModuleType.CHAT, this::handleNetworkMessage);
    }

    /**
     * Handles an incoming message from the Frontend via RPC.
     * Its job is to send this message to the network.
     */
    private byte[] handleFrontendMessage(byte[] messageBytes) {
        // messageBytes is a serialized ChatMessage from the frontend

        // TODO: Get the list of destination IPs from the Controller/Meeting
        ClientNode[] dests = { new ClientNode("127.0.0.1", 5678) }; // Placeholder

        // Send to the network (this part is the same as your old sendMessage)
        this.network.sendData(messageBytes, dests, ModuleType.CHAT, 0);

        return null; // 'null' means "I received it, no reply needed"
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
        final String json = MessageParser.serialize(message);
        final byte[] data = json.getBytes(StandardCharsets.UTF_8);

        // 6. THE SENDDATA CALL IS UPDATED
        // Old: network.sendData(data, new String[]{}, new int[]{}); // broadcast

        // New: We must provide a destination and ModuleType.
        // TODO: You need a real way to get destinations.
        // For now, I am using the hard-coded destination from their example.
        ClientNode[] dests = { new ClientNode("127.0.0.1", 5678) };

        network.sendData(data, dests, ModuleType.CHAT, 0);
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
        final ChatMessage message = MessageParser.deserialize(json);

        // Instead of printing to the console, notify our UI listener
        if (onMessageReceivedListener != null) {
            onMessageReceivedListener.accept(message);
        }
    }

    /**
     * Handles an incoming message from the Network.
     * Its job is to broadcast this message to all Frontends via RPC.
     */
    private void handleNetworkMessage(final byte[] messageBytes) {
        // messageBytes is a serialized ChatMessage from the network

        // Broadcast this message to all listening frontends
        this.rpc.call("chat:new-message", messageBytes);
    }

    /**
     * This is the private callback from the network.
     * It calls the public receiveMessage, just like your original code.
     *
     * @param data raw byte array received from the network
     */
    private void receiveFromNetwork(final byte[] data) {
        final String json = new String(data, StandardCharsets.UTF_8);
        // This now correctly calls the method from your contract
        receiveMessage(json);
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
        // not needed for demo
    }
}