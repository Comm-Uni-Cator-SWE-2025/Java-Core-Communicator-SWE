package com.swe.ScreenNVideo.PatchGenerator;

import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.ByteBuffer;

public class CompressedPatchTest {

    @Test
    void testSerializePacketFormat() {
        byte[] data = new byte[]{10, 20, 30};
        CompressedPatch patch = new CompressedPatch(5, 6, 7, 8, data);

        byte[] serialized = patch.serializeCPacket();

        // lenRequired = data.length + 16
        int expectedLenRequired = data.length + 16;       // 3 + 16 = 19
        int expectedTotal = expectedLenRequired + 4;      // +4 for length prefix = 23

        assertEquals(expectedTotal, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);

        assertEquals(expectedLenRequired, buffer.getInt()); // length prefix
        assertEquals(5, buffer.getInt()); // x
        assertEquals(6, buffer.getInt()); // y
        assertEquals(7, buffer.getInt()); // width
        assertEquals(8, buffer.getInt()); // height

        byte[] dataRead = new byte[data.length];
        buffer.get(dataRead);

        assertArrayEquals(data, dataRead);
    }

    @Test
    void testDeserializeFromByteArray() {
        byte[] data = new byte[]{1, 2, 3, 4};
        CompressedPatch original = new CompressedPatch(10, 20, 30, 40, data);

        byte[] serialized = original.serializeCPacket();

        CompressedPatch result = CompressedPatch.deserializeCPacket(serialized);

        assertNotNull(result);
        assertEquals(10, result.x());
        assertEquals(20, result.y());
        assertEquals(30, result.width());
        assertEquals(40, result.height());
        assertArrayEquals(data, result.data());
    }

    @Test
    void testDeserializeFromByteBuffer() {
        byte[] data = new byte[]{9, 8, 7};
        CompressedPatch original = new CompressedPatch(1, 2, 3, 4, data);
        byte[] serialized = original.serializeCPacket();

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        int packetLength = buffer.getInt(); // read length prefix as deserializer expects

        CompressedPatch result = CompressedPatch.deserializeCPacket(buffer, packetLength);

        assertEquals(1, result.x());
        assertEquals(2, result.y());
        assertEquals(3, result.width());
        assertEquals(4, result.height());
        assertArrayEquals(data, result.data());
    }

    @Test
    void testBufferOverflowReturnsNull() {
        // Create a correct packet
        byte[] data = new byte[]{1,2,3};
        CompressedPatch original = new CompressedPatch(5, 6, 7, 8, data);
        byte[] serialized = original.serializeCPacket();

        // Corrupt packet by making packetLength too large
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        int packetLength = 9999; // too large

        CompressedPatch result = CompressedPatch.deserializeCPacket(buffer, packetLength);

        assertNull(result, "Should return null when buffer does not contain enough bytes");
    }

    @Test
    void testRoundTripSerialization() {
        byte[] data = new byte[]{100, 101, 102, 103, 104};

        CompressedPatch patch = new CompressedPatch(
                111, 222, 333, 444, data
        );

        byte[] serialized = patch.serializeCPacket();
        CompressedPatch restored = CompressedPatch.deserializeCPacket(serialized);

        assertEquals(patch.x(), restored.x());
        assertEquals(patch.y(), restored.y());
        assertEquals(patch.width(), restored.width());
        assertEquals(patch.height(), restored.height());
        assertArrayEquals(patch.data(), restored.data());
    }
}
