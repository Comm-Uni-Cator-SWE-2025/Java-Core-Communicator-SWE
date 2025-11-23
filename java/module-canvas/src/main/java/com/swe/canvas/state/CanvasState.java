package com.swe.canvas.state;

import com.swe.canvas.model.CanvasAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory representation of the collaborative canvas.
 */
public class CanvasState {
    private final Map<String, CanvasAction> actions = new ConcurrentHashMap<>();

    public void applyAction(final CanvasAction action) {
        if (action == null || action.getActionId() == null) {
            return;
        }
        actions.put(action.getActionId(), action);
    }

    public List<CanvasAction> snapshot() {
        return new ArrayList<>(actions.values());
    }
}
