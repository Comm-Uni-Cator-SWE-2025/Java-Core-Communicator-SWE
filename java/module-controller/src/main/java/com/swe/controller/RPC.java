package com.swe.controller;

import com.socketry.SocketryServer;
import com.swe.RPC.AbstractRPC;

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

    public void subscribe(String methodName, Function<byte[], byte[]> method) {
        methods.put(methodName, method);
    }

    public Thread connect() throws IOException, InterruptedException, ExecutionException {
        socketryServer = new SocketryServer(60000, methods);
        final Thread rpcThread = new Thread(socketryServer::listenLoop);
        rpcThread.start();
        return rpcThread;
    }

    public CompletableFuture<byte[]> call(String methodName, byte[] data) {
        if(socketryServer == null) {
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
