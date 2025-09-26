package com.swe.networking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class PacketParserTest {
    @Test
    public void testTypeExtraction() {
        byte[] pkt = PacketParser.createPkt(2, 0, 0, 0, 0, new byte[0]);
        assertEquals(2, PacketParser.getType(pkt));
    }

    @Test
    public void testPriorityExtraction() {
        byte[] pkt = PacketParser.createPkt(0, 5, 0, 0, 0, new byte[0]);
        assertEquals(5, PacketParser.getPriority(pkt));
    }

    @Test
    public void testModuleExtraction() {
        byte[] pkt = PacketParser.createPkt(0, 0, 9, 0, 0, new byte[0]); // 9 = 1001 (binary)
        assertEquals(9, PacketParser.getModule(pkt));
    }

    @Test
    public void testConnectionTypeExtraction() {
        byte[] pkt = PacketParser.createPkt(0, 0, 0, 6, 0, new byte[0]);
        assertEquals(6, PacketParser.getConnectionType(pkt));
    }

    @Test
    public void testBroadcastExtraction() {
        byte[] pkt = PacketParser.createPkt(0, 0, 0, 0, 1, new byte[0]);
        assertEquals(1, PacketParser.getBroadcast(pkt));
    }

    @Test
    public void testPayloadExtraction() {
        byte[] payload = {10, 20, 30, 40};
        byte[] pkt = PacketParser.createPkt(1, 2, 3, 4, 0, payload);
        assertArrayEquals(payload, PacketParser.getPayload(pkt));
    }

    @Test
    public void testPkt() {
        int type = 3;
        int priority = 7;
        int module = 15;
        int connectionType = 5;
        int broadcast = 1;
        byte[] data = {1, 2, 3, 4, 5};

        byte[] pkt = PacketParser.createPkt(type, priority, module, connectionType, broadcast, data);

        assertEquals(type, PacketParser.getType(pkt));
        assertEquals(priority, PacketParser.getPriority(pkt));
        assertEquals(module, PacketParser.getModule(pkt));
        assertEquals(connectionType, PacketParser.getConnectionType(pkt));
        assertEquals(broadcast, PacketParser.getBroadcast(pkt));
        assertArrayEquals(data, PacketParser.getPayload(pkt));

        pkt = PacketParser.createPkt(0, 0, 0, 0, 0, new byte[0]);
        assertEquals(0, PacketParser.getType(pkt));
        assertEquals(0, PacketParser.getPriority(pkt));
        assertEquals(0, PacketParser.getModule(pkt));
        assertEquals(0, PacketParser.getConnectionType(pkt));
        assertEquals(0, PacketParser.getBroadcast(pkt));

        pkt = PacketParser.createPkt(3, 7, 15, 7, 1, new byte[0]);
        assertEquals(3, PacketParser.getType(pkt));
        assertEquals(7, PacketParser.getPriority(pkt));
        assertEquals(15, PacketParser.getModule(pkt));
        assertEquals(7, PacketParser.getConnectionType(pkt));
        assertEquals(1, PacketParser.getBroadcast(pkt));
    }
}