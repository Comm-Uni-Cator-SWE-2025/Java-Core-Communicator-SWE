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
import com.swe.core.logging.SweLogger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CanvasManager {

    private final Networking networking;
    private final AbstractRPC rpc;
    private ClientNode hostClientNode;
    private ClientNode selfClientNode;
    private final SweLogger logger;

    public CanvasManager(Networking networking, SweLogger logger) {
        this.logger = logger;
        this.networking = networking;
        this.rpc = networking.getRPC();
        
        registerRPCMethods();
        
        this.networking.subscribe(ModuleType.CANVAS.ordinal(), this::handleNetworkMessage);
    }

    public void setHostClientNode(ClientNode hostClientNode) {
        this.hostClientNode = hostClientNode;
        logger.info("Host client node set to " + hostClientNode);
    }

    public void setSelfClientNode(ClientNode selfClientNode) {
        this.selfClientNode = selfClientNode;
        logger.info("Self client node set to " + selfClientNode);
    }

    private void registerRPCMethods() {
        if (rpc == null) {
            logger.error("RPC is null in CanvasManager");
            return;
        }

        rpc.subscribe("canvas:describe", this::handleDescribe);
        rpc.subscribe("canvas:regularize", this::handleRegularize);
        rpc.subscribe("canvas:sendToHost", this::handleSendToHost);
        rpc.subscribe("canvas:sendToClient", this::handleSendToClient);
        rpc.subscribe("canvas:broadcast", this::handleBroadcast);
        rpc.subscribe("canvas:whoami", this::handleWhoAmI);
    }

    private byte[] handleWhoAmI(byte[] data) {
        try {
            if (selfClientNode == null) {
                logger.warn("Self client node is not set.");
                return new byte[0];
            }
            return DataSerializer.serialize(selfClientNode);
        } catch (Exception e) {
            logger.error("Failed to handle whoami request", e);
            return new byte[0];
        }
    }

    private byte[] handleDescribe(byte[] data) {
        try {
            // Deserialize input using DataSerializer
            String input = new String(data, StandardCharsets.UTF_8);
            System.out.println("[CanvasManager] Canvas Describe requested for: " + input);

            // In a real scenario: AiInstance.getInstance().describe(input);
            String result = null;
            String future = AiInstance.getInstance().describe(input).get();
            result = future;    
            return result.getBytes();
        } catch (Exception e) {
            logger.error("Failed to handle describe request", e);
            return new byte[0];
        }
    }

    private byte[] handleRegularize(byte[] data) {
        try {
            // Deserialize to JsonNode using DataSerializer
            String rootNode = DataSerializer.deserialize(data, String.class);
            System.out.println("[CanvasManager] Regularizing canvas data. : " + rootNode);
            String result = null;
            String future = AiInstance.getInstance().regularise(rootNode).get();
            result = future;
            System.out.println("[CanvasManager] Modified: " + result);
            // Serialize back using DataSerializer
            return DataSerializer.serialize(result);

        } catch (Exception e) {
            logger.error("Failed to regularize canvas payload", e);
            return new byte[0];
        }
    }

    private byte[] handleSendToHost(byte[] data) {
        try {
            if (hostClientNode == null) {
                logger.warn("Host client node is not set. Cannot send to host.");
                return new byte[0];
            }

            // Send the data to the Host Node using Networking.
            // The Host Node will receive it via 'handleNetworkMessage'.
            logger.info("Sending data to host " + hostClientNode);
            networking.sendData(data, new ClientNode[]{hostClientNode}, ModuleType.CANVAS.ordinal(), 1); // Module 2, Priority 1
            
            return new byte[0];
        } catch (Exception e) {
            logger.error("Failed to send data to host", e);
            return new byte[0];
        }
    }

    private void handleNetworkMessage(byte[] data) {
        try {
            logger.debug("Everyone received broadcast from host");
            rpc.call("canvas:update", data);
        } catch (Exception e) {
            logger.error("Failed to handle network message", e);
        }
    }

    private byte[] handleBroadcast(byte[] data) {
        // Called by Host Frontend AFTER verification.
        try {
            // Broadcast to all clients
            logger.info("Host broadcasting verified data to all clients");
            networking.broadcast(data, ModuleType.CANVAS.ordinal(), 1);
            
            return new byte[0];
        } catch (Exception e) {
            logger.error("Failed to broadcast canvas update", e);
            return new byte[0];
        }
    }

    private byte[] handleSendToClient(byte[] data) {
        try {
            JsonNode node = DataSerializer.deserialize(data, JsonNode.class);
            
            if (node == null || !node.has("target") || !node.has("data")) {
                logger.warn("Invalid unicast request received.");
                return new byte[0];
            }

            // Extract target ClientNode
            JsonNode targetNode = node.get("target");
            String hostName = targetNode.get("hostName").asText();
            int port = targetNode.get("port").asInt();
            ClientNode target = new ClientNode(hostName, port);

            // Extract data string
            String payloadData = node.get("data").asText();

            logger.info("Sending unicast data to " + target);
            byte[] payload = DataSerializer.serialize(payloadData);
            
            networking.sendData(payload, new ClientNode[]{target}, ModuleType.CANVAS.ordinal(), 1);
            
            return new byte[0];
        } catch (Exception e) {
            logger.error("Failed to send unicast data to client", e);
            return new byte[0];
        }
    }

}
