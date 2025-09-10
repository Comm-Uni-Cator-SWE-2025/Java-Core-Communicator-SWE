package com.Comm_Uni_Cator.Networking;

@FunctionalInterface
public interface MessageListener {
    void ReceiveData(byte[] data);
}
