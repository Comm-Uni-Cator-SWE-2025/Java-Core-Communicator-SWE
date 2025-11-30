package com.swe.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ClientNodeTest {

    @Test
    public void recordStoresHostNameAndPort() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        assertEquals("127.0.0.1", node.hostName());
        assertEquals(8080, node.port());
    }

    @Test
    public void equalsReturnsTrueForSameValues() {
        final ClientNode node1 = new ClientNode("127.0.0.1", 8080);
        final ClientNode node2 = new ClientNode("127.0.0.1", 8080);
        assertEquals(node1, node2);
    }

    @Test
    public void equalsReturnsFalseForDifferentValues() {
        final ClientNode node1 = new ClientNode("127.0.0.1", 8080);
        final ClientNode node2 = new ClientNode("127.0.0.1", 8081);
        assertNotEquals(node1, node2);
    }

    @Test
    public void hashCodeIsConsistent() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        final int hashCode1 = node.hashCode();
        final int hashCode2 = node.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void hashCodeIsSameForEqualObjects() {
        final ClientNode node1 = new ClientNode("127.0.0.1", 8080);
        final ClientNode node2 = new ClientNode("127.0.0.1", 8080);
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    public void hashCodeUsesCustomImplementation() {
        final ClientNode node1 = new ClientNode("host", 123);
        final ClientNode node2 = new ClientNode("host", 123);
        final String expected = "host123";
        assertEquals(expected.hashCode(), node1.hashCode());
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    public void differentHostNamesProduceDifferentHashCodes() {
        final ClientNode node1 = new ClientNode("host1", 8080);
        final ClientNode node2 = new ClientNode("host2", 8080);
        assertNotEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    public void differentPortsProduceDifferentHashCodes() {
        final ClientNode node1 = new ClientNode("127.0.0.1", 8080);
        final ClientNode node2 = new ClientNode("127.0.0.1", 8081);
        assertNotEquals(node1.hashCode(), node2.hashCode());
    }
}

