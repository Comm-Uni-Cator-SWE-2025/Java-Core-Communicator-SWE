package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChatProtocolTest {

    @Test
    void addProtocolFlagPrefixesFlagAndCopiesPayload() {
        byte[] payload = {10, 20, 30};

        byte[] flagged = ChatProtocol.addProtocolFlag(payload, ChatProtocol.FLAG_TEXT_MESSAGE);

        assertEquals(ChatProtocol.FLAG_TEXT_MESSAGE, flagged[0]);
        assertArrayEquals(payload, new byte[] {flagged[1], flagged[2], flagged[3]});
    }

    @Test
    void addProtocolFlagHandlesNullData() {
        byte[] flagged = ChatProtocol.addProtocolFlag(null, ChatProtocol.FLAG_FILE_MESSAGE);

        assertEquals(1, flagged.length);
        assertEquals(ChatProtocol.FLAG_FILE_MESSAGE, flagged[0]);
    }

    @Test
    void addProtocolFlagUsesDefensiveCopy() {
        byte[] payload = {1, 2};
        byte[] flagged = ChatProtocol.addProtocolFlag(payload, ChatProtocol.FLAG_DELETE_MESSAGE);

        payload[0] = 99;
        assertEquals(ChatProtocol.FLAG_DELETE_MESSAGE, flagged[0]);
        assertArrayEquals(new byte[] {1, 2}, new byte[] {flagged[1], flagged[2]});
    }
}

