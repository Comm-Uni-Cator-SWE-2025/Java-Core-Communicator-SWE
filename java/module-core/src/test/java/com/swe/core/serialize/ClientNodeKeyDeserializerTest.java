package com.swe.core.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.swe.core.ClientNode;
import org.junit.Test;

public class ClientNodeKeyDeserializerTest {

    private final ClientNodeKeyDeserializer deserializer = new ClientNodeKeyDeserializer();

    @Test
    public void deserializeKeyWithValidFormat() throws Exception {
        final String key = "127.0.0.1:8080";
        final Object result = deserializer.deserializeKey(key, null);

        assertNotNull(result);
        assertTrue(result instanceof ClientNode);
        final ClientNode node = (ClientNode) result;
        assertEquals("127.0.0.1", node.hostName());
        assertEquals(8080, node.port());
    }

    @Test
    public void deserializeKeyWithDifferentHostAndPort() throws Exception {
        final String key = "192.168.1.1:9090";
        final Object result = deserializer.deserializeKey(key, null);

        assertNotNull(result);
        final ClientNode node = (ClientNode) result;
        assertEquals("192.168.1.1", node.hostName());
        assertEquals(9090, node.port());
    }

    @Test
    public void deserializeKeyWithLocalhost() throws Exception {
        final String key = "localhost:3000";
        final Object result = deserializer.deserializeKey(key, null);

        assertNotNull(result);
        final ClientNode node = (ClientNode) result;
        assertEquals("localhost", node.hostName());
        assertEquals(3000, node.port());
    }

    @Test
    public void deserializeKeyWithZeroPort() throws Exception {
        final String key = "host:0";
        final Object result = deserializer.deserializeKey(key, null);

        assertNotNull(result);
        final ClientNode node = (ClientNode) result;
        assertEquals("host", node.hostName());
        assertEquals(0, node.port());
    }

    @Test
    public void deserializeKeyWithLargePort() throws Exception {
        final String key = "host:65535";
        final Object result = deserializer.deserializeKey(key, null);

        assertNotNull(result);
        final ClientNode node = (ClientNode) result;
        assertEquals("host", node.hostName());
        assertEquals(65535, node.port());
    }
}

