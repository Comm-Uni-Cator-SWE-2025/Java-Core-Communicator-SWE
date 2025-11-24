package com.swe.canvas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.serialize.DataSerializer;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import com.swe.networking.Topology;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CanvasManager {

    private final Networking networking;
    private final AbstractRPC rpc;
    private final List<byte[]> canvasHistory;
    private ClientNode hostClientNode;
    private boolean isHost = false;

    public CanvasManager(Networking networking) {
        this.networking = networking;
        this.rpc = networking.getRPC();
        this.canvasHistory = new ArrayList<>();
        
        registerRPCMethods();
        
        this.networking.subscribe(ModuleType.CANVAS.ordinal(), this::handleNetworkMessage);
    }

    public void setHostClientNode(ClientNode hostClientNode) {
        this.hostClientNode = hostClientNode;
        System.out.println("[CanvasManager] Host Client Node set to " + hostClientNode);
    }

    public void setIsHost(boolean isHost) {
        this.isHost = isHost;
    }

    private void registerRPCMethods() {
        if (rpc == null) {
            System.err.println("RPC is null in CanvasManager");
            return;
        }

        rpc.subscribe("canvas:describe", this::handleDescribe);
        rpc.subscribe("canvas:regularize", this::handleRegularize);
        rpc.subscribe("canvas:sendToHost", this::handleSendToHost);
        rpc.subscribe("canvas:broadcast", this::handleBroadcast);
        rpc.subscribe("canvas:getHistory", this::handleGetHistory);
    }

    private byte[] handleDescribe(byte[] data) {
        try {
            // Deserialize input using DataSerializer
            String input = DataSerializer.deserialize(data, String.class);
            System.out.println("[CanvasManager] Canvas Describe requested for: " + input);

            // In a real scenario: AiInstance.getInstance().describe(input);
            
            return DataSerializer.serialize("Canvas is Described");
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private byte[] handleRegularize(byte[] data) {
        try {
            // Deserialize to JsonNode using DataSerializer
            JsonNode rootNode = DataSerializer.deserialize(data, JsonNode.class);

            // Find "Thickness" key and change value to 5
            if (rootNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) rootNode;
                if (objectNode.has("Thickness")) {
                    objectNode.put("Thickness", 5);
                }
            }

            // Serialize back using DataSerializer
            return DataSerializer.serialize(rootNode);

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private byte[] handleSendToHost(byte[] data) {
        try {
            if (hostClientNode == null) {
                System.err.println("[CanvasManager] Host Client Node is not set. Cannot send to host.");
                return new byte[0];
            }

            // Send the data to the Host Node using Networking.
            // The Host Node will receive it via 'handleNetworkMessage'.
            System.out.println("[CanvasManager] Sending data to host: " + hostClientNode);
            networking.sendData(data, new ClientNode[]{hostClientNode}, ModuleType.CANVAS.ordinal(), 1); // Module 2, Priority 1
            
            return new byte[0];
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private void handleNetworkMessage(byte[] data) {
        try {
            if (isHost) {
                // Host Logic:
                // Received data from a Client.
                // DO NOT broadcast yet. Send to Frontend for verification.
                rpc.call("canvas:update", data);
            } else {
                // Client Logic:
                // Received broadcast from Host.
                // Update Frontend.
                rpc.call("canvas:update", data);
                
                // Store in local history
                synchronized (canvasHistory) {
                    canvasHistory.add(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] handleBroadcast(byte[] data) {
        // Called by Host Frontend AFTER verification.
        try {
            // Broadcast to all clients
            networking.broadcast(data, ModuleType.CANVAS.ordinal(), 1);
            
            // Store in Host history
            synchronized (canvasHistory) {
                canvasHistory.add(data);
            }
            
            return new byte[0];
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private byte[] handleGetHistory(byte[] data) {
        try {
            synchronized (canvasHistory) {
                return DataSerializer.serialize(canvasHistory);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
