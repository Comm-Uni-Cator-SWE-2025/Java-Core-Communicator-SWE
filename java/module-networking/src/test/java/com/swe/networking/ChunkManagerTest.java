package com.swe.networking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

/**
 * Test cases for chunk manager.
 */
public class ChunkManagerTest {
    /**
     * Chunk manager test.
     */
    @Test
    void messageChunkingTest() throws UnknownHostException {
        final int payloadSize = 4;
        final ChunkManager chunkManager = new ChunkManager(payloadSize);
        final PacketParser parser = PacketParser.getPacketParser();

        final String message = "Hello this is Networking Team";
        final byte[] data = message.getBytes();

        final int priority = 3;
        final int module = 0;
        final int connectionType = 1;
        final int broadcast = 1;
        final InetAddress ipAddr = InetAddress.getByName("0.0.0.0");
        final int port = 8000;
        final int messageId = 3;
        final Vector<byte[]> chunks = chunkManager.chunk(
                priority, module, connectionType,
                broadcast, ipAddr, port, messageId, data
        );
        String chunkMsg;
        String expectedMsg;
        int index = 0;
        // 0
        chunkMsg = new String(parser.getPayload(chunks.get(index)), StandardCharsets.UTF_8);
        expectedMsg = "Hell";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        index++;
        // 1
        chunkMsg = new String(parser.getPayload(chunks.get(index)), StandardCharsets.UTF_8);
        expectedMsg = "o th";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        index++;
        // 2
        chunkMsg = new String(parser.getPayload(chunks.get(index)), StandardCharsets.UTF_8);
        expectedMsg = "is i";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        index++;
        // 3
        chunkMsg = new String(parser.getPayload(chunks.get(index)), StandardCharsets.UTF_8);
        expectedMsg = "s Ne";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        index++;
        // 4
        chunkMsg = new String(parser.getPayload(chunks.get(index)), StandardCharsets.UTF_8);
        expectedMsg = "twor";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        index++;
        // 5
        chunkMsg = new String(parser.getPayload(chunks.get(index)), StandardCharsets.UTF_8);
        expectedMsg = "king";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        index++;
        // 6
        chunkMsg = new String(parser.getPayload(chunks.get(index)), StandardCharsets.UTF_8);
        expectedMsg = " Tea";
        Assertions.assertEquals(expectedMsg, chunkMsg);
        index++;
        // 7
        chunkMsg = new String(parser.getPayload(chunks.get(index)), StandardCharsets.UTF_8);
        expectedMsg = "m";
        Assertions.assertEquals(expectedMsg, chunkMsg);
    }

    @Test
    void chunkNumChunkingTest() throws UnknownHostException {
        final int payloadSize = 3;
        final ChunkManager chunkManager = new ChunkManager(payloadSize);
        final PacketParser parser = PacketParser.getPacketParser();

        final String message = "Hello this is Networking Team";
        final byte[] data = message.getBytes();
        final int priority = 3;
        final int module = 0;
        final int connectionType = 1;
        final int broadcast = 1;
        final InetAddress ipAddr = InetAddress.getByName("0.0.0.0");
        final int port = 8000;
        final int messageId = 3;
        final Vector<byte[]> chunks = chunkManager.chunk(
                priority, module, connectionType,
                broadcast, ipAddr, port, messageId, data
        );
        for (int expectedChunkNum = 0; expectedChunkNum < chunks.size(); expectedChunkNum++) {
            int chunkNum = parser.getChunkNum(chunks.get(expectedChunkNum));
            if (chunkNum == 0) {
                chunkNum = chunks.size() - 1;
            } else {
                chunkNum--;
            }
            Assertions.assertEquals(expectedChunkNum, chunkNum);
        }
    }

