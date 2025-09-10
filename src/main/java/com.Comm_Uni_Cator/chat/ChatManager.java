package com.Comm_Uni_Cator.chat;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer; // Import Consumer for the listener

public class ChatManager implements IChatService {
    private final abstractNetworking network;
    private Consumer<ChatMessage> onMessageReceivedListener; // Listener for the UI

    public ChatManager(abstractNetworking network) {
        this.network = network;
        // The original subscribe had an issue with the method reference type.
        // It needs a MessageListener, which has a ReceiveData method.
        network.Subscribe("ChatManagerSubscription", this::receiveFromNetwork);
    }

    /**
     * The UI Controller will call this method to listen for new messages.
     * @param listener A function that accepts a ChatMessage.
     */
    public void setOnMessageReceived(Consumer<ChatMessage> listener) {
        this.onMessageReceivedListener = listener;
    }

    @Override
    public void sendMessage(ChatMessage message) {
        String json = MessageParser.serialize(message);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        network.SendData(data, new String[]{}, new int[]{}); // broadcast
    }

    @Override
    public void receiveMessage(String json) {
        ChatMessage message = MessageParser.deserialize(json);

        // Instead of printing to the console, notify our UI listener
        if (onMessageReceivedListener != null) {
            onMessageReceivedListener.accept(message);
        }
    }

    // This method matches the MessageListener interface
    private void receiveFromNetwork(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        receiveMessage(json);
    }

    @Override
    public void deleteMessage(String messageId) {
        // not needed for demo
    }
}
