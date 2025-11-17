package com.swe.controller.serializer;

import com.swe.core.ClientNode;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for AnnouncePacket serialization and deserialization.
 */
public class AnnouncePacketTest {

    @org.junit.jupiter.api.Test
    public void testBasicRoundTrip() {
        final String email = "user@example.com";
        final ClientNode node = new ClientNode("192.168.1.1", 8080);
        final AnnouncePacket original = new AnnouncePacket(email, node);

        final byte[] serialized = original.serialize();
        final AnnouncePacket deserialized = AnnouncePacket.deserialize(serialized);

        assertEquals(email, deserialized.getEmail());
        assertEquals(node.hostName(), deserialized.getClientNode().hostName());
        assertEquals(node.port(), deserialized.getClientNode().port());
    }

    @org.junit.jupiter.api.Test
    public void testLocalhostRoundTrip() {
        final String email = "test@localhost";
        final ClientNode node = new ClientNode("127.0.0.1", 6942);
        final AnnouncePacket original = new AnnouncePacket(email, node);

        final byte[] serialized = original.serialize();
        final AnnouncePacket deserialized = AnnouncePacket.deserialize(serialized);

        assertEquals(email, deserialized.getEmail());
        assertEquals("127.0.0.1", deserialized.getClientNode().hostName());
        assertEquals(6942, deserialized.getClientNode().port());
    }

    @org.junit.jupiter.api.Test
    public void testMinimumPort() {
        final String email = "user@test.com";
        final ClientNode node = new ClientNode("10.0.0.1", 0);
        final AnnouncePacket original = new AnnouncePacket(email, node);

        final byte[] serialized = original.serialize();
        final AnnouncePacket deserialized = AnnouncePacket.deserialize(serialized);

        assertEquals(email, deserialized.getEmail());
        assertEquals(0, deserialized.getClientNode().port());
    }

    @org.junit.jupiter.api.Test
    public void testMaximumPort() {
        final String email = "user@test.com";
        final ClientNode node = new ClientNode("172.16.0.1", 65535);
        final AnnouncePacket original = new AnnouncePacket(email, node);

        final byte[] serialized = original.serialize();
        final AnnouncePacket deserialized = AnnouncePacket.deserialize(serialized);

        assertEquals(email, deserialized.getEmail());
        assertEquals(65535, deserialized.getClientNode().port());
    }

    @org.junit.jupiter.api.Test
    public void testEmptyEmail() {
        final String email = "";
        final ClientNode node = new ClientNode("192.168.1.1", 8080);
        final AnnouncePacket original = new AnnouncePacket(email, node);

        final byte[] serialized = original.serialize();
        final AnnouncePacket deserialized = AnnouncePacket.deserialize(serialized);

        assertEquals("", deserialized.getEmail());
        assertEquals(node.hostName(), deserialized.getClientNode().hostName());
        assertEquals(node.port(), deserialized.getClientNode().port());
    }

    @org.junit.jupiter.api.Test
    public void testVeryLongEmail() {
        final String email = "a".repeat(1000) + "@example.com";
        final ClientNode node = new ClientNode("192.168.1.1", 8080);
        final AnnouncePacket original = new AnnouncePacket(email, node);

        final byte[] serialized = original.serialize();
        final AnnouncePacket deserialized = AnnouncePacket.deserialize(serialized);

        assertEquals(email, deserialized.getEmail());
    }

    @org.junit.jupiter.api.Test
    public void testEmailWithSpecialCharacters() {
        final String email = "user+tag@example.co.uk";
        final ClientNode node = new ClientNode("192.168.1.1", 8080);
        final AnnouncePacket original = new AnnouncePacket(email, node);

        final byte[] serialized = original.serialize();
        final AnnouncePacket deserialized = AnnouncePacket.deserialize(serialized);

        assertEquals(email, deserialized.getEmail());
    }

    @org.junit.jupiter.api.Test
    public void testEmailWithUnicode() {
        final String email = "tëst@exämple.com";
        final ClientNode node = new ClientNode("192.168.1.1", 8080);
        final AnnouncePacket original = new AnnouncePacket(email, node);

        final byte[] serialized = original.serialize();
        final AnnouncePacket deserialized = AnnouncePacket.deserialize(serialized);

        assertEquals(email, deserialized.getEmail());
    }

    @org.junit.jupiter.api.Test
    public void testNullEmail() {
        final ClientNode node = new ClientNode("192.168.1.1", 8080);
        assertThrows(IllegalArgumentException.class, () -> {
            new AnnouncePacket(null, node);
        });
    }

    @org.junit.jupiter.api.Test
    public void testNullClientNode() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AnnouncePacket("user@example.com", null);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeNullData() {
        assertThrows(InvalidParameterException.class, () -> {
            AnnouncePacket.deserialize(null);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeTooShort() {
        final byte[] shortData = new byte[]{0, 1, 2, 3, 4, 5}; // Only 6 bytes, need at least 7
        assertThrows(InvalidParameterException.class, () -> {
            AnnouncePacket.deserialize(shortData);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeWrongPacketType() {
        final String email = "user@example.com";
        final ClientNode node = new ClientNode("192.168.1.1", 8080);
        final AnnouncePacket original = new AnnouncePacket(email, node);
        final byte[] serialized = original.serialize();

        // Corrupt the packet type byte
        serialized[0] = (byte) 99; // Invalid packet type

        assertThrows(InvalidParameterException.class, () -> {
            AnnouncePacket.deserialize(serialized);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeInsufficientPortData() {
        final byte[] data = new byte[6]; // Only 6 bytes: packet type (1) + IP (4) + missing port
        data[0] = (byte) MeetingPacketType.ANNOUNCE.ordinal();
        data[1] = (byte) 192;
        data[2] = (byte) 168;
        data[3] = (byte) 1;
        data[4] = (byte) 1;
        // Missing port bytes

        assertThrows(InvalidParameterException.class, () -> {
            AnnouncePacket.deserialize(data);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDifferentIPAddresses() {
        final String[] ips = {"0.0.0.0", "255.255.255.255", "10.0.0.1", "172.16.0.1", "192.168.1.1"};
        final String email = "user@example.com";

        for (final String ip : ips) {
            final ClientNode node = new ClientNode(ip, 8080);
            final AnnouncePacket original = new AnnouncePacket(email, node);
            final byte[] serialized = original.serialize();
            final AnnouncePacket deserialized = AnnouncePacket.deserialize(serialized);

            assertEquals(ip, deserialized.getClientNode().hostName());
        }
    }

    @org.junit.jupiter.api.Test
    public void testMinimumSize() {
        final String email = ""; // Empty email
        final ClientNode node = new ClientNode("0.0.0.0", 0);
        final AnnouncePacket packet = new AnnouncePacket(email, node);
        final byte[] serialized = packet.serialize();

        // Minimum: 1 (type) + 4 (IP) + 2 (port) + 0 (empty email) = 7 bytes
        assertEquals(7, serialized.length);
    }

    @org.junit.jupiter.api.Test
    public void testMultipleRoundTrips() {
        final String email = "user@example.com";
        final ClientNode node = new ClientNode("192.168.1.1", 8080);
        AnnouncePacket packet = new AnnouncePacket(email, node);

        // Perform 10 round-trips
        for (int i = 0; i < 10; i++) {
            final byte[] serialized = packet.serialize();
            packet = AnnouncePacket.deserialize(serialized);
        }

        assertEquals(email, packet.getEmail());
        assertEquals(node.hostName(), packet.getClientNode().hostName());
        assertEquals(node.port(), packet.getClientNode().port());
    }
}

