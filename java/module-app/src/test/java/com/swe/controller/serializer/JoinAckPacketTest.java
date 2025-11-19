package com.swe.controller.serializer;

import com.swe.core.ClientNode;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for JoinAckPacket serialization and deserialization.
 */
public class JoinAckPacketTest {

    @org.junit.jupiter.api.Test
    public void testBasicRoundTrip() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("192.168.1.1", 8080), "user1@example.com");
        map.put(new ClientNode("192.168.1.2", 8081), "user2@example.com");

        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();
        final JoinAckPacket deserialized = JoinAckPacket.deserialize(serialized);

        final Map<ClientNode, String> deserializedMap = deserialized.getNodeToEmailMap();
        assertEquals(2, deserializedMap.size());
        assertEquals("user1@example.com", deserializedMap.get(new ClientNode("192.168.1.1", 8080)));
        assertEquals("user2@example.com", deserializedMap.get(new ClientNode("192.168.1.2", 8081)));
    }

    @org.junit.jupiter.api.Test
    public void testEmptyMap() {
        final Map<ClientNode, String> map = new HashMap<>();
        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();
        final JoinAckPacket deserialized = JoinAckPacket.deserialize(serialized);

        assertEquals(0, deserialized.getNodeToEmailMap().size());
    }

    @org.junit.jupiter.api.Test
    public void testSingleEntry() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("127.0.0.1", 6942), "single@test.com");

        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();
        final JoinAckPacket deserialized = JoinAckPacket.deserialize(serialized);

        final Map<ClientNode, String> deserializedMap = deserialized.getNodeToEmailMap();
        assertEquals(1, deserializedMap.size());
        assertEquals("single@test.com", deserializedMap.get(new ClientNode("127.0.0.1", 6942)));
    }

    @org.junit.jupiter.api.Test
    public void testManyEntries() {
        final Map<ClientNode, String> map = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            map.put(new ClientNode("192.168.1." + (i + 1), 8000 + i), "user" + i + "@example.com");
        }

        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();
        final JoinAckPacket deserialized = JoinAckPacket.deserialize(serialized);

        final Map<ClientNode, String> deserializedMap = deserialized.getNodeToEmailMap();
        assertEquals(100, deserializedMap.size());
        for (int i = 0; i < 100; i++) {
            final ClientNode node = new ClientNode("192.168.1." + (i + 1), 8000 + i);
            assertEquals("user" + i + "@example.com", deserializedMap.get(node));
        }
    }

    @org.junit.jupiter.api.Test
    public void testEmptyEmails() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("192.168.1.1", 8080), "");
        map.put(new ClientNode("192.168.1.2", 8081), "");

        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();
        final JoinAckPacket deserialized = JoinAckPacket.deserialize(serialized);

        final Map<ClientNode, String> deserializedMap = deserialized.getNodeToEmailMap();
        assertEquals(2, deserializedMap.size());
        assertEquals("", deserializedMap.get(new ClientNode("192.168.1.1", 8080)));
        assertEquals("", deserializedMap.get(new ClientNode("192.168.1.2", 8081)));
    }

    @org.junit.jupiter.api.Test
    public void testVeryLongEmails() {
        final Map<ClientNode, String> map = new HashMap<>();
        final String longEmail = "a".repeat(500) + "@example.com";
        map.put(new ClientNode("192.168.1.1", 8080), longEmail);

        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();
        final JoinAckPacket deserialized = JoinAckPacket.deserialize(serialized);

        final Map<ClientNode, String> deserializedMap = deserialized.getNodeToEmailMap();
        assertEquals(longEmail, deserializedMap.get(new ClientNode("192.168.1.1", 8080)));
    }

    @org.junit.jupiter.api.Test
    public void testUnicodeEmails() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("192.168.1.1", 8080), "tëst@exämple.com");
        map.put(new ClientNode("192.168.1.2", 8081), "用户@例子.中国");

        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();
        final JoinAckPacket deserialized = JoinAckPacket.deserialize(serialized);

        final Map<ClientNode, String> deserializedMap = deserialized.getNodeToEmailMap();
        assertEquals("tëst@exämple.com", deserializedMap.get(new ClientNode("192.168.1.1", 8080)));
        assertEquals("用户@例子.中国", deserializedMap.get(new ClientNode("192.168.1.2", 8081)));
    }

    @org.junit.jupiter.api.Test
    public void testDifferentPorts() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("192.168.1.1", 0), "user1@test.com");
        map.put(new ClientNode("192.168.1.1", 65535), "user2@test.com");
        map.put(new ClientNode("192.168.1.1", 8080), "user3@test.com");

        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();
        final JoinAckPacket deserialized = JoinAckPacket.deserialize(serialized);

        final Map<ClientNode, String> deserializedMap = deserialized.getNodeToEmailMap();
        assertEquals(3, deserializedMap.size());
        assertEquals("user1@test.com", deserializedMap.get(new ClientNode("192.168.1.1", 0)));
        assertEquals("user2@test.com", deserializedMap.get(new ClientNode("192.168.1.1", 65535)));
        assertEquals("user3@test.com", deserializedMap.get(new ClientNode("192.168.1.1", 8080)));
    }

    @org.junit.jupiter.api.Test
    public void testDifferentIPAddresses() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("0.0.0.0", 8080), "user1@test.com");
        map.put(new ClientNode("255.255.255.255", 8080), "user2@test.com");
        map.put(new ClientNode("10.0.0.1", 8080), "user3@test.com");
        map.put(new ClientNode("172.16.0.1", 8080), "user4@test.com");
        map.put(new ClientNode("192.168.1.1", 8080), "user5@test.com");

        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();
        final JoinAckPacket deserialized = JoinAckPacket.deserialize(serialized);

        final Map<ClientNode, String> deserializedMap = deserialized.getNodeToEmailMap();
        assertEquals(5, deserializedMap.size());
        assertEquals("user1@test.com", deserializedMap.get(new ClientNode("0.0.0.0", 8080)));
        assertEquals("user5@test.com", deserializedMap.get(new ClientNode("192.168.1.1", 8080)));
    }

    @org.junit.jupiter.api.Test
    public void testNullMap() {
        assertThrows(IllegalArgumentException.class, () -> {
            new JoinAckPacket(null);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeNullData() {
        assertThrows(InvalidParameterException.class, () -> {
            JoinAckPacket.deserialize(null);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeTooShort() {
        final byte[] shortData = new byte[]{0, 1, 2, 3}; // Only 4 bytes, need at least 5
        assertThrows(InvalidParameterException.class, () -> {
            JoinAckPacket.deserialize(shortData);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeWrongPacketType() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("192.168.1.1", 8080), "user@example.com");
        final JoinAckPacket original = new JoinAckPacket(map);
        final byte[] serialized = original.serialize();

        // Corrupt the packet type byte
        serialized[0] = (byte) 99; // Invalid packet type

        assertThrows(InvalidParameterException.class, () -> {
            JoinAckPacket.deserialize(serialized);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeNegativeEntryCount() {
        final byte[] data = new byte[10];
        data[0] = (byte) MeetingPacketType.JOINACK.ordinal();
        // Set negative entry count (will be interpreted as large positive number)
        data[1] = (byte) 0xFF;
        data[2] = (byte) 0xFF;
        data[3] = (byte) 0xFF;
        data[4] = (byte) 0xFF;

        // This should fail during validation
        assertThrows(InvalidParameterException.class, () -> {
            JoinAckPacket.deserialize(data);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeInsufficientIPData() {
        final byte[] data = new byte[6];
        data[0] = (byte) MeetingPacketType.JOINACK.ordinal();
        data[1] = 0;
        data[2] = 0;
        data[3] = 0;
        data[4] = 1; // 1 entry
        // Missing IP bytes

        assertThrows(InvalidParameterException.class, () -> {
            JoinAckPacket.deserialize(data);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeInsufficientPortData() {
        final byte[] data = new byte[10];
        data[0] = (byte) MeetingPacketType.JOINACK.ordinal();
        data[1] = 0;
        data[2] = 0;
        data[3] = 0;
        data[4] = 1; // 1 entry
        data[5] = (byte) 192;
        data[6] = (byte) 168;
        data[7] = (byte) 1;
        data[8] = (byte) 1;
        // Missing port bytes

        assertThrows(InvalidParameterException.class, () -> {
            JoinAckPacket.deserialize(data);
        });
    }

    @org.junit.jupiter.api.Test
    public void testDeserializeInvalidEmailLength() {
        final byte[] data = new byte[15];
        data[0] = (byte) MeetingPacketType.JOINACK.ordinal();
        data[1] = 0;
        data[2] = 0;
        data[3] = 0;
        data[4] = 2; // 2 entries
        // First entry: IP + port
        data[5] = (byte) 192;
        data[6] = (byte) 168;
        data[7] = (byte) 1;
        data[8] = (byte) 1;
        data[9] = 0x1F; // port high byte
        data[10] = (byte) 0x90; // port low byte (8080)
        // Invalid email length (larger than remaining)
        data[11] = (byte) 0xFF;
        data[12] = (byte) 0xFF;
        data[13] = (byte) 0xFF;
        data[14] = (byte) 0xFF;

        assertThrows(InvalidParameterException.class, () -> {
            JoinAckPacket.deserialize(data);
        });
    }

    @org.junit.jupiter.api.Test
    public void testMinimumSizeEmptyMap() {
        final Map<ClientNode, String> map = new HashMap<>();
        final JoinAckPacket packet = new JoinAckPacket(map);
        final byte[] serialized = packet.serialize();

        // Minimum: 1 (type) + 4 (entry count = 0) = 5 bytes
        assertEquals(5, serialized.length);
    }

    @org.junit.jupiter.api.Test
    public void testSizeSingleEntry() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("192.168.1.1", 8080), "");
        final JoinAckPacket packet = new JoinAckPacket(map);
        final byte[] serialized = packet.serialize();

        // 1 (type) + 4 (count) + 4 (IP) + 2 (port) + 0 (email, no length prefix for last) = 11 bytes
        assertEquals(11, serialized.length);
    }

    @org.junit.jupiter.api.Test
    public void testSizeTwoEntries() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("192.168.1.1", 8080), "user1@test.com");
        map.put(new ClientNode("192.168.1.2", 8081), "user2@test.com");
        final JoinAckPacket packet = new JoinAckPacket(map);
        final byte[] serialized = packet.serialize();

        // 1 (type) + 4 (count) + 
        // Entry 1: 4 (IP) + 2 (port) + 4 (email len) + 13 (email) +
        // Entry 2: 4 (IP) + 2 (port) + 13 (email, no length prefix)
        final int expectedSize = 1 + 4 + (4 + 2 + 4 + 13) + (4 + 2 + 13);
        assertEquals(expectedSize, serialized.length);
    }

    @org.junit.jupiter.api.Test
    public void testMultipleRoundTrips() {
        final Map<ClientNode, String> map = new HashMap<>();
        map.put(new ClientNode("192.168.1.1", 8080), "user1@example.com");
        map.put(new ClientNode("192.168.1.2", 8081), "user2@example.com");
        map.put(new ClientNode("192.168.1.3", 8082), "user3@example.com");

        JoinAckPacket packet = new JoinAckPacket(map);

        // Perform 5 round-trips
        for (int i = 0; i < 5; i++) {
            final byte[] serialized = packet.serialize();
            packet = JoinAckPacket.deserialize(serialized);
        }

        final Map<ClientNode, String> finalMap = packet.getNodeToEmailMap();
        assertEquals(3, finalMap.size());
        assertEquals("user1@example.com", finalMap.get(new ClientNode("192.168.1.1", 8080)));
        assertEquals("user2@example.com", finalMap.get(new ClientNode("192.168.1.2", 8081)));
        assertEquals("user3@example.com", finalMap.get(new ClientNode("192.168.1.3", 8082)));
    }

    @org.junit.jupiter.api.Test
    public void testGetNodeToEmailMapReturnsCopy() {
        final Map<ClientNode, String> originalMap = new HashMap<>();
        originalMap.put(new ClientNode("192.168.1.1", 8080), "user@example.com");
        final JoinAckPacket packet = new JoinAckPacket(originalMap);

        final Map<ClientNode, String> map1 = packet.getNodeToEmailMap();
        final Map<ClientNode, String> map2 = packet.getNodeToEmailMap();

        // Modifying one should not affect the other
        map1.put(new ClientNode("192.168.1.2", 8081), "user2@example.com");
        assertEquals(1, map2.size());
        assertEquals(2, map1.size());
    }

    @org.junit.jupiter.api.Test
    public void testConstructorDefensiveCopy() {
        final Map<ClientNode, String> originalMap = new HashMap<>();
        originalMap.put(new ClientNode("192.168.1.1", 8080), "user@example.com");
        final JoinAckPacket packet = new JoinAckPacket(originalMap);

        // Modify original map
        originalMap.put(new ClientNode("192.168.1.2", 8081), "user2@example.com");

        // Packet should not be affected
        final Map<ClientNode, String> packetMap = packet.getNodeToEmailMap();
        assertEquals(1, packetMap.size());
        assertEquals(2, originalMap.size());
    }
}

