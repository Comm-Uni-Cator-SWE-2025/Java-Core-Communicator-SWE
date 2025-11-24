// module-chat/src/main/java/com/swe/chat/ChatProtocol.java

package com.swe.chat;

/**
 * UTILITY: Holds constants and byte-level logic (protocol and flags).
 * Adheres to SRP by isolating protocol knowledge.
 */
public class ChatProtocol {
    public static final byte FLAG_TEXT_MESSAGE = (byte) 0x01;
    public static final byte FLAG_FILE_MESSAGE = (byte) 0x02;
    public static final byte FLAG_FILE_METADATA = (byte) 0x03;
    public static final byte FLAG_DELETE_MESSAGE = (byte) 0x04;

    private ChatProtocol() { /* Utility class */ }

    /**
     * Utility method to wrap data with the protocol flag.
     */
    public static byte[] addProtocolFlag(byte[] data, byte flag) {
        if (data == null) data = new byte[0];
        byte[] flaggedPacket = new byte[data.length + 1];
        flaggedPacket[0] = flag;
        System.arraycopy(data, 0, flaggedPacket, 1, data.length);
        return flaggedPacket;
    }
}