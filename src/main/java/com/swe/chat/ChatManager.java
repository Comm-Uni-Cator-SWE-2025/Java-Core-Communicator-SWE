package com.swe.chat;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer; // Import Consumer for the listener

/**
 * Manages chat functionality between the networking layer and the UI.
 * Handles sending, receiving, and notifying listeners about chat messages.
 */
public class ChatManager implements IChatService {

    /** Networking layer used to send and receive data. */
    private final AbstractNetworking network;

    /** Listener that gets notified when a new message is received. */
    private Consumer<ChatMessage> onMessageReceivedListener; // Listener for the UI

    /**
     * Constructs a ChatManager with the given networking service.
     *
     * @param networkingService the networking service used for communication
     */
    public ChatManager(final AbstractNetworking networkingService) {
        this.network = networkingService;
        // The original subscribe had an issue with the method reference type.
        // It needs a MessageListener, which has a ReceiveData method.
        network.subscribe("ChatManagerSubscription", this::receiveFromNetwork);
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
     *
     * @param message the chat message to send
     */
    @Override
    public void sendMessage(final ChatMessage message) {
        final String json = MessageParser.serialize(message);
        final byte[] data = json.getBytes(StandardCharsets.UTF_8);
        network.sendData(data, new String[]{}, new int[]{}); // broadcast
    }

    /**
     * Receives a chat message (from JSON), deserializes it,
     * and notifies the listener if present.
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
     * Handles incoming raw network data, converts it to a JSON string,
     * and passes it to the receiveMessage method.
     *
     * @param data raw byte array received from the network
     */
    private void receiveFromNetwork(final byte[] data) {
        final String json = new String(data, StandardCharsets.UTF_8);
        receiveMessage(json);
    }

    /**
     * Deletes a chat message by its ID.
     * Currently not implemented for the demo.
     *
     * @param messageId ID of the message to delete
     */
    @Override
    public void deleteMessage(final String messageId) {
        // not needed for demo
    }
}
