package com.swe.controller.serializer;

/**
 * Enum representing the types of meeting-related network packets.
 */
public enum MeetingPacketType {
    /**
     * Packet sent by a new joinee to request joining a meeting.
     */
    JOIN,
    /**
     * Packet sent by the server in response to a JOIN request,
     * containing the mapping from IP address to email address.
     */
    JOINACK,
    /**
     * Packet broadcast by participants to announce their presence.
     */
    ANNOUNCE
}

