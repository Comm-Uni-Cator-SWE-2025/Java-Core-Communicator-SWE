package com.swe.core;

import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.MessageListener;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

/**
 * Adapter class that wraps SimpleNetworking and implements NetworkingInterface.
 * This allows SimpleNetworking to be used where NetworkingInterface is expected.
 */
public class SimpleNetworkingAdapter implements NetworkingInterface {
    private final SimpleNetworking simpleNetworking;

    public SimpleNetworkingAdapter(SimpleNetworking simpleNetworking) {
        this.simpleNetworking = simpleNetworking;
    }

    @Override
    public void sendData(byte[] data, ClientNode[] destIp, ModuleType module, int priority) {
        simpleNetworking.sendData(data, destIp, module, priority);
    }

    @Override
    public void subscribe(ModuleType name, MessageListener function) {
        simpleNetworking.subscribe(name, function);
    }

    @Override
    public void removeSubscription(ModuleType name) {
        simpleNetworking.removeSubscription(name);
    }

    @Override
    public void addUser(ClientNode deviceAddress, ClientNode mainServerAddress) {
        simpleNetworking.addUser(deviceAddress, mainServerAddress);
    }

    @Override
    public void closeNetworking() {
        simpleNetworking.closeNetworking();
    }
}

