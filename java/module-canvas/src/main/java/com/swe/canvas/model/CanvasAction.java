package com.swe.canvas.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single canvas action (create/update/delete) performed by a user.
 */
public class CanvasAction {
    private String actionId;
    private String actorId;
    private String actionType;
    private String payload;
    private long timestamp;

    public CanvasAction() {
        // default constructor for serializers
    }

    public CanvasAction(final String actionId,
                        final String actorId,
                        final String actionType,
                        final String payload,
                        final long timestamp) {
        this.actionId = actionId;
        this.actorId = actorId;
        this.actionType = actionType;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    /**
     * Ensures the action contains an ID and timestamp before it is broadcast.
     *
     * @param fallbackActorId default actor when none provided
     */
    public void normalize(final String fallbackActorId) {
        if (actionId == null || actionId.isBlank()) {
            actionId = UUID.randomUUID().toString();
        }
        if (actorId == null || actorId.isBlank()) {
            actorId = fallbackActorId;
        }
        if (timestamp == 0L) {
            timestamp = System.currentTimeMillis();
        }
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(final String actionId) {
        this.actionId = actionId;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(final String actorId) {
        this.actorId = actorId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(final String actionType) {
        this.actionType = actionType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(final String payload) {
        this.payload = payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CanvasAction that = (CanvasAction) o;
        return Objects.equals(actionId, that.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId);
    }
}
