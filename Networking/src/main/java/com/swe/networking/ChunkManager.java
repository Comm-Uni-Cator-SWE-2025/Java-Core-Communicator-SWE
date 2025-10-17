package com.swe.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.Math;

public class ChunkManager {
    int payloadSize; // size of payload in bytes
    ChunkManager(int payloadSize){
        this.payloadSize = payloadSize;
    }

    Map<Integer, Vector<byte[]>> GroupChunks(Vector<byte[]> chunks){
        PacketParser parser = PacketParser.getPacketParser();
        Map<Integer, Vector<byte[]>> messageMap = new HashMap<>();
        for (byte[] chunk:chunks){
            int messageId = parser.getMessageId(chunk);
            Vector<byte[]> groupedChunks;
            if (!messageMap.containsKey(messageId)){
                groupedChunks = new Vector<>();
                groupedChunks.add(chunk);
                messageMap.put(messageId, groupedChunks);
            }
            else{
                groupedChunks = messageMap.get(messageId);
                groupedChunks.add(chunk);
            }
        }
        return messageMap;
    }

    // should be of same message id
    byte[] MergeChunks(Vector<byte[]> chunks) throws UnknownHostException {
        if (chunks.isEmpty()){
            throw new IllegalArgumentException("there should be at least one chunk in the input array");
        }
        PacketParser parser = PacketParser.getPacketParser();
        int data_size = 0;
        for (byte[] chunk:chunks){
            data_size += parser.getChunkLength(chunk)-20;
        }
        byte[] completePayload = new byte[data_size];
        Vector<byte[]> sortedChunks = new Vector<>(Collections.nCopies(chunks.size(), (byte[]) null));
        for (byte[] chunk:chunks){
            int i = parser.getChunkNum(chunk);
            if (i == 0){
                sortedChunks.set(chunks.size()-1, chunk);
            }
            else{
                sortedChunks.set(i-1, chunk);
            }
        }
        int i = 0;
        for (byte[] chunk:sortedChunks){
            int chunkSize = parser.getChunkLength(chunk)-20;
            byte[] chunkPayload = parser.getPayload(chunk);
            System.arraycopy(chunkPayload, 0, completePayload, i, chunkSize);
            i += chunkSize;
        }
        int type = parser.getType(chunks.get(0));
        int priority = parser.getPriority(chunks.get(0));
        int module = parser.getModule(chunks.get(0));
        int connectionType = parser.getConnectionType(chunks.get(0));
        int broadcast = parser.getBroadcast(chunks.get(0));
        InetAddress ipaddr = parser.getIpAddress(chunks.get(0));
        int portNumber = parser.getPortNum(chunks.get(0));
        int messageId = parser.getMessageId(chunks.get(0));
        return parser.createPkt(
                type, priority, module,
                connectionType, broadcast, ipaddr,
                portNumber, messageId, 0,
                data_size+20, completePayload
        );
    }
    public Vector<byte[]> Chunk(int priority, int module,
                                int connectionType, int broadcast,
                                InetAddress ipAddr, int portNum,
                                int messageId, byte[] data) {
        Vector<byte[]> chunks = new Vector<>();
        PacketParser parser = PacketParser.getPacketParser();
        // It would be better to make HEADER_SIZE variable in PacketParser public or make function to get its value
        // Currently I am hardcoding it

        // Type is currently set to 0 : Send Packet to Cluster
        // But we need a function in topology to identify the type
        for (int i = 0; i < data.length; i+=payloadSize){
            int pSize = Math.min(payloadSize, data.length-i);
            byte[] payloadChunk = new byte[pSize];
            System.arraycopy(data, i, payloadChunk, 0, pSize);
            int type = 0;
            int chunkNumber = i/payloadSize + 1;
            if (i == data.length-payloadSize){
                chunkNumber = 0;
            }
            byte[] pkt = parser.createPkt(
                    type, priority, module,
                    connectionType, broadcast, ipAddr,
                    portNum, messageId, chunkNumber, pSize+20, payloadChunk
            );
            chunks.add(pkt);
        }
        return chunks;
    }
}
