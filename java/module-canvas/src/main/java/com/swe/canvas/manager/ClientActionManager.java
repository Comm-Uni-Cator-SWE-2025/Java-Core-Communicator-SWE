package com.swe.canvas.manager;

import com.swe.canvas.model.CanvasAction;
import com.swe.canvas.model.CanvasMessageType;
import com.swe.canvas.model.CanvasNetworkMessage;
import com.swe.canvas.state.CanvasState;
import com.swe.core.ClientNode;

/**
 * Handles action creation on the client.
 */
public class ClientActionManager {
    private final CanvasState canvasState;
    private final ClientNode localNode;

    public ClientActionManager(final CanvasState canvasState, final ClientNode localNode) {
        this.canvasState = canvasState;
        this.localNode = localNode;
    }

    public CanvasNetworkMessage prepareOutgoingMessage(final CanvasAction action) {
        if (action != null) {
            action.normalize(localNode != null ? localNode.hostName() : "");
        }
        return new CanvasNetworkMessage(CanvasMessageType.CLIENT_ACTION, action, localNode);
    }

    public void applyBroadcast(final CanvasNetworkMessage message) {
        if (message == null || message.getAction() == null) {
            return;
        }
        canvasState.applyAction(message.getAction());
    }
}
