package com.swe.networking;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Class for serializing network objects.
 */
public class NetworkSerializer {
    /**
     * variable to store the singleton classobject.
     */
    private static NetworkSerializer serializer = null;

    /**
     * Constructor class network serializer class.
     */
    private NetworkSerializer() {

    }

    /**
     * Function to return the static serializer class.
     *
     * @return the singleton object
     */
    public static NetworkSerializer getNetworkSerializer() {
        if (serializer == null) {
            System.out.println("Creating new Network Serializer object...");
            serializer = new NetworkSerializer();
            return serializer;
        }
        System.out.println("Passing already instantiated Network Serializer object...");
        return serializer;
    }

    /**
     * Function to serialize the clientNetworkRecord datatype.
     *
     * @param record The object to serialize.
     * @return the serialized output
     */
    public byte[] serializeClientNetworkRecord(final ClientNetworkRecord record) {
        final int bufferSize = 20;
        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putShort((byte) record.client().hostName().length());
        final byte[] hostName = record.client().hostName().getBytes();
        buffer.put(hostName);
        buffer.putInt(record.client().port());
        buffer.putInt(record.clusterIndex());
        return buffer.array();
    }

    /**
     * Function to deserialize ClientNetworkRecord.
     *
     * @param data the data to desrialized
     * @return the ClientNetworkRecord object
     */
    public ClientNetworkRecord deserializeClientNetworkRecord(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final int hostLength = buffer.get();
        final byte[] hostName = new byte[hostLength];
        buffer.get(hostName);
        final String hostIp = new String(hostName, StandardCharsets.UTF_8);
        final int hostPort = buffer.getInt();
        final int clusterIdx = buffer.getInt();
        final ClientNetworkRecord record = new ClientNetworkRecord(new ClientNode(hostIp, hostPort), clusterIdx);
        return record;
    }

    /**
     * Function to serialize the clientNetworkRecord datatype.
     *
     * @param record The object to serialize.
     * @return the serialized output
     */
    public byte[] serializeClientNode(final ClientNode record) {
        final int bufferSize = 10;
        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putShort((byte) record.hostName().length());
        final byte[] hostName = record.hostName().getBytes();
        buffer.put(hostName);
        buffer.putInt(record.port());
        return buffer.array();
    }

    /**
     * Function to deserialize ClientNode.
     *
     * @param data the data to desrialized
     * @return the ClientNode object
     */
    public ClientNode deserializeClientNode(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final int hostLength = buffer.get();
        final byte[] hostName = new byte[hostLength];
        buffer.get(hostName);
        final String hostIp = new String(hostName, StandardCharsets.UTF_8);
        final int hostPort = buffer.getInt();
        final ClientNode record = new ClientNode(hostIp, hostPort);
        return record;
    }

    /**
     * Function to deserialize ClientNode.
     *
     * @param structure the data to serialize
     * @return the serialized object
     */
    public byte[] serializeNetworkStructure(final NetworkStructure structure) {
        // Assume maximum 80 clients in 8 clusters
        // 1 Client -> max 10 bytes total -> 800 bytes
        //
        return null;
    }
}
