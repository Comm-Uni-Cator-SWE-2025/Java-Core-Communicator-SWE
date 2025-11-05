package com.swe.controller;

import com.socketry.SocketryClient;
import com.swe.controller.RPCinterface.AbstractRPC;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class RPC implements AbstractRPC {
    HashMap<String, Function<byte[], byte[]>> methods;

    private SocketryClient socketryServer;

    public RPC() {
        methods = new HashMap<>();
    }

    public void subscribe(String methodName, Function<byte[], byte[]> method) {
        methods.put(methodName, method);
    }

    public Thread connect() throws IOException, InterruptedException, ExecutionException {
        socketryServer = new SocketryClient(new byte[] {20}, 60000, methods);
        Thread rpcThread = new Thread(socketryServer::listenLoop);
        rpcThread.start();
        return rpcThread;
    }

    public CompletableFuture<byte[]> call(String methodName, byte[] data) {
        byte methodId = socketryServer.getRemoteProcedureId(methodName);
        try {
            return socketryServer.makeRemoteCall(methodId, data, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
