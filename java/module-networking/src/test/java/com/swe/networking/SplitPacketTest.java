package com.swe.networking;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for splitting the packets.
 */
public class SplitPacketTest {
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        SplitPackets.getSplitPackets().emptyBuffer();
    }

    @org.junit.jupiter.api.Test
    void testSingleCompletePacket() {
        final SplitPackets splitter = SplitPackets.getSplitPackets();
        final byte[] packet = new byte[] { 0x06, 0x00, 1, 2, 3, 4 };
        final ReceivePacket receivePacket = new ReceivePacket(null, packet);
        final List<byte[]> result = splitter.split(receivePacket);
        assertEquals(1, result.size());
        System.out.println(Arrays.toString(result.get(0)));
        assertArrayEquals(new byte[] { 0x06, 0x00, 1, 2, 3, 4 }, result.get(0));
    }

    @org.junit.jupiter.api.Test
    void testMultipleCompletePackets() {
        final SplitPackets splitter = SplitPackets.getSplitPackets();
        final byte[] data = new byte[] {
                0x06, 0x00, 1, 2, 3, 4,
                0x05, 0x00, 5, 6, 7
        };
        final ReceivePacket receivePacket = new ReceivePacket(null, data);
        final List<byte[]> result = splitter.split(receivePacket);
        System.out.println(Arrays.toString(result.get(0)));
        System.out.println(Arrays.toString(result.get(1)));
        assertEquals(2, result.size());
        assertArrayEquals(new byte[] { 0x06, 0x00, 1, 2, 3, 4 }, result.get(0));
        assertArrayEquals(new byte[] { 0x05, 0x00, 5, 6, 7 }, result.get(1));
    }

    @org.junit.jupiter.api.Test
    void testFragmentedPacketAcrossCalls() {
        final SplitPackets splitter = SplitPackets.getSplitPackets();

        final byte[] part1 = new byte[] { 0x08, 0x00, 9, 8 };
        final ReceivePacket receivePacket1 = new ReceivePacket(null, part1);
        final List<byte[]> result1 = splitter.split(receivePacket1);
        assertTrue(result1.isEmpty());
        final byte[] part2 = new byte[] { 7, 6, 5, 4 };
        final ReceivePacket receivePacket2 = new ReceivePacket(null, part2);
        final List<byte[]> result2 = splitter.split(receivePacket2);
        assertEquals(1, result2.size());
        assertArrayEquals(new byte[] { 0x08, 0x00, 9, 8, 7, 6, 5, 4 }, result2.get(0));
    }

    @org.junit.jupiter.api.Test
    void testMultiplePacketsWithLastFragmented() {
        final SplitPackets splitter = SplitPackets.getSplitPackets();

        final byte[] data = new byte[] { 0x05, 0x00, 11, 12, 13, 0x07, 0x00, 14 };
        final ReceivePacket receivePacket1 = new ReceivePacket(null, data);
        final List<byte[]> result = splitter.split(receivePacket1);
        assertEquals(1, result.size());
        assertArrayEquals(new byte[] { 0x05, 0x00, 11, 12, 13 }, result.get(0));
        final byte[] remainder = new byte[] { 15, 16, 17, 18 };
        final ReceivePacket receivePacket2 = new ReceivePacket(null, remainder);
        final List<byte[]> result2 = splitter.split(receivePacket2);
        assertEquals(1, result2.size());
        assertArrayEquals(new byte[] { 0x07, 0x00, 14, 15, 16, 17, 18 }, result2.get(0));
    }

    @org.junit.jupiter.api.Test
    void testEmptyData() {
        final SplitPackets splitter = SplitPackets.getSplitPackets();
        final byte[] empty = new byte[0];
        final ReceivePacket receivePacket = new ReceivePacket(null, empty);
        final List<byte[]> result = splitter.split(receivePacket);
        assertTrue(result.isEmpty());
    }

    @org.junit.jupiter.api.Test
    void testEmptyBuffer() {
        final SplitPackets splitter = SplitPackets.getSplitPackets();
        // Note: @BeforeEach's setUp method ensures the buffer is initially empty.

        // 1. Send a fragmented packet. This will populate the incompleteBuffer.
        final byte[] part1 = new byte[] { 0x08, 0x00, 1, 2, 3, 4, 5 }; // An 8-byte packet, we send 7 bytes.
        final ReceivePacket receivePacket1 = new ReceivePacket(null, part1);
        List<byte[]> result1 = splitter.split(receivePacket1);
        assertTrue(result1.isEmpty(), "No complete packet should be found, and the buffer should be populated.");

        // 2. Call emptyBuffer() to clear the state.
        splitter.emptyBuffer();

        // 3. Send the final byte of the original packet.
        final byte[] part2 = new byte[] { 6 };
        final ReceivePacket receivePacket2 = new ReceivePacket(null, part2);
        List<byte[]> result2 = splitter.split(receivePacket2);

        // 4. Assert that no packet is created.
        // If emptyBuffer() worked, part1 is gone, and part2 is just junk, so no packet should be produced.
        // If emptyBuffer() did NOT work, the splitter would combine part1 and part2 and create a packet, failing the test.
        assertTrue(result2.isEmpty(), "The buffer should have been cleared, so no packet should be formed.");
    }
}
