package com.swe.core.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

public class DataSerializer {

    static ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] serialize(Object participant) throws JsonProcessingException {
        System.out.println("serializing");
        objectMapper.registerModule(new ClientNodeModule());
        String data = objectMapper.writeValueAsString(participant);

        return data.getBytes(StandardCharsets.UTF_8);
    }

    public static <T> T deserialize(byte[] data, Class<T> datatype) throws JsonProcessingException {
        System.out.println("deserializing 1");
        String json = new String(data, StandardCharsets.UTF_8);
        System.out.println("deserializing 2");

        return objectMapper.readValue(json, datatype);
    }
}
