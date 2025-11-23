package com.swe.canvas.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.canvas.model.CanvasAction;
import com.swe.canvas.model.CanvasNetworkMessage;
import com.swe.core.serialize.DataSerializer;

import java.util.List;

/**
 * Utility helper for serialising/deserialising canvas objects over RPC/network.
 */
public final class CanvasMessageSerializer {
    private CanvasMessageSerializer() {
    }

    public static byte[] serializeMessage(final CanvasNetworkMessage message) {
        try {
            return DataSerializer.serialize(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize canvas message", e);
        }
    }

    public static CanvasNetworkMessage deserializeMessage(final byte[] payload) {
        try {
            return DataSerializer.deserialize(payload, CanvasNetworkMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize canvas message", e);
        }
    }

    public static CanvasAction deserializeAction(final byte[] payload) {
        try {
            return DataSerializer.deserialize(payload, CanvasAction.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize canvas action", e);
        }
    }

    public static byte[] serializeAction(final CanvasAction action) {
        try {
            return DataSerializer.serialize(action);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize canvas action", e);
        }
    }

    public static byte[] serializeState(final List<CanvasAction> state) {
        try {
            return DataSerializer.serialize(state);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize canvas state", e);
        }
    }
}
