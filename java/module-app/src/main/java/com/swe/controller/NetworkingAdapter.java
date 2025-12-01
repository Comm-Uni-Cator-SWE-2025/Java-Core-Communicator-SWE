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
    /**
     * The underlying networking instance.
     */
    private final Networking networking;

    /**
     * Constructs a new NetworkingAdapter.
     *
     * @param networkingParam The networking instance to wrap
     */
    public NetworkingAdapter(final Networking networkingParam) {
        this.networking = networkingParam;
    }

    @Override
    public void sendData(final byte[] data, final ClientNode[] destIp, final ModuleType module, final int priority) {
        networking.sendData(data, destIp, module.ordinal(), priority);
    }

    @Override
    public void subscribe(final ModuleType name, final MessageListener function) {
        // Convert SimpleNetworking.MessageListener to Networking.MessageListener
        final com.swe.networking.MessageListener networkingListener = function::receiveData;
        networking.subscribe(name.ordinal(), networkingListener);
    }

    @Override
    public void removeSubscription(final ModuleType name) {
        networking.removeSubscription(name.ordinal());
    }

    @Override
    public void addUser(final ClientNode deviceAddress, final ClientNode mainServerAddress) {
        networking.addUser(deviceAddress, mainServerAddress);
    }

    @Override
    public void closeNetworking() {
        networking.closeNetworking();
    }

    @Override
    public void consumeRPC(final AbstractRPC rpc) {
        // Convert RPCinterface.AbstractRPC to RPCinteface.AbstractRPC
        // Create an adapter wrapper since they're in different packages
        final com.swe.core.RPCinterface.AbstractRPC adaptedRpc =
            new com.swe.core.RPCinterface.AbstractRPC() {
            @Override
            public void subscribe(final String methodName,
                                  final java.util.function.Function<byte[], byte[]> method) {
                rpc.subscribe(methodName, method);
            }

            @Override
            public Thread connect(final int portNumber)
                throws java.io.IOException, java.util.concurrent.ExecutionException, InterruptedException {
                return rpc.connect(portNumber);
            }

            @Override
            public java.util.concurrent.CompletableFuture<byte[]> call(final String methodName, final byte[] data) {
                return rpc.call(methodName, data);
            }
        };
        networking.consumeRPC(adaptedRpc);
    }
}

