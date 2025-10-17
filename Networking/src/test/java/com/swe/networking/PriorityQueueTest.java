package com.swe.networking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class PriorityQueueTest {

    @Test
    void testSinglePacket() throws UnknownHostException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        byte[] data = parser.createPkt(0, 1, 0, 0, 0, ip, 0, 0, 0, 0, "hello".getBytes());
        pq.addPacket(data);

        byte[] result = pq.nextPacket();
        assertNotNull(result, "Next packet should not be null");
        assertArrayEquals(data, result, "Packet data should match");
    }

    @Test
    void testMultiplePacketsDifferentPriorities() throws UnknownHostException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        byte[] highPriority = parser.createPkt(
                0, 1, 0, 0, 0, ip, 0, 0, 0, 0, "high".getBytes());
        byte[] midPriority = parser.createPkt(
                0, 2, 0, 0, 0, ip, 0, 0, 0, 0, "medium".getBytes());
        byte[] lowPriority = parser.createPkt(
                0, 3, 0, 0, 0, ip, 0, 0, 0, 0, "lowest".getBytes());


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
    void testMLFQRotation() throws InterruptedException, UnknownHostException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        // Add multiple low-priority packets
        List<byte[]> lowPackets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            byte[] pkt = parser.createPkt(
                    0, 3, 0, 0, 0, ip, 0, 0, 0, 0, ("low" + i).getBytes());
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
    void testBudgetLimits() throws UnknownHostException, InterruptedException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");

        // Send 200 high-priority packets (budget is 50)
        for (int i = 0; i < 200; i++) {
            byte[] pkt = parser.createPkt(
                    0, 1, 0,0, 0, ip, 0, 0, i, 0, ("p" + i).getBytes());
            pq.addPacket(pkt);
        }
        long t1 = System.currentTimeMillis();
        int sentCount = 0;
        for (int i = 0; i < 200; i++) {
            byte[] pkt = pq.nextPacket();
            if (pkt != null) {
                sentCount++;
                System.out.println(parser.getChunkNum(pkt));
            }
        }
        long t2 = System.currentTimeMillis();

        // Only 50 packets should be sent in one epoch(highest priority budget)
        assertEquals(50, sentCount,
                "Budget should limit number of packets sent per epoch");

        System.out.println(t2-t1 + " ms");

        Thread.sleep(110);

        for(int i = 0; i < 150; i++){
            byte[] pkt = pq.nextPacket();
            if(pkt != null){
                sentCount++;
                System.out.println(parser.getChunkNum(pkt));
            }
        }

        assertEquals(100, sentCount, "Budget should limit number of packets sent per epoch");
    }

    @Test
    void testMixedChunkNumber() throws InterruptedException, UnknownHostException{
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = PacketParser.getPacketParser();
        InetAddress ip = InetAddress.getByName("0.0.0.0");

        for(int i = 0; i < 200; i++){
            byte[] hpkt = parser.createPkt(0, 1, 0, 0, 0, ip, 0, 0, i, 0, ("p" + i).getBytes());
            byte[] mpkt = parser.createPkt(0, 2, 0, 0, 0, ip, 0, 0, i, 0, ("p" + i).getBytes());
            byte[] lpkt = parser.createPkt(0, 3, 0, 0, 0, ip, 0, 0, i, 0, ("p" + i).getBytes());

            pq.addPacket(hpkt);
            pq.addPacket(mpkt);
            pq.addPacket(lpkt);
        }

        int sentCount = 0;
        for (int i = 0; i < 200; i++) {
            byte[] pkt = pq.nextPacket();
            if (pkt != null) {
                sentCount++;
                System.out.println(parser.getPriority(pkt) + " " + parser.getChunkNum(pkt));
            }
        }

        // Only 100 packets should be sent in one epoch(highest priority budget)
        assertEquals(100, sentCount,
                "Budget should limit number of packets sent per epoch");

        Thread.sleep(1100);

        byte[] pkt1 = pq.nextPacket();
        if(pkt1 != null){
            sentCount++;
            System.out.println(parser.getPriority(pkt1) + " " + parser.getChunkNum(pkt1));
        }
        for(int i = 0; i < 20; i++){
            byte[] lpkt = parser.createPkt(0, 3, 0, 0, 0, ip, 0, 0, i, 0, ("p" + i).getBytes());
            pq.addPacket(lpkt);
        }
        for (int i = 0; i < 200; i++) {
            byte[] pkt = pq.nextPacket();
            if (pkt != null) {
                sentCount++;
                System.out.println(parser.getPriority(pkt) + " " + parser.getChunkNum(pkt));
            }
        }

        // Only 100 packets should be sent in one epoch(highest priority budget)
        assertEquals(200, sentCount,
                "Budget should limit number of packets sent per epoch");

    }

    @Test
    void testConcurrentAccessAndOrder() throws InterruptedException, UnknownHostException, ExecutionException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = PacketParser.getPacketParser(); // Assuming you fixed the static/non-static parser issue

        final int NUM_PACKETS_PER_PRIORITY = 200;
        final int NUM_THREADS = 10;
        final AtomicInteger globalChunkCounter = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        ConcurrentLinkedQueue<String> sentPacketsLog = new ConcurrentLinkedQueue<>();

        InetAddress ip = InetAddress.getByName("0.0.0.0");

        // --- 1. Concurrent Packet Addition (Sender Threads) ---
        System.out.println("--- STARTING CONCURRENT ADDITION ---");

        Callable<Void> senderTask = () -> {
            // Each thread adds a small, staggered batch of packets to simulate bursty traffic
            for (int i = 0; i < NUM_PACKETS_PER_PRIORITY / 2; i++) {

                int uniqueChunkId = globalChunkCounter.getAndIncrement();
                byte[] hpkt = parser.createPkt(0, 1, 0, 0, 0, ip, 0, 0, uniqueChunkId, 0, ("HP-" + i).getBytes());
                pq.addPacket(hpkt);

                byte[] mpkt = parser.createPkt(0, 2, 0, 0, 0, ip, 0, 0, uniqueChunkId, 0, ("MP-" + i).getBytes());
                pq.addPacket(mpkt);

                byte[] lpkt = parser.createPkt(0, 3, 0, 0, 0, ip, 0, 0, uniqueChunkId, 0, ("LP-" + i).getBytes());
                pq.addPacket(lpkt);

                Thread.sleep(1);
            }
            return null;
        };

        // Submit the sender tasks (e.g., 4 threads sending batches)
        List<Future<Void>> sendFutures = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            sendFutures.add(executor.submit(senderTask));
        }

        // Wait for all sending threads to complete
        for (Future<Void> future : sendFutures) {
            future.get();
        }

        // --- 2. Concurrent Packet Retrieval (Receiver Threads) ---
        System.out.println("\n--- STARTING CONCURRENT RETRIEVAL (Budget Test) ---");

        Callable<Void> receiverTask = () -> {
            for (int i = 0; i < (NUM_PACKETS_PER_PRIORITY * 3) / 2; i++) {
                byte[] pkt = pq.nextPacket();
                if (pkt != null) {
                    int priority = parser.getPriority(pkt);
                    int chunkNum = parser.getChunkNum(pkt);
//                    System.out.println(priority+ " " + chunkNum);
                    String logEntry = String.format("P: %d, Chunk: %d, Thread: %s",
                            priority, chunkNum, Thread.currentThread().getName());
                    sentPacketsLog.add(logEntry);
                }
                // Wait to ensure interleaving of nextPacket() calls
//                Thread.sleep(1);
            }
            return null;
        };

        // Submit receiver tasks
        List<Future<Void>> receiveFutures = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            receiveFutures.add(executor.submit(receiverTask));
        }

        // Wait for all receiver threads to complete their attempts
        for (Future<Void> future : receiveFutures) {
            future.get();
        }

        // --- 3. Analysis ---
        System.out.println("\n--- SENT PACKET LOG ---");
        int totalSent = sentPacketsLog.size();

        // Print the final order
        for (String log : sentPacketsLog) {
            System.out.println(log);
        }

        // Assertion remains the same for the first epoch total budget
        // Note: The budget is now consumed by multiple threads.
        assertEquals(100, totalSent,
                "Total packets sent in first epoch should match TOTAL_BUDGET (100)");

        executor.shutdown();
    }

