package com.swe.controller.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MeetingPacketTypeTest {

    @Test
    void enumContainsExpectedValues() {
        assertEquals(2, MeetingPacketType.values().length);
        assertEquals(MeetingPacketType.IAM, MeetingPacketType.valueOf("IAM"));
        assertEquals(MeetingPacketType.JOINACK, MeetingPacketType.valueOf("JOINACK"));
    }
}

