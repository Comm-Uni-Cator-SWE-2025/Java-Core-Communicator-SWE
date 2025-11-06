package com.swe.core;

import com.socketry.SocketryServer;
import com.swe.core.RPCinterface.AbstractRPC;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class RPC implements AbstractRPC {
    HashMap<String, Function<byte[], byte[]>> methods;

    private SocketryServer socketryServer;

    public RPC() {
        methods = new HashMap<>();
    }

    @Override
    public void subscribe(String methodName, Function<byte[], byte[]> method) {
        methods.put(methodName, method);
    }

    public Thread connect(int portNumber) throws IOException, InterruptedException, ExecutionException {
        socketryServer = new SocketryClient(new byte[] {
            1, // Chat
            2, // Networking
            5, // Screensharing
            2, // Canvas
            1, // Controller
            1, // Misc
        }, portNumber, methods);
        Thread rpcThread = new Thread(socketryServer::listenLoop);
        rpcThread.start();
        return rpcThread;
    }

    @Override
    public CompletableFuture<byte[]> call(final String methodName, final byte[] data) {
        if (socketryServer == null) {
            System.err.println("Server is null");
            return CompletableFuture.supplyAsync(() -> {
                return new byte[0];
            });
        }
        final byte methodId = socketryServer.getRemoteProcedureId(methodName);
        try {
            return socketryServer.makeRemoteCall(methodId, data, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
