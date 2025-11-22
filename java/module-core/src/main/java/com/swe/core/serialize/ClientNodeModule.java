package com.swe.core.serialize;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.swe.core.ClientNode;

public class ClientNodeModule extends SimpleModule {
    public ClientNodeModule() {
        addKeySerializer(ClientNode.class, new ClientNodeKeySerializer());
        addKeyDeserializer(ClientNode.class, new ClientNodeKeyDeserializer());
    }
}
