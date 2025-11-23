package com.swe.controller;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.MessageListener;
import com.swe.networking.Dynamo.DynamoNetworking;

/**
 * Adapter class that wraps DynamoNetworking and implements NetworkingInterface.
 * This allows DynamoNetworking to be used where NetworkingInterface is expected.
 */
public class DynamoNetworkingAdapter implements NetworkingInterface {
    private final DynamoNetworking dynamoNetworking;

    public DynamoNetworkingAdapter(DynamoNetworking dynamoNetworking) {
        this.dynamoNetworking = dynamoNetworking;
    }

    @Override
    public void sendData(byte[] data, ClientNode[] destIp, ModuleType module, int priority) {
        dynamoNetworking.sendData(data, destIp, module.ordinal(), priority);
    }

    @Override
    public void subscribe(ModuleType name, MessageListener function) {
        // Convert SimpleNetworking.MessageListener to networking.MessageListener
        com.swe.networking.MessageListener networkingListener = function::receiveData;
        dynamoNetworking.subscribe(name.ordinal(), networkingListener);
    }

    @Override
    public void removeSubscription(ModuleType name) {
        dynamoNetworking.removeSubscription(name.ordinal());
    }

    @Override
    public void addUser(ClientNode deviceAddress, ClientNode mainServerAddress) {
        dynamoNetworking.addUser(deviceAddress, mainServerAddress);
    }

    @Override
    public void closeNetworking() {
        dynamoNetworking.closeNetworking();
    }

    @Override
    public void consumeRPC(AbstractRPC rpc) {
        dynamoNetworking.consumeRPC(rpc);
    }
}

