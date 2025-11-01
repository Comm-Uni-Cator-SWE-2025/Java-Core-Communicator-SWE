package com.swe.networking;

import java.util.HashMap;

/**
 * Enum type for Module Priority.
 */
public enum ModulePriority {
    NETWORKING,
    SCREENSHARING,
    CANVAS,
    UIUX,
    CONTROLLER,
    AI,
    CLOUD,
    CHAT;

    /**
     * Enum type for Module priority.
     * 0 - Highest priority
     * 7 - Lowest priority
     */
    private static final HashMap<Integer, ModulePriority> MAP = new HashMap<>() {
        {
            put(0, NETWORKING);
            put(1, SCREENSHARING);
            put(2, CHAT);
            put(3, CANVAS);
            put(4, AI);
            put(5, CLOUD);
            put(6, CONTROLLER);
            put(7, UIUX);
        }
    };

    private static ModulePriority modulePriority;

    private ModulePriority() {
    }

    public ModulePriority getType(final int n) {
        return MAP.get(n);
    }
}
