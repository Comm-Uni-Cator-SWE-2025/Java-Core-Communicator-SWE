package com.Comm_Uni_Cator.Networking;

public interface AbstractNetworking {
    void SendData(byte[] data, String[] dest,int[] port);
    void Subscribe(String name, MessageListener function);
    void RemoveSubscription(String name);
}
