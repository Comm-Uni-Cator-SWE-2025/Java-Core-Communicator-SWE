package com.swe.networking.SimpleNetworking;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.swe.networking.PacketInfo;
import com.swe.networking.PacketParser;

/**
 * Code for Chunk Manager.
 */
public class SimpleChunkManager {

    /**
     * Variable to store the name of the module.
     */
    private static final String MODULENAME = "SIMPLECHUNKMANAGER";
    /**
     * Singleton chunkManger.
     */
    private static SimpleChunkManager chunkManager = null;
    /**
     * Payload Size.
     */
    private final int defaultPayloadSize; // size of payload in bytes
    /**
     * packetParser.
     */
    private final PacketParser parser = PacketParser.getPacketParser();
    /**
     * headerSize.
     */
    private final int headerSize = PacketParser.getHeaderSize();
    /**
     * Message id.
     */
    private int messageId = 0;
    /**
     * chunkListMap maps message id to list of chunks.
     */
    private final Map<Integer, ByteBuffer> chunkListMap = new HashMap<>();
    /**
     * chunkListMap maps message id to list of chunks.
     */
    private final Map<Integer, Integer> chunkLengthMap = new HashMap<>();

    private SimpleChunkManager(final int payloadSize) {
        SimpleNetworkLogger.printInfo(MODULENAME, "Simple Chunk manager initialized...");
        defaultPayloadSize = payloadSize;
    }

    /**
     * Singleton chunkManager getter.
     *
     * @param payloadSize the payloadsize for the chunkManager
     * @return chunkManager.
     */
    public static SimpleChunkManager getChunkManager(final int payloadSize) {
        if (chunkManager == null) {
            chunkManager = new SimpleChunkManager(payloadSize);
            return chunkManager;
        }
        SimpleNetworkLogger.printInfo(MODULENAME, "Passing already initialized Simple Chunk Manager...");
        return chunkManager;
    }

    /**
     * Add chunk function.
     *
     * @param chunk the byte of chunk coming in.
     * @return the combined message
     * @throws UnknownHostException the issue from packet parser.
     */
    public byte[] addChunk(final byte[] chunk) throws UnknownHostException {
        final PacketInfo info = parser.parsePacket(chunk);
        final int msgId = info.getMessageId();
        final int maxNumChunks = info.getChunkLength();
        final int chunkId = info.getChunkNum();
        SimpleNetworkLogger.printInfo(MODULENAME, "Chunk id / total chunks : " + chunkId + " / " + maxNumChunks);
        SimpleNetworkLogger.printInfo(MODULENAME, "msg id : " + msgId);
        if (chunkListMap.containsKey(msgId)) {
            final ByteBuffer messageBuffer = chunkListMap.get(msgId);
            messageBuffer.position(chunkId * defaultPayloadSize);
            messageBuffer.put(info.getPayload());
            final int newVal = chunkLengthMap.getOrDefault(msgId, 0) + 1;
            chunkLengthMap.put(msgId, newVal);
        } else {
            final ByteBuffer messageBuffer = ByteBuffer.allocate(maxNumChunks * defaultPayloadSize);
            messageBuffer.position(chunkId * defaultPayloadSize);
            messageBuffer.put(info.getPayload());
            chunkListMap.put(msgId, messageBuffer);
            chunkLengthMap.put(msgId, 1);
        }
        if (chunkLengthMap.get(msgId) == maxNumChunks) {
            final byte[] messageChunk = chunkListMap.get(msgId).array();
            chunkListMap.remove(msgId);
            return messageChunk;
        }
        return null;
    }

    /**
     * Chunking function.
     *
     * @param info The Chunk information including payload of the message
     * @param payloadSize The payload size of the message.
     * @return chunks The message broken into list of chunks.
     */
    public Vector<byte[]> chunk(final PacketInfo info, final int payloadSize) {
        // List of chunked packets we will be returning
        final Vector<byte[]> chunks = new Vector<>();

        final byte[] data = info.getPayload();
        final int numChunks = (data.length + payloadSize - 1) / payloadSize;
        info.setChunkLength(numChunks);
        info.setMessageId(messageId);
        messageId++;
        // reset message id to zero once it exceed limit
        for (int i = 0; i < data.length; i += payloadSize) {
            final int pSize = Math.min(payloadSize, data.length - i);
            final byte[] payloadChunk = new byte[pSize];
            System.arraycopy(data, i, payloadChunk, 0, pSize);
            final int chunkNumber = i / payloadSize;
            info.setChunkNum(chunkNumber);
            info.setPayload(payloadChunk);
            info.setLength(headerSize + pSize);
            final byte[] pkt = parser.createPkt(info);
            chunks.add(pkt);
        }
        return chunks;
    }

    public Vector<byte[]> chunk(final PacketInfo info) {
        return chunk(info, defaultPayloadSize);
    }
}
