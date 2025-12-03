package com.swe.core.serialize;

import com.fasterxml.jackson.databind.KeyDeserializer;
import com.swe.core.ClientNode;

public class ClientNodeKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(final String key, final com.fasterxml.jackson.databind.DeserializationContext ctxt) {
        final String[] parts = key.split(":");
        return new ClientNode(parts[0], Integer.parseInt(parts[1]));
    }
}
