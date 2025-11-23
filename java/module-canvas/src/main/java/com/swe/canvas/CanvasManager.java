package com.swe.canvas;

import com.swe.core.Context;
import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.networking.Networking;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;


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

    private AiClientService aiClientService;

    public CanvasManager(Networking network, ClientNode hostClientNode) {
        // Constructor implementation (if needed)
        Context context = Context.getInstance();
        this.rpc = context.rpc;
        this.network = network;
        this.aiClientService = AiInstance.getInstance();
        this.hostClientNode = hostClientNode;
        
        this.rpc.subscribe("canvas:regularize", this::handleRegularize);
        this.rpc.subscribe("canvas:describe", this::handleDescribe);
        this.rpc.subscribe("canvas:getHostIp", this::handleGetHostIp);

    }


    private byte[] handleRegularize(byte[] data) {
        final String json = new String(data, StandardCharsets.UTF_8);
        final CompletableFuture<String> response = aiClientService.regularise(json);
        return response.thenApply(result -> result.getBytes(StandardCharsets.UTF_8)).join();
    }

    private byte[] handleDescribe(byte[] data) {
        final String path = new String(data, StandardCharsets.UTF_8);
        final CompletableFuture<String> response = aiClientService.describe(path);
        return response.thenApply(result -> result.getBytes(StandardCharsets.UTF_8)).join();
    }


    String serializeHost(ClientNode host){
        return host.hostName() + ":" + host.port();
    }

    private byte[] handleGetHostIp(byte[] data) {
        String hostString = serializeHost(this.hostClientNode);
        return hostString.getBytes(StandardCharsets.UTF_8);
    }

}
