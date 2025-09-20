package com.swe.Networking;

public interface AbstractNetworking {
    void SendData(byte[] data, String[] dest,int[] port);
    String getSelfIP();
    void Subscribe(String name, MessageListener function);
    void RemoveSubscription(String name);
}
