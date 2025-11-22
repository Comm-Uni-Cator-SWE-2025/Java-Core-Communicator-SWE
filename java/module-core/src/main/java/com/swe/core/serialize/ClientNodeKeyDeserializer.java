package com.swe.core.serialize;

import com.fasterxml.jackson.databind.KeyDeserializer;
import com.swe.core.ClientNode;

public class ClientNodeKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, com.fasterxml.jackson.databind.DeserializationContext ctxt) {
        String[] parts = key.split(":");
        return new ClientNode(parts[0], Integer.parseInt(parts[1]));
    }
}
