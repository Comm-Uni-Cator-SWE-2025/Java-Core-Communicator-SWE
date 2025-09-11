package com.swe.networking;

@FunctionalInterface
public interface MessageListener {
    void ReceiveData(byte[] data);
}
