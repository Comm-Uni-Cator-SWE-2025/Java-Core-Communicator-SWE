/**
 * RPC implementation for remote procedure calls.
 * Contributed by Pushti Vasoya.
 */
package com.swe.core;

import com.socketry.SocketryClient;
import com.swe.core.RPCinterface.AbstractRPC;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * RPC implementation for remote procedure calls.
 */
public class RPC implements AbstractRPC {
    /**
     * Logger for RPC operations.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("CORE");

    /**
     * Module ID for Chat.
     */
    private static final byte MODULE_ID_CHAT = 1;

    /**
     * Module ID for Networking.
     */
    private static final byte MODULE_ID_NETWORKING = 2;

    /**
     * Module ID for Screensharing.
     */
    private static final byte MODULE_ID_SCREENSHARING = 5;

    /**
     * Module ID for Controller.
     */
    private static final byte MODULE_ID_CONTROLLER = 1;

    /**
     * Module ID for Misc.
     */
    private static final byte MODULE_ID_MISC = 1;

    /**
     * Registered RPC methods.
     */
    private HashMap<String, Function<byte[], byte[]>> methods;

    /**
     * Socketry client instance.
     */
    private SocketryClient socketryClient;

    /**
     * Connection status.
     */
    private Boolean isConnected = false;

    /**
     * Constructs a new RPC instance.
     */
    public RPC() {
        methods = new HashMap<>();
    }

    /**
     * Checks if RPC is connected.
     *
     * @return True if connected, false otherwise
     */
    public Boolean isConnected() {
        return isConnected;
    }

    @Override
    public void subscribe(final String methodName, final Function<byte[], byte[]> method) {
        methods.put(methodName, method);
    }

    /**
     * Connects to the RPC server.
     *
     * @param portNumber The port number to connect to
     * @return The RPC thread
     * @throws IOException If connection fails
     * @throws InterruptedException If interrupted
     * @throws ExecutionException If execution fails
     */
    public Thread connect(final int portNumber) throws IOException, InterruptedException, ExecutionException {

        socketryClient = new SocketryClient(new byte[] {
                MODULE_ID_CHAT, // Chat
                MODULE_ID_NETWORKING, // Networking
                MODULE_ID_SCREENSHARING, // Screensharing
                MODULE_ID_NETWORKING, // Canvas
                MODULE_ID_CONTROLLER, // Controller
                MODULE_ID_MISC, // Misc
        }, portNumber, methods);
        final Thread rpcThread = new Thread(socketryClient::listenLoop);
        rpcThread.start();
        isConnected = true;
        return rpcThread;
    }

    @Override
    public CompletableFuture<byte[]> call(final String methodName, final byte[] data) {
        if (socketryClient == null) {
            LOG.error("Server is null");
            return CompletableFuture.supplyAsync(() -> new byte[0]);
        }
        final byte methodId = socketryClient.getRemoteProcedureId(methodName);
        try {
            return socketryClient.makeRemoteCall(methodId, data, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
