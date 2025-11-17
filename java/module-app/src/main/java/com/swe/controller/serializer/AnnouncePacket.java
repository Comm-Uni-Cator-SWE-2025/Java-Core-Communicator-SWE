package com.swe.controller.serializer;

import com.swe.core.ClientNode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Packet broadcast by each controller to announce its presence in a meeting.
 */
public class AnnouncePacket {
    private final String email;
    private final ClientNode clientNode;

    public AnnouncePacket(final String email, final ClientNode clientNode) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (clientNode == null) {
            throw new IllegalArgumentException("ClientNode cannot be null");
        }
        this.email = email;
        this.clientNode = clientNode;
    }

    public String getEmail() {
        return email;
    }

    public ClientNode getClientNode() {
        return clientNode;
    }

    /**
     * Serializes the ANNOUNCE packet:
     *  - 1 byte: packet type
     *  - 4 bytes: IP address (as int, network byte order)
     *  - 2 bytes: port (as short)
     *  - N bytes: email (UTF-8, no length prefix since it's the last field)
     *
     * @return serialized bytes
     */
    public byte[] serialize() {
        try {
            final InetAddress ipAddress = InetAddress.getByName(clientNode.hostName());
            final byte[] ipBytes = ipAddress.getAddress();
            if (ipBytes.length != 4) {
                throw new IllegalArgumentException("Only IPv4 addresses are supported");
            }
            
            final byte[] emailBytes = email.getBytes(StandardCharsets.UTF_8);
            final ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + 2 + emailBytes.length);

            buffer.put((byte) MeetingPacketType.ANNOUNCE.ordinal());
            buffer.put(ipBytes); // 4 bytes for IPv4
            buffer.putShort((short) clientNode.port()); // 2 bytes for port
            buffer.put(emailBytes); // Email without length prefix

            return buffer.array();
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address: " + clientNode.hostName(), e);
        }
    }

    public static AnnouncePacket deserialize(final byte[] data) {
        if (data == null || data.length < 7) {
            throw new InvalidParameterException("Invalid data: too short for AnnouncePacket (need at least 7 bytes)");
        }

        final ByteBuffer buffer = ByteBuffer.wrap(data);

        final byte packetType = buffer.get();
        if (packetType != MeetingPacketType.ANNOUNCE.ordinal()) {
            throw new InvalidParameterException("Invalid packet type for AnnouncePacket: " + packetType);
        }

        // Read IP address (4 bytes)
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

        // Read email (remaining bytes, no length prefix)
        final byte[] emailBytes = new byte[buffer.remaining()];
        buffer.get(emailBytes);
        final String email = new String(emailBytes, StandardCharsets.UTF_8);

        return new AnnouncePacket(email, new ClientNode(host, port));
    }
}

