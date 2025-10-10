package com.swe.networking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Vector;


public class ChunkManagerTest {

    @Test
    void messageChunkingTest() throws UnknownHostException {
        int chunkSize = 4;
        ChunkManager chunkManager = new ChunkManager(chunkSize);
        PacketParser parser = PacketParser.getPacketParser();

        String message = "Hello this is Networking Team";
        byte[] data = message.getBytes();

        int priority = 3;
        int module = 0;
        int connectionType = 1;
        int broadcast = 1;
        InetAddress ipAddr = InetAddress.getByName("0.0.0.0");
        int port = 8000;
        int messageId = 3;
        Vector<byte[]> chunks = chunkManager.Chunk(
                priority, module, connectionType,
                broadcast, ipAddr, port, messageId, data
        );
        for (int chunkNum = 0; chunkNum < chunks.size(); chunkNum++){
            String chunkMsg = new String(parser.getPayload(chunks.get(chunkNum)), StandardCharsets.UTF_8);
            String expectedMsg = message.substring(chunkNum*chunkSize, (chunkNum+1)*chunkSize);
            Assertions.assertEquals(expectedMsg, chunkMsg);
        }
    }
    @Test
    void chunkNumChunkingTest() throws UnknownHostException {
        int chunkSize = 4;
        ChunkManager chunkManager = new ChunkManager(chunkSize);
        PacketParser parser = PacketParser.getPacketParser();

        String message = "Hello this is Networking Team";
        byte[] data = message.getBytes();

        int priority = 3;
        int module = 0;
        int connectionType = 1;
        int broadcast = 1;
        InetAddress ipAddr = InetAddress.getByName("0.0.0.0");
        int port = 8000;
        int messageId = 3;
        Vector<byte[]> chunks = chunkManager.Chunk(
                priority, module, connectionType,
                broadcast, ipAddr, port, messageId, data
        );
        for (int expectedChunkNum = 0; expectedChunkNum < chunks.size(); expectedChunkNum++){
            int chunkNum = parser.getChunkNum(chunks.get(expectedChunkNum));
            Assertions.assertEquals(expectedChunkNum, chunkNum);
        }
    }
    @Test
    void constFieldChunkingTest() throws UnknownHostException{
        int chunkSize = 4;
        ChunkManager chunkManager = new ChunkManager(chunkSize);
        PacketParser parser = PacketParser.getPacketParser();

        String message = "Hello this is Networking Team";
        byte[] data = message.getBytes();

        int priority = 3;
        int module = 0;
        int connectionType = 1;
        int broadcast = 1;
        InetAddress ipAddr = InetAddress.getByName("0.0.0.0");
        int port = 8000;
        int messageId = 3;
        Vector<byte[]> chunks = chunkManager.Chunk(
                priority, module, connectionType,
                broadcast, ipAddr, port, messageId, data
        );
        for (byte[] chunk : chunks) {
            int chunkPriority = parser.getPriority(chunk);
            int chunkModule = parser.getModule(chunk);
            int chunkConnectionType = parser.getConnectionType(chunk);
            int chunkBroadcast = parser.getBroadcast(chunk);
            InetAddress chunkIp = parser.getIpAddress(chunk);
            int chunkPort = parser.getPortNum(chunk);
            int chunkMessageId = parser.getMessageId(chunk);
            Assertions.assertEquals(priority, chunkPriority);
            Assertions.assertEquals(module, chunkModule);
            Assertions.assertEquals(connectionType, chunkConnectionType);
            Assertions.assertEquals(broadcast, chunkBroadcast);
            Assertions.assertEquals(ipAddr, chunkIp);
            Assertions.assertEquals(port, chunkPort);
            Assertions.assertEquals(messageId, chunkMessageId);
        }
    }

}
