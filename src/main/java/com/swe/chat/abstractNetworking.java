package com.swe.chat;

public interface abstractNetworking {
    void SendData(byte[] data, String[] dest,int[] port);
    void Subscribe(String name, MessageListener function);
    void RemoveSubscription(String name);
}
