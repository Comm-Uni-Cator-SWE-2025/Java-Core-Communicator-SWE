package com.swe.RPC;

import com.socketry.SocketryServer;
import com.swe.RPC.AbstractRPC;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class SocketryServerRPC implements AbstractRPC {

    private final int port;
    private SocketryServer server;
    // Collect procedures BEFORE starting the server
    private final HashMap<String, Function<byte[], byte[]>> procedures = new HashMap<>();

    public SocketryServerRPC(int port) {
        this.port = port;
    }

    @Override
    public void subscribe(String name, Function<byte[], byte[]> func) {
        System.out.println("[CORE RPC] Registered procedure: " + name);
        procedures.put(name, func);
    }

    @Override
    public Thread connect() throws IOException, ExecutionException, InterruptedException {
        System.out.println("[CORE RPC] Starting server on port " + port + "...");

        // 1. Create the real server, passing ALL collected procedures at once
        this.server = new SocketryServer(port, procedures);

        // 2. Start the listen loop in a background thread
        Thread listenerThread = new Thread(() -> {
            System.out.println("[CORE RPC] Server listen loop started.");
            this.server.listenLoop();
        });
        listenerThread.start();

        return listenerThread;
    }

    @Override
    public CompletableFuture<byte[]> call(String name, byte[] args) {
        // This part might be tricky. The 'DummyRPC' you found implements 'call'
        // by making a REMOTE call from the server to a client.
        // This is standard for two-way RPC.

        if (server == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Server not started"));
        }

        System.out.println("[CORE RPC] Calling client procedure: " + name);

        // 1. Get the ID for the procedure name
        Byte funcId = server.getRemoteProcedureId(name);
        if (funcId == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown procedure: " + name));
        }

        // 2. Make the call down to the client(s)
        try {
            // '0' might be a timeout or a specific client ID.
            // If it broadcasts to all, this is perfect. If not, you might need
            // to manage client IDs. For now, trust their Dummy implementation.
            return server.makeRemoteCall(funcId, args, 0);
        } catch (InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}