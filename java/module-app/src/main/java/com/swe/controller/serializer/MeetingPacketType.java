package com.swe.controller.serializer;

/**
 * Enum representing the types of meeting-related network packets.
 */
public enum MeetingPacketType {
    /**
     * Packet sent by participants to announce their presence.
     * When sent to the server, it serves as a join request.
     * When broadcast to peers, it serves as an announcement.
     */
    IAM,
    /**
     * Packet sent by the server in response to an IAM request,
     * containing the mapping from IP address to email address.
     */
    JOINACK
}

