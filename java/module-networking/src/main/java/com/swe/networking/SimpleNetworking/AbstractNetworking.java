package com.swe.networking.SimpleNetworking;

import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;

/**
 * Interface used between other modules and networking to send data.
 * Every module subscribes to this interface and then sends data
 * using the sendData function
 * */
public interface AbstractNetworking {
    void sendData(byte[] data, ClientNode[] destIp, ModuleType module, int priority);

    void subscribe(ModuleType name, MessageListener function);

    void removeSubscription(ModuleType name);
}
