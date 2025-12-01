package com.swe.controller;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.MessageListener;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

/**
 * Adapter class that wraps SimpleNetworking and implements NetworkingInterface.
 * This allows SimpleNetworking to be used where NetworkingInterface is expected.
 */
public class SimpleNetworkingAdapter implements NetworkingInterface {
    /**
     * The underlying simple networking instance.
     */
    private final SimpleNetworking simpleNetworking;

    /**
     * Constructs a new SimpleNetworkingAdapter.
     *
     * @param simpleNetworkingParam The simple networking instance to wrap
     */
    public SimpleNetworkingAdapter(final SimpleNetworking simpleNetworkingParam) {
        this.simpleNetworking = simpleNetworkingParam;
    }

    @Override
    public void sendData(final byte[] data, final ClientNode[] destIp, final ModuleType module, final int priority) {
        simpleNetworking.sendData(data, destIp, module, priority);
    }

    @Override
    public void subscribe(final ModuleType name, final MessageListener function) {
        simpleNetworking.subscribe(name, function);
    }

    @Override
    public void removeSubscription(final ModuleType name) {
        simpleNetworking.removeSubscription(name);
    }

    @Override
    public void addUser(final ClientNode deviceAddress, final ClientNode mainServerAddress) {
        simpleNetworking.addUser(deviceAddress, mainServerAddress);
    }

    @Override
    public void closeNetworking() {
        simpleNetworking.closeNetworking();
    }

    @Override
    public void consumeRPC(final AbstractRPC rpc) {
        simpleNetworking.consumeRPC(rpc);
    }
}

