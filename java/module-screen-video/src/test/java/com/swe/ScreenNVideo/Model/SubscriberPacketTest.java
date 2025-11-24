/**
 * Contributed by @Bhupati-Varun
 */

package com.swe.ScreenNVideo.Model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class SubscriberPacketTest {

    @Test
    @DisplayName("Serialization should produce correct byte structure")
    void testSerialize() {
        // Setup
        String email = "test@example.com";
        boolean reqCompression = true;
        SubscriberPacket packet = new SubscriberPacket(email, reqCompression);

        // Action
        byte[] data = packet.serialize();

        // Verification
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // 1. Check Compression Byte (Logic: reqCompression ? 1 : 0)
        byte compressionByte = buffer.get();
        assertEquals(1, compressionByte, "True should be serialized as 1");

        // 2. Check Email Bytes
        byte[] emailBytes = new byte[buffer.remaining()];
        buffer.get(emailBytes);
        assertEquals(email, new String(emailBytes), "Email string should match");
    }

    @Test
    @DisplayName("Deserialize should reconstruct object from byte array")
    void testDeserialize() {
        // Setup raw bytes manually
        String email = "user@domain.com";
        byte[] emailBytes = email.getBytes();

        // Structure: [CompressionByte (0/1)] + [EmailBytes]
        ByteBuffer buffer = ByteBuffer.allocate(1 + emailBytes.length);
        buffer.put((byte) 0); // 0 for false
        buffer.put(emailBytes);
        byte[] inputData = buffer.array();

        // Action
        SubscriberPacket packet = SubscriberPacket.deserialize(inputData);

        // Verify
        assertEquals(email, packet.email());
        assertFalse(packet.reqCompression());
    }

    @Test
    @DisplayName("Round Trip: Serialize then Deserialize should return identical object")
    void testRoundTrip() {
        // Setup
        SubscriberPacket original = new SubscriberPacket("roundtrip@test.com", true);

        // Action
        byte[] serializedData = original.serialize();
        SubscriberPacket restored = SubscriberPacket.deserialize(serializedData);

        // Verify
        assertEquals(original, restored, "Deserialized object should equal original record");
        assertEquals(original.email(), restored.email());
        assertEquals(original.reqCompression(), restored.reqCompression());
    }
}