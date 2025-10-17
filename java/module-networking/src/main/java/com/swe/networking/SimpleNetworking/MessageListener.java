package com.swe.networking.SimpleNetworking;

/**
 * Interface which the networking module invokes during sending data.
 * Each module must implement their respective receiveData function
 *
 */
@FunctionalInterface
public interface MessageListener {
    void receiveData(byte[] data);
}
