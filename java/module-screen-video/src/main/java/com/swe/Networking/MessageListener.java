package com.swe.Networking;

@FunctionalInterface
public interface MessageListener {
    void ReceiveData(byte[] data);
}
