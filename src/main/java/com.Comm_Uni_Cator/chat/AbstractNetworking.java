package com.Comm_Uni_Cator.chat;

/**
 * Defines networking operations for sending data and managing subscriptions.
 */

public interface AbstractNetworking {
    void sendData(byte[] data, String[] dest, int[] port);

    void subscribe(String name, MessageListener function);

    void removeSubscription(String name);

}
