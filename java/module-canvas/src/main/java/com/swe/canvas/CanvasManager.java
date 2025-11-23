package com.swe.canvas;

import com.swe.core.Context;
import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.AbstractNetworking;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import com.swe.networking.Networking;
import com.swe.aiinsights.aiinstance.AiInstance;


/**
 * ============================================================================
 * BACKEND - CanvasManager
 * ============================================================================
 *
 * Responsibilities:
 *   1. Receive canvas actions from frontend (create, update, delete)
 *   2. Validate shapes (owner, format, coordinates)
 *   3. Store VALIDATED shapes in memory
 *   4. Broadcast validated changes to remote peers
 *   5. Receive remote changes and forward to frontend
 *
 * Backend NEVER draws anything.
 */
public class CanvasManager {

    private final AbstractRPC rpc;
    private final Networking network;
    private ClientNode hostClientNode;

    private AiInstance aiInstance;

    public CanvasManager(Networking network, ClientNode hostClientNode) {
        // Constructor implementation (if needed)
        Context context = Context.getInstance();
        this.rpc = context.rpc;
        this.network = network;
        this.aiInstance = AiInstance.getInstance();
        this.hostClientNode = hostClientNode;
        
        this.rpc.subscribe("canvas:regularize", this::handleRegularize);
        this.rpc.subscribe("canvas:describe", this::handleDescribe);
        this.rpc.subscribe("canvas:getHostIp", this::handleGetHostIp);

    }


    private byte[] handleRegularize(byte[] data) {
        // byte is the serialized action

        String json = data.toString();
        CompletableFuture<String> resp = aiInstance.regularize(json);
        
        byte[] result = new byte[0];

        // what you want to do here
        resp.thenAccept(response -> {
            result = response.getBytes();
        }).join();

        return result;
    }

    private byte[] handleDescribe(byte[] data) {
        String path = data.toString();
        CompletableFuture<String> resp = aiInstance.describe(path);

        byte[] result = new byte[0];
        resp.thenAccept(response -> {
            // func to send back to frontend
            result = response.getBytes();
        }).join();

        return result;
    }


    String serializeHost(ClientNode host){
        return host.hostName() + ":" + host.port();
    }

    private byte[] handleGetHostIp(byte[] data) {
        String hostString = serializeHost(this.hostClientNode);
        return hostString.getBytes(StandardCharsets.UTF_8);
    }

}
