package com.Comm_Uni_Cator.chat;

@FunctionalInterface
public interface MessageListener {
    void ReceiveData(byte[] data);
}
