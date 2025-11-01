package com.swe.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Code for Chunk Manager.
 */
public class ChunkManager {
    /**
     * Payload Size.
     */

    private final int payloadSize; // size of payload in bytes
    /**
     * headerSize.
     */
    private final int headerSize = 20;

    ChunkManager(final int chunkPayloadSize) {
        this.payloadSize = chunkPayloadSize;
    }

    Map<Integer, Vector<byte[]>> groupChunks(final Vector<byte[]> chunks) {
        final PacketParser parser = PacketParser.getPacketParser();
        final Map<Integer, Vector<byte[]>> messageMap = new HashMap<>();
        for (byte[] chunk:chunks) {
            final int messageId = parser.getMessageId(chunk);
            final Vector<byte[]> groupedChunks;
            if (!messageMap.containsKey(messageId)) {
                groupedChunks = new Vector<>();
                groupedChunks.add(chunk);
                messageMap.put(messageId, groupedChunks);
            } else {
                groupedChunks = messageMap.get(messageId);
                groupedChunks.add(chunk);
            }
        }
        return messageMap;
    }

    // should be of same message id
    byte[] mergeChunks(final Vector<byte[]> chunks) throws UnknownHostException {
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("there should be at least one chunk in the input array");
        }
        final PacketParser parser = PacketParser.getPacketParser();
        int dataSize = 0;
        for (byte[] chunk:chunks) {
            dataSize += parser.getChunkLength(chunk) - headerSize;
        }
        final byte[] completePayload = new byte[dataSize];
        final Vector<byte[]> sortedChunks = new Vector<>(Collections.nCopies(chunks.size(), (byte[]) null));
        for (byte[] chunk:chunks) {
            final int i = parser.getChunkNum(chunk);
            if (i == 0) {
                sortedChunks.set(chunks.size() - 1, chunk);
            } else {
                sortedChunks.set(i - 1, chunk);
            }
        }
        int i = 0;
        for (byte[] chunk:sortedChunks) {
            final int chunkSize = parser.getChunkLength(chunk) - headerSize;
            final byte[] chunkPayload = parser.getPayload(chunk);
            System.arraycopy(chunkPayload, 0, completePayload, i, chunkSize);
            i += chunkSize;
        }
        final int type = parser.getType(chunks.get(0));
        final int priority = parser.getPriority(chunks.get(0));
        final int module = parser.getModule(chunks.get(0));
        final int connectionType = parser.getConnectionType(chunks.get(0));
        final int broadcast = parser.getBroadcast(chunks.get(0));
        final InetAddress ipaddr = parser.getIpAddress(chunks.get(0));
        final int portNumber = parser.getPortNum(chunks.get(0));
        final int messageId = parser.getMessageId(chunks.get(0));
        return parser.createPkt(
                type, priority, module,
                connectionType, broadcast, ipaddr,
                portNumber, messageId, 0,
                dataSize + headerSize, completePayload
        );
    }

    /**
     * Chunking function.
     * @param priority The priority of the message
     * @param  module The module to send the message to
     * @param connectionType The connection type
     * @param  broadcast Broadcast or not
     * @param  ipAddr IP address of receiver
     * @param  portNum Port number of receiver
     * @param  messageId unique message id
     * @param  data the payload
     * @return the message broken to chunks
     */
    public Vector<byte[]> chunk(final int priority, final int module,
                                final int connectionType, final int broadcast,
                                final InetAddress ipAddr, final int portNum,
                                final int messageId, final byte[] data) {
        final Vector<byte[]> chunks = new Vector<>();
        final PacketParser parser = PacketParser.getPacketParser();
        // It would be better to make HEADER_SIZE variable in PacketParser public or make function to get its value
        // Currently I am hardcoding it

        // Type is currently set to 0 : Send Packet to Cluster
        // But we need a function in topology to identify the type
        for (int i = 0; i < data.length; i += payloadSize) {
            final int pSize = Math.min(payloadSize, data.length - i);
            final byte[] payloadChunk = new byte[pSize];
            System.arraycopy(data, i, payloadChunk, 0, pSize);
            final int type = 0;
            int chunkNumber = i / payloadSize + 1;
            if (i == data.length - payloadSize) {
                chunkNumber = 0;
            }
            final byte[] pkt = parser.createPkt(
                    type, priority, module,
                    connectionType, broadcast, ipAddr,
                    portNum, messageId, chunkNumber, pSize + headerSize, payloadChunk
            );
            chunks.add(pkt);
        }
        return chunks;
    }
}
