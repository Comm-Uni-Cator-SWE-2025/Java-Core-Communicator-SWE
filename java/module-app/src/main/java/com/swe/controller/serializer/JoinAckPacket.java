package com.swe.controller.serializer;

import com.swe.core.ClientNode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * Packet sent by the server in response to a JOIN request.
 * Contains a mapping from ClientNode to email address for all participants.
 */
public class JoinAckPacket {
    /**
     * Mapping from ClientNode to email address (String).
     */
    private final Map<ClientNode, String> nodeToEmailMap;

    /**
     * Constructs a new JoinAckPacket.
     *
     * @param nodeToEmailMap The mapping from ClientNode to email address
     */
    public JoinAckPacket(final Map<ClientNode, String> nodeToEmailMap) {
        if (nodeToEmailMap == null) {
            throw new IllegalArgumentException("Node to email map cannot be null");
        }
        this.nodeToEmailMap = new HashMap<>(nodeToEmailMap);
    }

    /**
     * Gets the ClientNode to email mapping.
     *
     * @return A copy of the mapping
     */
    public Map<ClientNode, String> getNodeToEmailMap() {
        return new HashMap<>(nodeToEmailMap);
    }

    /**
     * Serializes the JoinAckPacket into a byte array.
     * Format:
     * - 1 byte: packet type (MeetingPacketType.JOINACK.ordinal())
     * - 4 bytes: number of entries in the map (int)
     * - For each entry (except the last):
     *   - 4 bytes: IP address (as int, network byte order)
     *   - 2 bytes: port (as short)
     *   - 4 bytes: email length (int)
     *   - N bytes: email string (UTF-8)
     * - For the last entry:
     *   - 4 bytes: IP address (as int, network byte order)
     *   - 2 bytes: port (as short)
     *   - N bytes: email string (UTF-8, no length prefix since it's the last field in packet)
     *
     * @return The serialized byte array
     */
    public byte[] serialize() {
        int totalSize = 1 + 4; // packet type + map size
        final int numEntries = nodeToEmailMap.size();
        int entryIndex = 0;

        for (Map.Entry<ClientNode, String> entry : nodeToEmailMap.entrySet()) {
            final String email = entry.getValue();
            final byte[] emailBytes = email.getBytes(StandardCharsets.UTF_8);
            totalSize += 4 + 2; // IP (4) + port (2)
            // Add email length prefix for all but the last entry
            if (entryIndex < numEntries - 1) {
                totalSize += 4; // email length (4 bytes)
            }
            totalSize += emailBytes.length; // email bytes
            entryIndex++;
        }

        final ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.put((byte) MeetingPacketType.JOINACK.ordinal());
        buffer.putInt(numEntries);

        entryIndex = 0;
        for (Map.Entry<ClientNode, String> entry : nodeToEmailMap.entrySet()) {
            final ClientNode node = entry.getKey();
            final String email = entry.getValue();
            
            try {
                final InetAddress ipAddr = InetAddress.getByName(node.hostName());
                final byte[] ipBytes = ipAddr.getAddress();
                if (ipBytes.length != 4) {
                    throw new IllegalArgumentException("Only IPv4 addresses are supported: " + node.hostName());
                }
                
                final byte[] emailBytes = email.getBytes(StandardCharsets.UTF_8);
                
                buffer.put(ipBytes); // 4 bytes for IPv4
                buffer.putShort((short) node.port()); // 2 bytes for port
                
                // Include email length for all but the last entry
                if (entryIndex < numEntries - 1) {
                    buffer.putInt(emailBytes.length);
                }
                buffer.put(emailBytes); // Email bytes
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Invalid IP address: " + node.hostName(), e);
            }
            entryIndex++;
        }

        return buffer.array();
    }

    /**
     * Deserializes a byte array into a JoinAckPacket.
     *
     * @param data The byte array to deserialize
     * @return The deserialized JoinAckPacket
     * @throws InvalidParameterException If the packet type is invalid or data is malformed
     */
    public static JoinAckPacket deserialize(final byte[] data) {
        if (data == null || data.length < 5) {
            throw new InvalidParameterException("Invalid data: too short for JoinAckPacket");
        }

        final ByteBuffer buffer = ByteBuffer.wrap(data);

        final byte packetType = buffer.get();
        if (packetType != MeetingPacketType.JOINACK.ordinal()) {
            throw new InvalidParameterException(
                "Invalid packet type: Expected " + MeetingPacketType.JOINACK.ordinal() + " got: " + packetType);
        }

        final int numEntries = buffer.getInt();
        if (numEntries < 0) {
            throw new InvalidParameterException("Invalid number of entries: " + numEntries);
        }

        final Map<ClientNode, String> mapping = new HashMap<>();

        for (int i = 0; i < numEntries; i++) {
            // Read IP address (4 bytes)
            if (buffer.remaining() < 4) {
                throw new InvalidParameterException("Insufficient data for IP address");
            }
            final byte[] ipBytes = new byte[4];
            buffer.get(ipBytes);
            final InetAddress ipAddress;
            try {
                ipAddress = InetAddress.getByAddress(ipBytes);
            } catch (UnknownHostException e) {
                throw new InvalidParameterException("Invalid IP address bytes", e);
            }
            final String host = ipAddress.getHostAddress();

            // Read port (2 bytes)
            if (buffer.remaining() < 2) {
                throw new InvalidParameterException("Insufficient data for port");
            }
            final int port = Short.toUnsignedInt(buffer.getShort());

            // Read email: for all but the last entry, we need length prefix to know boundaries.
            // For the last entry, email takes all remaining bytes (optimization).
            final int emailLength;
            if (i < numEntries - 1) {
                // Not the last entry - need email length to know where next entry starts
                if (buffer.remaining() < 4) {
                    throw new InvalidParameterException("Insufficient data for email length");
                }
                emailLength = buffer.getInt();
                if (emailLength < 0 || emailLength > buffer.remaining()) {
                    throw new InvalidParameterException("Invalid email length: " + emailLength);
                }
            } else {
                // Last entry - email takes all remaining bytes (no length prefix needed)
                emailLength = buffer.remaining();
            }
            
            final byte[] emailBytes = new byte[emailLength];
            buffer.get(emailBytes);
            final String email = new String(emailBytes, StandardCharsets.UTF_8);

            // Create ClientNode and add to mapping
            final ClientNode node = new ClientNode(host, port);
            mapping.put(node, email);
        }

        return new JoinAckPacket(mapping);
    }
}

