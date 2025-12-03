package com.swe.canvas;

import com.fasterxml.jackson.databind.JsonNode;
import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.logging.SweLogger;
import com.swe.core.serialize.DataSerializer;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Manages canvas operations, including RPC handling and network synchronization
 * between the host and client nodes.
 */
public class CanvasManager {

    /**
     * The networking instance used for communication.
     */
    private final Networking networking;

    /**
     * The RPC interface for handling remote procedure calls.
     */
    private final AbstractRPC rpc;

    /**
     * The client node representing the host.
     */
    private ClientNode hostClientNode;

    /**
     * The client node representing the current instance.
     */
    private ClientNode selfClientNode;

    /**
     * Logger for recording events and errors.
     */
    private final SweLogger logger;

    /**
     * Constructs a new CanvasManager.
     *
     * @param networkSystem The networking instance.
     * @param sweLogger     The logger instance.
     */
    public CanvasManager(final Networking networkSystem, final SweLogger sweLogger) {
        this.logger = sweLogger;
        this.networking = networkSystem;
        this.rpc = networking.getRPC();

        registerRPCMethods();

        this.networking.subscribe(ModuleType.CANVAS.ordinal(), this::handleNetworkMessage);
    }

    /**
     * Sets the host client node.
     *
     * @param node The host client node.
     */
    public void setHostClientNode(final ClientNode node) {
        this.hostClientNode = node;
        logger.info("Host client node set to " + hostClientNode);
    }

    /**
     * Sets the self client node.
     *
     * @param node The self client node.
     */
    public void setSelfClientNode(final ClientNode node) {
        this.selfClientNode = node;
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

    private byte[] handleWhoAmI(final byte[] data) {
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

    private byte[] handleDescribe(final byte[] data) {
        try {
            // final String input = new String(data, StandardCharsets.UTF_8);
            final String input = DataSerializer.deserialize(data, String.class);

            logger.info("[CanvasManager] Canvas Describe requested for: " + input);

            final CompletableFuture<String> resp = AiInstance.getInstance().describe(input);

            // Use thenApply to transform the result and join to wait for completion
            return resp.thenApply(response -> response.getBytes(StandardCharsets.UTF_8))
                       .exceptionally(ex -> {
                           logger.error("Error during AI describe", ex);
                           return new byte[0];
                       })
                       .join();

        } catch (Exception e) {
            logger.error("Failed to handle describe request", e);
            return new byte[0];
        }
    }

    private byte[] handleRegularize(final byte[] data) {
        try {
            final String rootNode = DataSerializer.deserialize(data, String.class);
            logger.info("[CanvasManager] Regularizing canvas data: " + rootNode);

            final CompletableFuture<String> resp = AiInstance.getInstance().regularise(rootNode);

            // Use thenApply to transform the result, serialize it, and join to wait
            return resp.thenApply(response -> {
                logger.info("[CanvasManager] Regularize complete: " + response);
                try {
                    return DataSerializer.serialize(response);
                } catch (Exception e) {
                    throw new RuntimeException("Serialization failed inside future", e);
                }
            }).exceptionally(ex -> {
                logger.error("Error during AI regularize", ex);
                return new byte[0];
            }).join();

        } catch (Exception e) {
            logger.error("Failed to regularize canvas payload", e);
            return new byte[0];
        }
    }

    private byte[] handleSendToHost(final byte[] data) {
        try {
            if (hostClientNode == null) {
                logger.warn("Host client node is not set. Cannot send to host.");
                return new byte[0];
            }

            // Send the data to the Host Node using Networking.
            // The Host Node will receive it via 'handleNetworkMessage'.
            logger.info("Sending data to host " + hostClientNode);
            // Module 2, Priority 1
            networking.sendData(data, new ClientNode[]{hostClientNode}, ModuleType.CANVAS.ordinal(), 1);

            return new byte[0];
        } catch (Exception e) {
            logger.error("Failed to send data to host", e);
            return new byte[0];
        }
    }

    private void handleNetworkMessage(final byte[] data) {
        try {
            logger.debug("Everyone received broadcast from host");
            rpc.call("canvas:update", data);
        } catch (Exception e) {
            logger.error("Failed to handle network message", e);
        }
    }

    private byte[] handleBroadcast(final byte[] data) {
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

    private byte[] handleSendToClient(final byte[] data) {
        try {
            final JsonNode node = DataSerializer.deserialize(data, JsonNode.class);

            if (node == null || !node.has("target") || !node.has("data")) {
                logger.warn("Invalid unicast request received.");
                return new byte[0];
            }

            // Extract target ClientNode
            final JsonNode targetNode = node.get("target");
            final String hostName = targetNode.get("hostName").asText();
            final int port = targetNode.get("port").asInt();
            final ClientNode target = new ClientNode(hostName, port);

            // Extract data string
            final String payloadData = node.get("data").asText();

            logger.info("Sending unicast data to " + target);
            final byte[] payload = DataSerializer.serialize(payloadData);

            networking.sendData(payload, new ClientNode[]{target}, ModuleType.CANVAS.ordinal(), 1);

            return new byte[0];
        } catch (Exception e) {
            logger.error("Failed to send unicast data to client", e);
            return new byte[0];
        }
    }

}