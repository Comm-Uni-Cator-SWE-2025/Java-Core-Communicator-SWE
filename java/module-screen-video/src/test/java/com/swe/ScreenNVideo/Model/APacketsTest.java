package com.swe.ScreenNVideo.Model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

class APacketsTest {

    @Test
    void testSerializeAndDeserialize() throws IOException {
        byte[] pcmData = new byte[]{10, 20, 30, 40};

        APackets original = new APackets(
                42,
                pcmData,
                "192.168.1.5",
                1234,
                7
        );

        byte[] serialized = original.serializeAPackets();
        APackets deserialized = APackets.deserialize(serialized);

        assertEquals(original.packetNumber(), deserialized.packetNumber());
        assertEquals(original.ip(), deserialized.ip());
        assertEquals(original.predictedPCM(), deserialized.predictedPCM());
        assertEquals(original.indexPCM(), deserialized.indexPCM());
        assertArrayEquals(original.data(), deserialized.data());
    }

    @Test
    void testWrongPacketTypeThrows() {
        byte[] invalid = new byte[]{99, 0, 0, 0, 0}; // wrong type

        assertThrows(InvalidParameterException.class, () -> {
            APackets.deserialize(invalid);
        });
    }

    @Test
    void testSerializeFormatIsCorrect() throws IOException {
        byte[] pcm = new byte[]{1, 2};

        APackets packet = new APackets(
                10,
                pcm,
                "1.2.3.4",
                55,
                7
        );

        byte[] serialized = packet.serializeAPackets();

        assertEquals(1 + 7 * Integer.BYTES + pcm.length, serialized.length);

        // Check packet type byte
        assertEquals((byte) NetworkPacketType.APACKETS.ordinal(), serialized[0]);
    }

    @Test
    void testDeserializeParsesIpCorrectly() {
        byte[] serialized = new byte[]{
                (byte) NetworkPacketType.APACKETS.ordinal(),

                // IP = 192.168.1.50 → 4 ints → 16 bytes
                0,0,0,(byte)192,
                0,0,0,(byte)168,
                0,0,0,1,
                0,0,0,50,

                // packetNumber = 5
                0,0,0,5,

                // predictedPCM = 1024
                0,0,4,0,

                // indexPCM = 3
                0,0,0,3,

                // audio bytes
                10,20,30
        };

        APackets ap = APackets.deserialize(serialized);

        assertEquals("192.168.1.50", ap.ip());
        assertEquals(5, ap.packetNumber());
        assertEquals(1024, ap.predictedPCM());
        assertEquals(3, ap.indexPCM());
        assertArrayEquals(new byte[]{10,20,30}, ap.data());
    }

}
