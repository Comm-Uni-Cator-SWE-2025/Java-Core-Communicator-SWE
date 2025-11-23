package com.swe.core.serialize;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.swe.core.ClientNode;

import java.io.IOException;

public class ClientNodeKeySerializer extends JsonSerializer<ClientNode> {

    @Override
    public void serialize(ClientNode value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        // convert key to a single string (your choice)
        gen.writeFieldName(value.hostName() + ":" + value.port());
    }
}
