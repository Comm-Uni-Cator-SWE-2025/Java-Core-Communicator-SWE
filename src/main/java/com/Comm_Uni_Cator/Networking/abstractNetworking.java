package com.Comm_Uni_Cator.Networking;

public interface abstractNetworking {
    void SendData(byte[] data, String[] dest,int[] port);
    void Subscribe(String name, MessageListener function);
    void RemoveSubscription(String name);
}