    @Test
    void constFieldChunkingTest() throws UnknownHostException {
        final int payloadSize = 6;
        final ChunkManager chunkManager = new ChunkManager(payloadSize);
        final PacketParser parser = PacketParser.getPacketParser();

        final String message = "Hello this is Networking Team";
        final byte[] data = message.getBytes();

        final int priority = 3;
        final int module = 0;
        final int connectionType = 1;
        final int broadcast = 1;
        final InetAddress ipAddr = InetAddress.getByName("0.0.0.0");
        final int port = 8000;
        final int messageId = 3;
        final Vector<byte[]> chunks = chunkManager.chunk(
                priority, module, connectionType,
                broadcast, ipAddr, port, messageId, data
        );
        for (byte[] chunk : chunks) {
            final int chunkPriority = parser.getPriority(chunk);
            final int chunkModule = parser.getModule(chunk);
            final int chunkConnectionType = parser.getConnectionType(chunk);
            final int chunkBroadcast = parser.getBroadcast(chunk);
            final InetAddress chunkIp = parser.getIpAddress(chunk);
            final int chunkPort = parser.getPortNum(chunk);
            final int chunkMessageId = parser.getMessageId(chunk);
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
    void mergeChunksTest() throws UnknownHostException, IllegalArgumentException {
        final int payloadSize = 3;
        final ChunkManager chunkManager = new ChunkManager(payloadSize);
        final PacketParser parser = PacketParser.getPacketParser();

        final String message = "Hello this is Networking Team";
        final byte[] data = message.getBytes();
        final int priority = 3;
        final int module = 0;
        final int connectionType = 1;
        final int broadcast = 1;
        final InetAddress ipAddr = InetAddress.getByName("0.0.0.0");
        final int port = 8000;
        final int messageId = 3;
        final Vector<byte[]> chunks = chunkManager.chunk(
                priority, module, connectionType,
                broadcast, ipAddr, port, messageId, data
        );
        Collections.shuffle(chunks);
        final byte[] mergedPkt = chunkManager.mergeChunks(chunks);
        Assertions.assertEquals(priority, parser.getPriority(mergedPkt));
        Assertions.assertEquals(module, parser.getModule(mergedPkt));
        Assertions.assertEquals(connectionType, parser.getConnectionType(mergedPkt));
        Assertions.assertEquals(broadcast, parser.getBroadcast(mergedPkt));
        Assertions.assertEquals(ipAddr, parser.getIpAddress(mergedPkt));
        Assertions.assertEquals(messageId, parser.getMessageId(mergedPkt));
        final String mergedMessage = new String(parser.getPayload(mergedPkt), StandardCharsets.UTF_8);
        Assertions.assertEquals(message, mergedMessage);
    }

    @Test
    void mapChunksTest() throws  UnknownHostException, IllegalArgumentException {
        final int payloadSize = 3;
        final ChunkManager chunkManager = new ChunkManager(payloadSize);
        final PacketParser parser = PacketParser.getPacketParser();

        final String message = "This is Networking Team";
        final byte[] data = message.getBytes();
        final int priority = 3;
        final int module = 0;
        final int connectionType = 1;
        final int broadcast = 1;
        final InetAddress ipAddr = InetAddress.getByName("0.0.0.0");
        final int port = 8000;
        final int messageId = 3;
        final Vector<byte[]> chunks = chunkManager.chunk(
                priority, module, connectionType,
                broadcast, ipAddr, port, messageId, data
        );
        Collections.shuffle(chunks);


        final String newMessage = "What is this team?";
        final byte[] newData = newMessage.getBytes();
        final int newPriority = 3;
        final int newModule = 0;
        final int newConnectionType = 1;
        final int newBroadcast = 1;
        final InetAddress newIpAddr = InetAddress.getByName("0.0.0.0");
        final int newPort = 8000;
        final int newMessageId = 4;
        final Vector<byte[]> newChunks = chunkManager.chunk(
                newPriority, newModule, newConnectionType,
                newBroadcast, newIpAddr, newPort, newMessageId, newData
        );
        Collections.shuffle(newChunks);
        final Vector<byte[]> allChunks = new Vector<>();
        allChunks.addAll(chunks);
        allChunks.addAll(newChunks);
        final Map<Integer, Vector<byte[]>> groupedChunks = chunkManager.groupChunks(allChunks);
        for (Vector<byte[]> chunkGroup: groupedChunks.values()) {
            final byte[] mergedPkt = chunkManager.mergeChunks(chunkGroup);
            final String msg = new String(parser.getPayload(mergedPkt), StandardCharsets.UTF_8);
            final int msgId = parser.getMessageId(mergedPkt);
            if (msgId == messageId) {
                Assertions.assertEquals(message, msg);
            } else if (msgId == newMessageId) {
                Assertions.assertEquals(newMessage, msg);
            } else {
                throw new UnknownError("message id " + msgId + " is not present");
            }
        }
    }

    @Test
    void illegalArgumentTest() throws IllegalArgumentException, UnknownHostException {
        final Vector<byte[]> emptyChunkList = new Vector<>();
        final int payloadSize = 3;
        final ChunkManager chunkManager = new ChunkManager(payloadSize);
        boolean errorDetected;
        try {
            chunkManager.mergeChunks(emptyChunkList);
            errorDetected = false;
        } catch (IllegalArgumentException e) {
            errorDetected = true;
        }
        Assertions.assertTrue(errorDetected);
    }
}
