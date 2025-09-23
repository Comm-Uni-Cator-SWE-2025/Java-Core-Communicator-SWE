package com.swe.Networking;

/**
 * Interface.
 */
public interface AbstractNetworking {
    
    void sendData(byte[] data, String[] dest, int[] port);
    
    String getSelfIP();
    
    void subscribe(String name, MessageListener function);
    
    void removeSubscription(String name);

}
