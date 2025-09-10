package com.Comm_Uni_Cator.chat;

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
}
