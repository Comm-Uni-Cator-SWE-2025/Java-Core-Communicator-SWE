package com.swe.networking;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Vector;
import java.lang.Math;

public class ChunkManager {
    int payloadSize; // size of payload in bytes
    ChunkManager(int payloadSize){
        this.payloadSize = payloadSize;
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
            byte[] pkt = parser.createPkt(
                    0, priority, module,
                    connectionType, broadcast, ipAddr,
                    portNum, messageId, i/payloadSize, pSize+20, payloadChunk
            );
            chunks.add(pkt);
        }
        return  chunks;
    }
}
