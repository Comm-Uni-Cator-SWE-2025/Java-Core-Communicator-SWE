/**
 *  Contributed by Pushti Vasoya.
 */

package com.swe.core;

import com.socketry.SocketryClient;
import com.swe.core.RPCinterface.AbstractRPC;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class RPC implements AbstractRPC {
    HashMap<String, Function<byte[], byte[]>> methods;

    private SocketryClient socketryClient;
    private Boolean isConnected = false;

    public RPC() {
        methods = new HashMap<>();
    }

    public Boolean isConnected() {
        return isConnected;
    }

    @Override
    public void subscribe(String methodName, Function<byte[], byte[]> method) {
        methods.put(methodName, method);
    }

    public Thread connect(int portNumber) throws IOException, InterruptedException, ExecutionException {

        socketryClient = new SocketryClient(new byte[] {
                1, // Chat
                2, // Networking
                5, // Screensharing
                2, // Canvas
                1, // Controller
                1, // Misc
        }, portNumber, methods);
        Thread rpcThread = new Thread(socketryClient::listenLoop);
        rpcThread.start();
        isConnected = true;
        return rpcThread;
    }

    @Override
    public CompletableFuture<byte[]> call(final String methodName, final byte[] data) {
        if (socketryClient == null) {
            System.err.println("Server is null");
            return CompletableFuture.supplyAsync(() -> {
                return new byte[0];
            });
        }
        final byte methodId = socketryClient.getRemoteProcedureId(methodName);
        try {
            return socketryClient.makeRemoteCall(methodId, data, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
