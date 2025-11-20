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
 * Packet sent by the server in response to an IAM request.
 * Contains a mapping from ClientNode to email address and email to displayName for all participants.
 */
public class JoinAckPacket {
    /**
     * Mapping from ClientNode to email address (String).
     */
    private final Map<ClientNode, String> nodeToEmailMap;

    /**
     * Mapping from email address to displayName (String).
     */
    private final Map<String, String> emailToDisplayNameMap;

    /**
     * Constructs a new JoinAckPacket.
     *
     * @param nodeToEmailMap The mapping from ClientNode to email address
     * @param emailToDisplayNameMap The mapping from email address to displayName
     */
    public JoinAckPacket(final Map<ClientNode, String> nodeToEmailMap, final Map<String, String> emailToDisplayNameMap) {
        if (nodeToEmailMap == null) {
            throw new IllegalArgumentException("Node to email map cannot be null");
        }
        if (emailToDisplayNameMap == null) {
            throw new IllegalArgumentException("Email to displayName map cannot be null");
        }
        this.nodeToEmailMap = new HashMap<>(nodeToEmailMap);
        this.emailToDisplayNameMap = new HashMap<>(emailToDisplayNameMap);
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
     * Gets the email to displayName mapping.
     *
     * @return A copy of the mapping
     */
    public Map<String, String> getEmailToDisplayNameMap() {
        return new HashMap<>(emailToDisplayNameMap);
    }

    /**
     * Serializes the JoinAckPacket into a byte array.
     * Format:
     * - 1 byte: packet type (MeetingPacketType.JOINACK.ordinal())
     * - 4 bytes: number of entries in nodeToEmailMap (int)
     * - For each entry in nodeToEmailMap:
     *   - 4 bytes: IP address (as int, network byte order)
     *   - 2 bytes: port (as short)
     *   - 4 bytes: email length (int)
     *   - N bytes: email string (UTF-8)
     * - 4 bytes: number of entries in emailToDisplayNameMap (int)
     * - For each entry in emailToDisplayNameMap:
     *   - 4 bytes: email length (int)
     *   - N bytes: email string (UTF-8)
     *   - 4 bytes: displayName length (int)
     *   - M bytes: displayName string (UTF-8)
     *
     * @return The serialized byte array
     */
    public byte[] serialize() {
        int totalSize = 1 + 4; // packet type + nodeToEmailMap size
        final int numNodeEntries = nodeToEmailMap.size();

        // Calculate size for nodeToEmailMap entries
        for (Map.Entry<ClientNode, String> entry : nodeToEmailMap.entrySet()) {
            final String email = entry.getValue();
            final byte[] emailBytes = email.getBytes(StandardCharsets.UTF_8);
            totalSize += 4 + 2 + 4 + emailBytes.length; // IP (4) + port (2) + email length (4) + email bytes
        }

        // Add size for emailToDisplayNameMap
        totalSize += 4; // emailToDisplayNameMap size

        for (Map.Entry<String, String> entry : emailToDisplayNameMap.entrySet()) {
            final String email = entry.getKey();
            final String displayName = entry.getValue();
            final byte[] emailBytes = email.getBytes(StandardCharsets.UTF_8);
            final byte[] displayNameBytes = displayName.getBytes(StandardCharsets.UTF_8);
            totalSize += 4 + emailBytes.length + 4 + displayNameBytes.length; // email length + email + displayName length + displayName
        }

        final ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.put((byte) MeetingPacketType.JOINACK.ordinal());
        buffer.putInt(numNodeEntries);

        // Serialize nodeToEmailMap
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
                buffer.putInt(emailBytes.length); // Email length
                buffer.put(emailBytes); // Email bytes
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Invalid IP address: " + node.hostName(), e);
            }
        }

        // Serialize emailToDisplayNameMap
        buffer.putInt(emailToDisplayNameMap.size());
        for (Map.Entry<String, String> entry : emailToDisplayNameMap.entrySet()) {
            final String email = entry.getKey();
            final String displayName = entry.getValue();
            final byte[] emailBytes = email.getBytes(StandardCharsets.UTF_8);
            final byte[] displayNameBytes = displayName.getBytes(StandardCharsets.UTF_8);
            
            buffer.putInt(emailBytes.length); // Email length
            buffer.put(emailBytes); // Email bytes
            buffer.putInt(displayNameBytes.length); // DisplayName length
            buffer.put(displayNameBytes); // DisplayName bytes
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

        // Deserialize nodeToEmailMap
        final int numNodeEntries = buffer.getInt();
        if (numNodeEntries < 0) {
            throw new InvalidParameterException("Invalid number of node entries: " + numNodeEntries);
        }

        final Map<ClientNode, String> nodeToEmailMapping = new HashMap<>();

        for (int i = 0; i < numNodeEntries; i++) {
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

            // Read email length (4 bytes)
            if (buffer.remaining() < 4) {
                throw new InvalidParameterException("Insufficient data for email length");
            }
            final int emailLength = buffer.getInt();
            if (emailLength < 0 || emailLength > buffer.remaining()) {
                throw new InvalidParameterException("Invalid email length: " + emailLength);
            }
            
            // Read email (N bytes)
            if (buffer.remaining() < emailLength) {
                throw new InvalidParameterException("Insufficient data for email");
            }
            final byte[] emailBytes = new byte[emailLength];
            buffer.get(emailBytes);
            final String email = new String(emailBytes, StandardCharsets.UTF_8);

            // Create ClientNode and add to mapping
            final ClientNode node = new ClientNode(host, port);
            nodeToEmailMapping.put(node, email);
        }

        // Deserialize emailToDisplayNameMap
        if (buffer.remaining() < 4) {
            throw new InvalidParameterException("Insufficient data for emailToDisplayNameMap size");
        }
        final int numDisplayNameEntries = buffer.getInt();
        if (numDisplayNameEntries < 0) {
            throw new InvalidParameterException("Invalid number of displayName entries: " + numDisplayNameEntries);
        }

        final Map<String, String> emailToDisplayNameMapping = new HashMap<>();

        for (int i = 0; i < numDisplayNameEntries; i++) {
            // Read email length (4 bytes)
            if (buffer.remaining() < 4) {
                throw new InvalidParameterException("Insufficient data for email length");
            }
            final int emailLength = buffer.getInt();
            if (emailLength < 0 || emailLength > buffer.remaining()) {
                throw new InvalidParameterException("Invalid email length: " + emailLength);
            }
            
            // Read email (N bytes)
            if (buffer.remaining() < emailLength) {
                throw new InvalidParameterException("Insufficient data for email");
            }
            final byte[] emailBytes = new byte[emailLength];
            buffer.get(emailBytes);
            final String email = new String(emailBytes, StandardCharsets.UTF_8);

            // Read displayName length (4 bytes)
            if (buffer.remaining() < 4) {
                throw new InvalidParameterException("Insufficient data for displayName length");
            }
            final int displayNameLength = buffer.getInt();
            if (displayNameLength < 0 || displayNameLength > buffer.remaining()) {
                throw new InvalidParameterException("Invalid displayName length: " + displayNameLength);
            }
            
            // Read displayName (M bytes)
            if (buffer.remaining() < displayNameLength) {
                throw new InvalidParameterException("Insufficient data for displayName");
            }
            final byte[] displayNameBytes = new byte[displayNameLength];
            buffer.get(displayNameBytes);
            final String displayName = new String(displayNameBytes, StandardCharsets.UTF_8);

            emailToDisplayNameMapping.put(email, displayName);
        }

        return new JoinAckPacket(nodeToEmailMapping, emailToDisplayNameMapping);
    }
}

