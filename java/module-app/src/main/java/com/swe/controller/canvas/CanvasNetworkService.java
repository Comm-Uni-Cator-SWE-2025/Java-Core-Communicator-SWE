package com.swe.controller.canvas;

import com.swe.canvas.ICanvasService;
import com.swe.canvas.manager.ClientActionManager;
import com.swe.canvas.manager.HostActionManager;
import com.swe.canvas.model.CanvasAction;
import com.swe.canvas.model.CanvasMessageType;
import com.swe.canvas.model.CanvasNetworkMessage;
import com.swe.canvas.serialization.CanvasMessageSerializer;
import com.swe.canvas.state.CanvasState;
import com.swe.controller.NetworkingInterface;
import com.swe.core.ClientNode;
import com.swe.core.Meeting.MeetingSession;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.MessageListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Bridges RPC calls coming from the frontend with the networking layer so canvas actions
 * flow between host and students.
 */
public class CanvasNetworkService implements ICanvasService, MessageListener {
    private static final long HOST_LOOKUP_TIMEOUT_MS = 3000L;

    private final AbstractRPC rpc;
    private final NetworkingInterface networking;
    private final boolean isHost;
    private final ClientNode localNode;
    private ClientNode hostNode;
    private final Supplier<MeetingSession> meetingSessionSupplier;
    private final CanvasState canvasState;
    private final ClientActionManager clientActionManager;
    private final HostActionManager hostActionManager;

    public CanvasNetworkService(final AbstractRPC rpc,
                                final NetworkingInterface networking,
                                final boolean isHost,
                                final ClientNode localNode,
                                final ClientNode initialHostNode,
                                final Supplier<MeetingSession> meetingSessionSupplier) {
        this.rpc = rpc;
        this.networking = networking;
        this.isHost = isHost;
        this.localNode = localNode;
        this.hostNode = initialHostNode;
        this.meetingSessionSupplier = meetingSessionSupplier;
        this.canvasState = new CanvasState();
        this.clientActionManager = new ClientActionManager(canvasState, localNode);
        this.hostActionManager = isHost ? new HostActionManager(canvasState) : null;

        this.networking.subscribe(ModuleType.CANVAS, this);
        registerRpcEndpoints();
        ensureHostNode();
    }

    private void registerRpcEndpoints() {
        rpc.subscribe("canvas:submitAction", this::submitAction);
        rpc.subscribe("canvas:getCanvasState", this::getCanvasState);
    }

    @Override
    public void receiveData(final byte[] data) {
        final CanvasNetworkMessage message = CanvasMessageSerializer.deserializeMessage(data);
        handleNetworkPayload(message);
    }

    private void handleNetworkPayload(final CanvasNetworkMessage message) {
        if (message == null || message.getAction() == null) {
            return;
        }

        if (isHost) {
            final CanvasNetworkMessage validated = hostActionManager.processClientMessage(message);
            if (validated != null) {
                broadcastValidatedAction(validated);
            }
            return;
        }

        if (message.getType() == CanvasMessageType.HOST_BROADCAST) {
            clientActionManager.applyBroadcast(message);
        }
    }

    @Override
    public byte[] submitAction(final byte[] payload) {
        final CanvasAction action = CanvasMessageSerializer.deserializeAction(payload);
        if (action == null) {
            return new byte[0];
        }

        if (isHost) {
            final CanvasNetworkMessage broadcast = hostActionManager.processHostAction(action, localNode);
            if (broadcast != null) {
                broadcastValidatedAction(broadcast);
                return CanvasMessageSerializer.serializeMessage(broadcast);
            }
            return new byte[0];
        }

        final CanvasNetworkMessage outgoing = clientActionManager.prepareOutgoingMessage(action);
        sendToHost(outgoing);
        return CanvasMessageSerializer.serializeMessage(outgoing);
    }

    private void sendToHost(final CanvasNetworkMessage message) {
        if (hostNode == null) {
            ensureHostNode();
        }
        if (hostNode == null) {
            System.out.println("[CANVAS] Host node unavailable, dropping action");
            return;
        }
        final ClientNode[] dest = new ClientNode[] { hostNode };
        networking.sendData(CanvasMessageSerializer.serializeMessage(message), dest, ModuleType.CANVAS, 0);
    }

    private void broadcastValidatedAction(final CanvasNetworkMessage message) {
        final ClientNode[] recipients = resolveRecipients();
        if (recipients.length == 0) {
            return;
        }
        networking.sendData(CanvasMessageSerializer.serializeMessage(message), recipients, ModuleType.CANVAS, 0);
    }

    private ClientNode[] resolveRecipients() {
        final MeetingSession meeting = meetingSessionSupplier.get();
        if (meeting == null || meeting.getParticipants().isEmpty()) {
            return new ClientNode[0];
        }
        final List<ClientNode> nodes = new ArrayList<>();
        for (ClientNode node : meeting.getParticipants().keySet()) {
            if (node != null && !node.equals(localNode)) {
                nodes.add(node);
            }
        }
        return nodes.toArray(new ClientNode[0]);
    }

    private void ensureHostNode() {
        if (isHost) {
            this.hostNode = localNode;
            return;
        }
        final Optional<ClientNode> response = requestHostNode();
        response.ifPresent(node -> this.hostNode = node);
    }

    private Optional<ClientNode> requestHostNode() {
        try {
            final byte[] bytes = rpc.call("canvas:getHostIp", new byte[0])
                    .get(HOST_LOOKUP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (bytes == null || bytes.length == 0) {
                return Optional.empty();
            }
            final String hostString = new String(bytes, StandardCharsets.UTF_8);
            final String[] parts = hostString.split(":");
            if (parts.length != 2) {
                return Optional.empty();
            }
            final int port = Integer.parseInt(parts[1]);
            return Optional.of(new ClientNode(parts[0], port));
        } catch (Exception e) {
            System.out.println("[CANVAS] Failed to resolve host IP: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public byte[] getCanvasState(final byte[] ignored) {
        return CanvasMessageSerializer.serializeState(canvasState.snapshot());
    }
}
