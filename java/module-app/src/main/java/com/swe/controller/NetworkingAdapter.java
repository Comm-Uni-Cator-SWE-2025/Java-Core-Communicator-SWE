package com.swe.controller;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.MessageListener;
import com.swe.networking.Networking;

/**
 * Adapter class that wraps Networking and implements NetworkingInterface.
 * This allows Networking to be used where NetworkingInterface is expected.
 */
public class NetworkingAdapter implements NetworkingInterface {
    private final Networking networking;

    public NetworkingAdapter(Networking networking) {
        this.networking = networking;
    }

    @Override
    public void sendData(byte[] data, ClientNode[] destIp, ModuleType module, int priority) {
        networking.sendData(data, destIp, module.ordinal(), priority);
    }

    @Override
    public void subscribe(ModuleType name, MessageListener function) {
        // Convert SimpleNetworking.MessageListener to Networking.MessageListener
        com.swe.networking.MessageListener networkingListener = function::receiveData;
        networking.subscribe(name.ordinal(), networkingListener);
    }

    @Override
    public void removeSubscription(ModuleType name) {
        networking.removeSubscription(name.ordinal());
    }

    @Override
    public void addUser(ClientNode deviceAddress, ClientNode mainServerAddress) {
        networking.addUser(deviceAddress, mainServerAddress);
    }

    @Override
    public void closeNetworking() {
        networking.closeNetworking();
    }

    @Override
    public void consumeRPC(AbstractRPC rpc) {
        // Convert RPCinterface.AbstractRPC to RPCinteface.AbstractRPC
        // Create an adapter wrapper since they're in different packages
        com.swe.core.RPCinterface.AbstractRPC adaptedRpc = new com.swe.core.RPCinterface.AbstractRPC() {
            @Override
            public void subscribe(String methodName, java.util.function.Function<byte[], byte[]> method) {
                rpc.subscribe(methodName, method);
            }

            @Override
            public Thread connect(int portNumber) throws java.io.IOException, java.util.concurrent.ExecutionException, InterruptedException {
                return rpc.connect(portNumber);
            }

            @Override
            public java.util.concurrent.CompletableFuture<byte[]> call(String methodName, byte[] data) {
                return rpc.call(methodName, data);
            }
        };
        networking.consumeRPC(adaptedRpc);
    }

    @Override
    public void emitToSubscriber(ModuleType module, byte[] payload) {
        networking.callSubscriber(module.ordinal(), payload);
    }
}
