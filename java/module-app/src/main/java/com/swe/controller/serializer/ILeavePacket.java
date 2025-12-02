package com.swe.controller.serializer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

import com.swe.core.ClientNode;

/**
 * Packet sent by participants to announce their presence in a meeting.
 * When sent to the server, it serves as a join request.
 * When broadcast to peers, it serves as an announcement.
 * Contains the email address, display name, and network coordinates of the
 * participant.
 */
public class ILeavePacket {
    /**
     * The email address of the participant.
     */
    private final String email;

    /**
     * The display name of the participant.
     */
    private final String displayName;

    /**
     * The network coordinates of the participant.
     */
    private final ClientNode clientNode;

    /**
     * Size of IPv4 address in bytes.
     */
    private static final int IPV4_BYTE_SIZE = 4;

    /**
     * Minimum packet size in bytes.
     */
    private static final int MIN_PACKET_SIZE = 15;

    /**
     * Constructs a new LEAVEPacket.
     *
     * @param emailParam       The email address of the participant
     * @param displayNameParam The display name of the participant
     * @param clientNodeParam  The network coordinates of the participant
     */
    public ILeavePacket(final String emailParam, final String displayNameParam, final ClientNode clientNodeParam) {
        if (emailParam == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (displayNameParam == null) {
            throw new IllegalArgumentException("DisplayName cannot be null");
        }
        if (clientNodeParam == null) {
            throw new IllegalArgumentException("ClientNode cannot be null");
        }
        this.email = emailParam;
        this.displayName = displayNameParam;
        this.clientNode = clientNodeParam;
    }

    /**
     * Gets the email address.
     *
     * @return The email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the display name.
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the {@link ClientNode}.
     *
     * @return client node details
     */
    public ClientNode getClientNode() {
        System.out.println("Getting client node: " + clientNode);
        return clientNode;
    }

    /**
     * Serializes the LEAVEPacket into a byte array.
     * Format:
     * - 1 byte: packet type (MeetingPacketType.LEAVE.ordinal())
     * - 4 bytes: IP address (as int, network byte order)
     * - 2 bytes: port (as short)
     * - 4 bytes: email length (int)
     * - N bytes: email string (UTF-8)
     * - 4 bytes: displayName length (int)
     * - M bytes: displayName string (UTF-8)
     *
     * @return The serialized byte array
     */
    public byte[] serialize() {
        try {
            final InetAddress ipAddress = InetAddress.getByName(clientNode.hostName());
            final byte[] ipBytes = ipAddress.getAddress();
            if (ipBytes.length != IPV4_BYTE_SIZE) {
                throw new IllegalArgumentException("Only IPv4 addresses are supported");
            }

            final byte[] emailBytes = email.getBytes(StandardCharsets.UTF_8);
            final byte[] displayNameBytes = displayName.getBytes(StandardCharsets.UTF_8);
            final ByteBuffer buffer = ByteBuffer.allocate(1 + IPV4_BYTE_SIZE + 2 + IPV4_BYTE_SIZE
                    + emailBytes.length + IPV4_BYTE_SIZE + displayNameBytes.length);

            buffer.put((byte) MeetingPacketType.LEAVE.ordinal());
            buffer.put(ipBytes); // 4 bytes for IPv4
            buffer.putShort((short) clientNode.port()); // 2 bytes for port
            buffer.putInt(emailBytes.length); // 4 bytes for email length
            buffer.put(emailBytes); // Email bytes
            buffer.putInt(displayNameBytes.length); // 4 bytes for displayName length
            buffer.put(displayNameBytes); // DisplayName bytes

            return buffer.array();
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address: " + clientNode.hostName(), e);
        }
    }

    /**
     * Deserializes a byte array into an LEAVEPacket.
     *
     * @param data The byte array to deserialize
     * @return The deserialized LEAVEPacket
     * @throws InvalidParameterException If the packet type is invalid or data is
     *                                   malformed
     */
    /**
     * Deserializes a byte array into an LEAVEPacket.
     *
     * @param data The byte array to deserialize
     * @return The deserialized LEAVEPacket
     * @throws InvalidParameterException If the packet type is invalid or data is
     *                                   malformed
     */
    // CHECKSTYLE:OFF: CyclomaticComplexity
    // CHECKSTYLE:OFF: NPathComplexity
    public static ILeavePacket deserialize(final byte[] data) {
        if (data == null || data.length < MIN_PACKET_SIZE) {
            throw new InvalidParameterException("Invalid data: too short for LEAVEPacket (need at least "
                    + MIN_PACKET_SIZE + " bytes)");
        }

        final ByteBuffer buffer = ByteBuffer.wrap(data);

        final byte packetType = buffer.get();
        if (packetType != MeetingPacketType.LEAVE.ordinal()) {
            throw new InvalidParameterException(
                    "Invalid packet type: Expected " + MeetingPacketType.LEAVE.ordinal() + " got: " + packetType);
        }

        // Read IP address (4 bytes)
        if (buffer.remaining() < IPV4_BYTE_SIZE) {
            throw new InvalidParameterException("Insufficient data for IP address");
        }
        final byte[] ipBytes = new byte[IPV4_BYTE_SIZE];
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
        if (buffer.remaining() < IPV4_BYTE_SIZE) {
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
        if (buffer.remaining() < IPV4_BYTE_SIZE) {
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

        return new ILeavePacket(email, displayName, new ClientNode(host, port));
    }
    // CHECKSTYLE:ON: CyclomaticComplexity
    // CHECKSTYLE:ON: NPathComplexity
}
