package com.swe.core.serialize;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.swe.core.ClientNode;
import java.io.StringWriter;
import org.junit.Before;
import org.junit.Test;

public class ClientNodeKeySerializerTest {

    private ClientNodeKeySerializer serializer;
    private JsonGenerator jsonGenerator;
    private SerializerProvider serializerProvider;
    private StringWriter stringWriter;

    @Before
    public void setUp() throws Exception {
        serializer = new ClientNodeKeySerializer();
        stringWriter = new StringWriter();
        jsonGenerator = new com.fasterxml.jackson.core.JsonFactory().createGenerator(stringWriter);
        serializerProvider = null; // Not used in serialize method
    }

    @Test
    public void serializeWritesHostNameAndPort() throws Exception {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        jsonGenerator.writeStartObject();
        serializer.serialize(node, jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
        jsonGenerator.close();

        final String result = stringWriter.toString();
        assertNotNull(result);
        // The serializer writes field name, so we check the pattern
        assertTrue(result.contains("127.0.0.1:8080"));
    }

    @Test
    public void serializeWithDifferentHostAndPort() throws Exception {
        final ClientNode node = new ClientNode("192.168.1.1", 9090);
        jsonGenerator.writeStartObject();
        serializer.serialize(node, jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
        jsonGenerator.close();

        final String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.contains("192.168.1.1:9090"));
    }

    @Test
    public void serializeWithLocalhost() throws Exception {
        final ClientNode node = new ClientNode("localhost", 3000);
        jsonGenerator.writeStartObject();
        serializer.serialize(node, jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
        jsonGenerator.close();

        final String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.contains("localhost:3000"));
    }

    @Test
    public void serializeWithZeroPort() throws Exception {
        final ClientNode node = new ClientNode("host", 0);
        jsonGenerator.writeStartObject();
        serializer.serialize(node, jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
        jsonGenerator.close();

        final String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.contains("host:0"));
    }
}

