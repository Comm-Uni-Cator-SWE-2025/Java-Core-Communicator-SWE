/**
 *  Contributed by Ram Charan.
 */

package com.swe.core.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import java.nio.charset.StandardCharsets;

public class DataSerializer {

    private static final SweLogger LOG = SweLoggerFactory.getLogger("CORE");

    static ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] serialize(Object participant) throws JsonProcessingException {
        LOG.debug("Serializing object of type " + (participant != null ? participant.getClass().getName() : "null"));
        objectMapper.registerModule(new ClientNodeModule());
        String data = objectMapper.writeValueAsString(participant);

        return data.getBytes(StandardCharsets.UTF_8);
    }

    public static <T> T deserialize(byte[] data, Class<T> datatype) throws JsonProcessingException {
        LOG.debug("Deserializing payload into type " + (datatype != null ? datatype.getName() : "unknown"));
        objectMapper.registerModule(new ClientNodeModule());
        String json = new String(data, StandardCharsets.UTF_8);
        LOG.trace("Deserialization payload: " + json);

        return objectMapper.readValue(json, datatype);
    }
}
