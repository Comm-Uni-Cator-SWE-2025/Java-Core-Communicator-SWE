package com.swe.Networking;

/**
 * Interface.
 */
@FunctionalInterface
public interface MessageListener {
    void receiveData(byte[] data);
}
