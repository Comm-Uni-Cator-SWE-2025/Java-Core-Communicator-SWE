package com.swe.chat;

@FunctionalInterface
public interface MessageListener {
    void ReceiveData(byte[] data);
}
