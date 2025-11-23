package com.swe.canvas.manager;

import com.swe.canvas.model.CanvasAction;
import com.swe.canvas.model.CanvasMessageType;
import com.swe.canvas.model.CanvasNetworkMessage;
import com.swe.canvas.state.CanvasState;
import com.swe.core.ClientNode;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates and applies actions received by the host.
 */
public class HostActionManager {
    private final CanvasState state;
    private final Set<String> processedActions = ConcurrentHashMap.newKeySet();

    public HostActionManager(final CanvasState state) {
        this.state = state;
    }

    public CanvasNetworkMessage processClientMessage(final CanvasNetworkMessage incoming) {
        if (incoming == null || incoming.getAction() == null) {
            return null;
        }

        final CanvasAction action = incoming.getAction();
        if (!processedActions.add(action.getActionId())) {
            return null;
        }

        action.normalize(action.getActorId());
        state.applyAction(action);
        return new CanvasNetworkMessage(CanvasMessageType.HOST_BROADCAST, action, incoming.getOrigin());
    }

    public CanvasNetworkMessage processHostAction(final CanvasAction action, final ClientNode hostNode) {
        if (action == null) {
            return null;
        }
        if (!processedActions.add(action.getActionId())) {
            return null;
        }
        action.normalize(hostNode != null ? hostNode.hostName() : "host");
        state.applyAction(action);
        return new CanvasNetworkMessage(CanvasMessageType.HOST_BROADCAST, action, hostNode);
    }
}
