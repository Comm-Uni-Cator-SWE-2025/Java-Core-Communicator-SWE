package com.Comm_Uni_Cator.chat;

/**
 * Functional interface for listening to incoming message data.
 */
@FunctionalInterface
public interface MessageListener {

    /**
     * Called when message data is received.
     *
     * @param data the received message data
     */
    void receiveData(byte[] data);
}
