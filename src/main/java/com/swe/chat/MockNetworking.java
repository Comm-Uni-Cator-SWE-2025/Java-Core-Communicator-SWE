package com.swe.chat;

import java.util.ArrayList;
import java.util.List;

public class MockNetworking implements abstractNetworking {
    private final List<MessageListener> listeners = new ArrayList<>();

    @Override
    public void SendData(byte[] data, String[] dest, int[] port) {
        // Just loop back the data to the subscribed listener
        for(MessageListener listener : listeners) {
            listener.ReceiveData(data);
        }
    }

    @Override
    public void Subscribe(String name, MessageListener function) {
        this.listeners.add(function);
    }

    @Override
    public void RemoveSubscription(String name) {
//        this.listener = null;
    }



    public void simulateMessageFromServer(byte[] data) {
        System.out.println("NETWORK_SIM: A message was received from the 'server'.");
        for (MessageListener listener : listeners) {
            listener.ReceiveData(data);
        }
    }
}
