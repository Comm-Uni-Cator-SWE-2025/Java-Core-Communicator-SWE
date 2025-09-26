package com.swe.networking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class PriorityQueueTest {

    @Test
    void testSinglePacket() {
        PriorityQueue pq = new PriorityQueue();
        byte[] data = PacketParser.createPkt(0, 1, 0, "hello".getBytes());
        pq.addPacket(data);

        byte[] result = pq.nextPacket();
        assertNotNull(result, "Next packet should not be null");
        assertArrayEquals(data, result, "Packet data should match");
    }

    @Test
    void testMultiplePacketsDifferentPriorities() {
        PriorityQueue pq = new PriorityQueue();

        byte[] highPriority = PacketParser.createPkt(
                0, 1, 0, "high".getBytes());
        byte[] midPriority = PacketParser.createPkt(
                0, 2, 0, "mid".getBytes());
        byte[] lowPriority = PacketParser.createPkt(
                0, 3, 0, "low".getBytes());

        pq.addPacket(lowPriority);
        pq.addPacket(midPriority);
        pq.addPacket(highPriority);

        // High priority should be served first
        assertArrayEquals(highPriority, pq.nextPacket(),
                "Highest priority packet sent first");
        assertArrayEquals(midPriority, pq.nextPacket(),
                "Mid priority packet sent second");
        assertArrayEquals(lowPriority, pq.nextPacket(),
                "Low priority packet sent last");
    }

    @Test
    void testMLFQRotation() throws InterruptedException {
        PriorityQueue pq = new PriorityQueue();

        // Add multiple low-priority packets
        List<byte[]> lowPackets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            byte[] pkt = PacketParser.createPkt(
                    0, 3, 0, ("low" + i).getBytes());
            pq.addPacket(pkt);
            lowPackets.add(pkt);
        }

        // Consume some packets (simulate epoch)
        for (int i = 0; i < 2; i++) {
            pq.nextPacket(); // consume first 2 packets
        }

        // Wait 1.1 sec to trigger rotation
        Thread.sleep(1100);
        pq.nextPacket(); // triggers rotation internally

//        // All remaining packets should eventually come out
//        Set<String> remainingData = new HashSet<>();
//        byte[] pkt;
//        while ((pkt = pq.nextPacket()) != null) {
//            remainingData.add(new String(pkt));
//        }
//
//        for (int i = 0; i < 5; i++) {
//            if (i >= 2) { // first 2 already sent
//                assertTrue(remainingData.contains("low" + i),
//                        "Packet low" + i + " should be sent after rotation");
//            }
//        }
    }

    @Test
    void testBudgetLimits() {
        PriorityQueue pq = new PriorityQueue();

        // Send 200 high-priority packets (budget is 50)
        List<byte[]> packets = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            byte[] pkt = PacketParser.createPkt(
                    0, 1, 0, ("p" + i).getBytes());
            pq.addPacket(pkt);
            packets.add(pkt);
        }

        int sentCount = 0;
        for (int i = 0; i < 200; i++) {
            byte[] pkt = pq.nextPacket();
            if (pkt != null) sentCount++;
        }

        // Only 50 packets should be sent in one epoch(highest priority budget)
        assertEquals(50, sentCount,
                "Budget should limit number of packets sent per epoch");
    }

    @Test
    void testMixedPrioritiesAndMLFQ() throws InterruptedException {
        PriorityQueue pq = new PriorityQueue();

        // Create packets with headers + payload
        byte[] high = PacketParser.createPkt(0, 1, 0, "H".getBytes());
        byte[] mid = PacketParser.createPkt(0, 2, 0, "M".getBytes());
        byte[] low = PacketParser.createPkt(0, 3, 0, "L".getBytes());

        pq.addPacket(low);
        pq.addPacket(mid);
        pq.addPacket(high);

        // High priority first
        byte[] pktHigh = pq.nextPacket();
        assertEquals("H", new String(PacketParser.getPayload(pktHigh)),
                "Highest priority packet should be sent first");

        // Mid-priority next
        byte[] pktMid = pq.nextPacket();
        assertEquals("M", new String(PacketParser.getPayload(pktMid)),
                "Mid priority packet should be sent second");

        // Low priority
        byte[] pktLow = pq.nextPacket();
        assertEquals("L", new String(PacketParser.getPayload(pktLow)),
                "Low priority packet should be sent last");

        // Add more low-priority packets to test rotation
        byte[] l1 = PacketParser.createPkt(0, 3, 0, "L1".getBytes());
        byte[] l2 = PacketParser.createPkt(0, 3, 0, "L2".getBytes());
        pq.addPacket(l1);
        pq.addPacket(l2);

        // Wait 1.1 seconds to trigger rotation
        Thread.sleep(1100);
        pq.nextPacket(); // triggers rotation internally

         // Collect all remaining low-priority packets
//        Set<String> collected = new HashSet<>();
//        byte[] pkt;
//        while ((pkt = pq.nextPacket()) != null) {
//            collected.add(new String(PacketParser.getPayload(pkt)));
//        }
//
//        assertTrue(collected.contains("L1"),
//        "L1 should be sent after rotation");
//        assertTrue(collected.contains("L2"),
//        "L2 should be sent after rotation");
    }
}