//    @Test
//    void testMixedPrioritiesAndMLFQ() throws InterruptedException {
//        PriorityQueue pq = new PriorityQueue();
//
//        // Create packets with headers + payload
//        byte[] high = PacketParser.createPkt(0, 1, 0, "H".getBytes());
//        byte[] mid = PacketParser.createPkt(0, 2, 0, "M".getBytes());
//        byte[] low = PacketParser.createPkt(0, 3, 0, "L".getBytes());
//
//        pq.addPacket(low);
//        pq.addPacket(mid);
//        pq.addPacket(high);
//
//        // High priority first
//        byte[] pktHigh = pq.nextPacket();
//        assertEquals("H", new String(PacketParser.getPayload(pktHigh)),
//                "Highest priority packet should be sent first");
//
//        // Mid-priority next
//        byte[] pktMid = pq.nextPacket();
//        assertEquals("M", new String(PacketParser.getPayload(pktMid)),
//                "Mid priority packet should be sent second");
//
//        // Low priority
//        byte[] pktLow = pq.nextPacket();
//        assertEquals("L", new String(PacketParser.getPayload(pktLow)),
//                "Low priority packet should be sent last");
//
//        // Add more low-priority packets to test rotation
//        byte[] l1 = PacketParser.createPkt(0, 3, 0, "L1".getBytes());
//        byte[] l2 = PacketParser.createPkt(0, 3, 0, "L2".getBytes());
//        pq.addPacket(l1);
//        pq.addPacket(l2);
//
//        // Wait 1.1 seconds to trigger rotation
//        Thread.sleep(1100);
//        pq.nextPacket(); // triggers rotation internally

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
//    }
}

