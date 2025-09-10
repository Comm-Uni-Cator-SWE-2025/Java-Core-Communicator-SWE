package com.Comm_Uni_Cator.chat;

import java.nio.charset.StandardCharsets;

public class ChatManager implements IChatService{
    private final abstractNetworking network;

    public ChatManager(abstractNetworking network) {
        this.network = network;

        // Subscribe to incoming network messages
        network.Subscribe("ChatManagerSubscription", this::receiveFromNetwork);
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
        System.out.println("Received message from " + message.getUserId()
                + ": " + message.getContent());
    }

    private void receiveFromNetwork(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        receiveMessage(json);
    }

    @Override
    public void deleteMessage(String messageId) {
        // not needed for demo
    }
}
