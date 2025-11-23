package com.swe.controller;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.MessageListener;

public interface NetworkingInterface {
    void sendData(byte[] data, ClientNode[] destIp, ModuleType module, int priority);
    
    void subscribe(ModuleType name, MessageListener function);
    
    void removeSubscription(ModuleType name);
    
    void addUser(ClientNode deviceAddress, ClientNode mainServerAddress);
    
    void closeNetworking();
    
    void consumeRPC(AbstractRPC rpc);

    void emitToSubscriber(ModuleType module, byte[] payload);
}



