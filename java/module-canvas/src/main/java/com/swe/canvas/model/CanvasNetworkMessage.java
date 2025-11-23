package com.swe.canvas.model;

import com.swe.core.ClientNode;

/**
 * Wrapper sent through the networking layer.
 */
public class CanvasNetworkMessage {
    private CanvasMessageType type;
    private CanvasAction action;
    private ClientNode origin;

    public CanvasNetworkMessage() {
    }

    public CanvasNetworkMessage(final CanvasMessageType type,
                                final CanvasAction action,
                                final ClientNode origin) {
        this.type = type;
        this.action = action;
        this.origin = origin;
    }

    public CanvasMessageType getType() {
        return type;
    }

    public void setType(final CanvasMessageType type) {
        this.type = type;
    }

    public CanvasAction getAction() {
        return action;
    }

    public void setAction(final CanvasAction action) {
        this.action = action;
    }

    public ClientNode getOrigin() {
        return origin;
    }

    public void setOrigin(final ClientNode origin) {
        this.origin = origin;
    }
}
