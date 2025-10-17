package com.swe.networking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;


public class ChunkManagerTest {

    @Test
    void messageChunkingTest() throws UnknownHostException {
        int payloadSize = 4;
        ChunkManager chunkManager = new ChunkManager(payloadSize);
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
        String chunkMsg;
        String expectedMsg;
        // 0
        chunkMsg = new String(parser.getPayload(chunks.get(0)), StandardCharsets.UTF_8);
        expectedMsg = "Hell";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        // 1
        chunkMsg = new String(parser.getPayload(chunks.get(1)), StandardCharsets.UTF_8);
        expectedMsg = "o th";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        // 2
        chunkMsg = new String(parser.getPayload(chunks.get(2)), StandardCharsets.UTF_8);
        expectedMsg = "is i";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        // 3
        chunkMsg = new String(parser.getPayload(chunks.get(3)), StandardCharsets.UTF_8);
        expectedMsg = "s Ne";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        // 4
        chunkMsg = new String(parser.getPayload(chunks.get(4)), StandardCharsets.UTF_8);
        expectedMsg = "twor";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        // 5
        chunkMsg = new String(parser.getPayload(chunks.get(5)), StandardCharsets.UTF_8);
        expectedMsg = "king";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        // 6
        chunkMsg = new String(parser.getPayload(chunks.get(6)), StandardCharsets.UTF_8);
        expectedMsg = " Tea";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        // 7
        chunkMsg = new String(parser.getPayload(chunks.get(7)), StandardCharsets.UTF_8);
        expectedMsg = "m";
        Assertions.assertEquals(expectedMsg, chunkMsg);
    }
    @Test
    void chunkNumChunkingTest() throws UnknownHostException {
        int payloadSize = 3;
        ChunkManager chunkManager = new ChunkManager(payloadSize);
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
            if (chunkNum == 0){
                chunkNum = chunks.size()-1;
            }
            else{
                chunkNum--;
            }
            Assertions.assertEquals(expectedChunkNum, chunkNum);
        }
    }
    @Test
    void constFieldChunkingTest() throws UnknownHostException{
        int payloadSize = 6;
        ChunkManager chunkManager = new ChunkManager(payloadSize);
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
    @Test
    void mergeChunksTest() throws UnknownHostException{
        int payloadSize = 3;
        ChunkManager chunkManager = new ChunkManager(payloadSize);
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
        Collections.shuffle(chunks);
        byte[] mergedPkt = chunkManager.MergeChunks(chunks);
        Assertions.assertEquals(priority, parser.getPriority(mergedPkt));
        Assertions.assertEquals(module, parser.getModule(mergedPkt));
        Assertions.assertEquals(connectionType, parser.getConnectionType(mergedPkt));
        Assertions.assertEquals(broadcast, parser.getBroadcast(mergedPkt));
        Assertions.assertEquals(ipAddr, parser.getIpAddress(mergedPkt));
        Assertions.assertEquals(messageId, parser.getMessageId(mergedPkt));
        String mergedMessage = new String(parser.getPayload(mergedPkt), StandardCharsets.UTF_8);
        Assertions.assertEquals(message, mergedMessage);
    }
    @Test
    void mapChunksTest() throws  UnknownHostException{
        int payloadSize = 3;
        ChunkManager chunkManager = new ChunkManager(payloadSize);
        PacketParser parser = PacketParser.getPacketParser();

        String message = "This is Networking Team";
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
        Collections.shuffle(chunks);


        String newMessage = "What is this team?";
        byte[] newData = newMessage.getBytes();
        int newPriority = 3;
        int newModule = 0;
        int newConnectionType = 1;
        int newBroadcast = 1;
        InetAddress newIpAddr = InetAddress.getByName("0.0.0.0");
        int newPort = 8000;
        int newMessageId = 4;
        Vector<byte[]> newChunks = chunkManager.Chunk(
                newPriority, newModule, newConnectionType,
                newBroadcast, newIpAddr, newPort, newMessageId, newData
        );
        Collections.shuffle(newChunks);
        Vector<byte[]> allChunks = new Vector<>();
        allChunks.addAll(chunks); allChunks.addAll(newChunks);
        Map<Integer, Vector<byte[]>> groupedChunks = chunkManager.groupChunks(allChunks);
        for (Vector<byte[]> chunkGroup: groupedChunks.values()){
            byte[] mergedPkt = chunkManager.MergeChunks(chunkGroup);
            String msg = new String(parser.getPayload(mergedPkt), StandardCharsets.UTF_8);
            int msgId = parser.getMessageId(mergedPkt);
            if (msgId == 3){
                Assertions.assertEquals(message, msg);
            }
            else if (msgId == 4){
                Assertions.assertEquals(newMessage, msg);
            }
            else{
                throw new UnknownError("message id " + msgId + " is not present");
            }
        }
    }
}
