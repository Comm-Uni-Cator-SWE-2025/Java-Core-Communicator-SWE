package com.swe.networking.Dynamo;

import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.dynamo.Dynamo;
import com.swe.networking.AbstractController;
import com.swe.networking.AbstractNetworking;
import com.swe.networking.MessageListener;

public class DynamoNetworking implements AbstractController, AbstractNetworking {
    // this is a singleton class
    private static DynamoNetworking dynamoNetworking;
    private final Dynamo dynamo;

    private DynamoNetworking() {
        this.dynamo = Dynamo.getInstance();
    }

    public static DynamoNetworking getDynamoNetworking() {
        if (dynamoNetworking == null) {
            dynamoNetworking = new DynamoNetworking();
        }
        return dynamoNetworking;
    }

    @Override
    public void sendData(byte[] data, ClientNode[] dest, int module, int priority) {
        dynamo.sendData(data, dest, module, priority);
    }

    @Override
    public void broadcast(byte[] data, int module, int priority) {
        dynamo.broadcast(data, module, priority);
    }

    @Override
    public void subscribe(int name, MessageListener function) {
        dynamo.subscribe(name, data -> {
            function.receiveData(data);
            return null;
        });
    }

    @Override
    public void removeSubscription(int name) {
        dynamo.removeSubscription(name);
    }

    @Override
    public void addUser(ClientNode deviceAddress, ClientNode mainServerAddress) {
        try {
            dynamo.addUser(deviceAddress, mainServerAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeNetworking() {
        dynamo.closeDynamo();
    }

    @Override
    public void consumeRPC(AbstractRPC rpc) {
        dynamo.consumeRPC(rpc);
    }

}
