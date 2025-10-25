package com.swe.networking;

/**
 * Interface used between other modules and networking to send data.
 * Every module subscribes to this interface and then sends data
 * using the sendData function
 * */
public interface AbstractNetworking {
    void sendData(byte[] data, ClientNode[] dest, ModuleType module);

    void sendData(byte[] data);

    void subscribe(String name, MessageListener function);

    String getSelfIP();

    void removeSubscription(String name);
}
