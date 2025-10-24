package com.swe.networking;

import java.util.HashMap;

public enum ModuleType {
    NETWORKING,
    SCREENSHARING,
    CANVAS,
    UIUX,
    CONTROLLER,
    AI,
    CLOUD,
    CHAT;

    private static final HashMap<Integer, ModuleType> map = new HashMap<>() {
        {
            put(0, NETWORKING);
            put(1, SCREENSHARING);
            put(2, CANVAS);
            put(3, UIUX);
            put(4, CONTROLLER);
            put(5, AI);
            put(6, CLOUD);
            put(7, CHAT);
        }
    };

    private static ModuleType moduleType;

    private ModuleType() {
    }

    public ModuleType getType(int n) {
        return map.get(n);
    }
}
