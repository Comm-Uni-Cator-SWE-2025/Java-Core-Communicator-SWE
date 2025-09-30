package com.swe.networking;

import org.junit.Test;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class PacketParserTest {

    @Test
    public void testTypeExtraction() throws UnknownHostException {
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        byte[] pkt = parser.createPkt(2, 0, 0, 0, 0, ip, 0, new byte[0]);
        assertEquals(2, parser.getType(pkt));
    }

    @Test
    public void testPriorityExtraction() throws UnknownHostException {
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        byte[] pkt = parser.createPkt(0, 5, 0, 0, 0, ip, 0, new byte[0]);
        assertEquals(5, parser.getPriority(pkt));
    }

    @Test
    public void testModuleExtraction() throws UnknownHostException {
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        byte[] pkt = parser.createPkt(0, 0, 9, 0, 0, ip, 0, new byte[0]); // 9 = 1001 (binary)
        assertEquals(9, parser.getModule(pkt));
    }

    @Test
    public void testConnectionTypeExtraction() throws UnknownHostException {
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        byte[] pkt = parser.createPkt(0, 0, 0, 6, 0, ip, 0, new byte[0]);
        assertEquals(6, parser.getConnectionType(pkt));
    }

    @Test
    public void testBroadcastExtraction() throws UnknownHostException {
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        byte[] pkt = parser.createPkt(0, 0, 0, 0, 1, ip, 0, new byte[0]);
        assertEquals(1, parser.getBroadcast(pkt));
    }

    @Test
    public void testIpAddressExtraction() throws UnknownHostException {
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("192.168.1.50");
        byte[] pkt = parser.createPkt(0, 0, 0, 0, 0, ip, 0, new byte[0]);
        assertEquals(ip, parser.getIpAddress(pkt));
    }

    @Test
    public void testPortNumExtraction() throws UnknownHostException {
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        int port = 8080;
        byte[] pkt = parser.createPkt(0, 0, 0, 0, 0, ip, port, new byte[0]);
        assertEquals(port, parser.getPortNum(pkt));
    }

    @Test
    public void testPayloadExtraction() throws UnknownHostException {
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        byte[] payload = {10, 20, 30, 40};
        byte[] pkt = parser.createPkt(0, 0, 0, 0, 0, ip, 0, payload);
        assertArrayEquals(payload, parser.getPayload(pkt));
    }

    @Test
    public void testPkt() throws UnknownHostException {
        PacketParser parser = PacketParser.getPacketParser();

        int type = 3;
        int priority = 7;
        int module = 15;
        int connectionType = 5;
        int broadcast = 1;
        InetAddress ip = InetAddress.getByName("10.0.0.5");
        int port = 65535;
        byte[] data = {1, 2, 3, 4, 5};

        byte[] pkt = parser.createPkt(type, priority, module, connectionType, broadcast, ip, port, data);

        assertEquals(type, parser.getType(pkt));
        assertEquals(priority, parser.getPriority(pkt));
        assertEquals(module, parser.getModule(pkt));
        assertEquals(connectionType, parser.getConnectionType(pkt));
        assertEquals(broadcast, parser.getBroadcast(pkt));
        assertEquals(ip, parser.getIpAddress(pkt));
        assertEquals(port, parser.getPortNum(pkt));
        assertArrayEquals(data, parser.getPayload(pkt));
    }
}