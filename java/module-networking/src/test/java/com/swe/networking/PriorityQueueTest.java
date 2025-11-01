package com.swe.networking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class PriorityQueueTest {

    // Helper method to create a dummy PacketParser instance
    private PacketParser getParser() {
        return PacketParser.getPacketParser();
    }

    // Helper method for creating packets
    private byte[] createTestPkt(PacketParser parser, int priority, int chunkNum, String payload)
            throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("0.0.0.0");
        return parser.createPkt(0, priority, 0, 0, 0, ip, 0, 0, chunkNum, 0, payload.getBytes());
    }

    //-------------------------------------------------------------------------
    // BASIC FUNCTIONALITY TESTS
    //-------------------------------------------------------------------------

    @Test
    void testSinglePacket() throws UnknownHostException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = getParser();
        byte[] data = createTestPkt(parser, 1, 0, "hello");
        pq.addPacket(data);

        byte[] result = pq.nextPacket();
        assertNotNull(result, "Next packet should not be null");
        assertArrayEquals(data, result, "Packet data should match");
    }

    @Test
    void testFromLevelInvalid() {
        // The enum defines levels 1 through 8.
        final int invalidLevel = 9;

        // The lambda expression inside assertThrows is the code being tested.
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    PriorityQueue.PacketPriority.fromLevel(invalidLevel);
                },
                "Should throw an IllegalArgumentException for an invalid priority level."
        );

        String expectedMessage = "Invalid priority level: " + invalidLevel;
        assertTrue(exception.getMessage().contains(expectedMessage),
                "The exception message should contain the invalid level.");
    }

    @Test
    void testMultiplePacketsDifferentPriorities() throws UnknownHostException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = getParser();

        byte[] highPriority = createTestPkt(parser, 1, 0, "high");
        byte[] midPriority = createTestPkt(parser, 2, 0, "medium");
        byte[] lowPriority = createTestPkt(parser, 3, 0, "lowest");

        pq.addPacket(lowPriority);
        pq.addPacket(midPriority);
        pq.addPacket(highPriority);

        // P1 > P2 > P3 order
        assertArrayEquals(highPriority, pq.nextPacket(), "Highest priority packet sent first");
        assertArrayEquals(midPriority, pq.nextPacket(), "Mid priority packet sent second");
        assertArrayEquals(lowPriority, pq.nextPacket(), "Low priority packet sent last");
    }

    @Test
    void testBudgetLimits() throws UnknownHostException, InterruptedException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = getParser();

        // Add 200 high-priority packets (budget is 50)
        for (int i = 0; i < 10000; i++) {
            pq.addPacket(createTestPkt(parser, 1, i, "p" + i));
            pq.addPacket(createTestPkt(parser, 2, 10000 + i, "mp" + i));
            pq.addPacket(createTestPkt(parser, 3, 20000+i, "lp" + i));
        }

        int sentCount = 0;
        long t1 = System.currentTimeMillis();
        // First consumption loop (should hit the 50 budget limit)
        for (int i = 0; i < 30000; i++) {
            byte[] pkt = pq.nextPacket();
            if (pkt != null) {
                sentCount++;
            }
        }
        long t2 = System.currentTimeMillis();


        System.out.println("packets " + sentCount + " time " + (t2-t1));
    }

    //-------------------------------------------------------------------------
    // MLFQ & ROTATION TESTS
    //-------------------------------------------------------------------------

    @Test
    void testRotation() throws UnknownHostException, InterruptedException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = getParser();

        for(int i = 0; i < 4000; i++){
            pq.addPacket(createTestPkt(parser, 3, i, "LP"));
        }

        for(int i = 0; i < 400; i++){
            byte[] pkt = pq.nextPacket();
        }

        Thread.sleep(1100);
        pq.nextPacket();

        for(int i = 0; i < 1000; i++){
            pq.addPacket(createTestPkt(parser, 3, 4000+i, "LP"));
        }

        for(int i = 0; i < 2900; i++){
            byte[] pkt = pq.nextPacket();
            if (pkt != null) {
                System.out.println(parser.getChunkNum(pkt));
            }
        }

        Thread.sleep(1100);

        pq.nextPacket();
    }

    @Test
    void testAggressiveMLFQSurvival() throws UnknownHostException, InterruptedException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = getParser();
        final int LP_BUDGET = 20;

        // --- Phase 1: Initial Load ---
        // Load 40 LP packets (P0-P39) to ensure 20 packets remain after the first epoch.
        for (int i = 0; i < 40; i++) {
            pq.addPacket(createTestPkt(parser, 3, i, "LP" + i));
        }
        // Add HP/MP traffic to ensure P3 only gets its 20 tokens
        for (int i = 0; i < 50; i++) { pq.addPacket(createTestPkt(parser, 1, 100+i, "HP")); }
        for (int i = 0; i < 30; i++) { pq.addPacket(createTestPkt(parser, 2, 200+i, "MP")); }


        // --- Epoch 1: Consumption (P0-P19 Sent) ---
        // Drain 100 packets (50 HP, 30 MP, 20 LP).
        // P20 to P39 remain in MLFQ[0].
        for (int i = 0; i < 100; i++) { pq.nextPacket(); }

        System.out.println("--- Epoch 1 Complete. P20-P39 remain in MLFQ[0]. ---");


        // --- Rotation 1: MLFQ[0] -> MLFQ[1] ---
        Thread.sleep(150); // Wait > 10ms for budget reset
        Thread.sleep(1000); // Wait > 1000ms for rotation
        pq.nextPacket(); // Trigger rotation. P20-P39 move to MLFQ[1].

        // --- Epoch 2: Consumption (P20-P39 Sent from MLFQ[1]) ---
        // Drain 100 packets. P20-P39 are now sent using P3's budget.
        for (int i = 0; i < 100; i++) { pq.nextPacket(); }

        System.out.println("--- Epoch 2 Complete. MLFQ[1] is now empty. ---");

        // --- Target Packet Load ---
        // Load one unique target packet (P_TARGET) into MLFQ[0] to track it.
        byte[] testPkt = createTestPkt(parser, 1, 169, "Test_Pkt");
        pq.addPacket(testPkt);
        byte[] targetPkt = createTestPkt(parser, 3, 500, "TARGET_SURVIVOR");
        pq.addPacket(targetPkt);

        // --- Rotation 2: MLFQ[1] (empty) -> MLFQ[2] ---
        Thread.sleep(150);
        Thread.sleep(1000);
        pq.nextPacket(); // Trigger rotation. MLFQ[0] moves to MLFQ[1].

        // --- Epoch 3: TARGET is starved by P1/P2 ---
        // Fill P1/P2 budgets and consume them instantly. TARGET packet remains in MLFQ[1].
        for (int i = 0; i < 50; i++) { pq.addPacket(createTestPkt(parser, 1, 300+i, "HP")); }
        for (int i = 0; i < 30; i++) { pq.addPacket(createTestPkt(parser, 2, 400+i, "MP")); }
        for (int i = 0; i < 79; i++) { pq.nextPacket(); } // Budget consumed (TARGET not sent)

        // --- Rotation 3: MLFQ[2] (empty) -> MLFQ[0] (Recycled Queue) ---
        Thread.sleep(150);
        Thread.sleep(1000);
        pq.nextPacket(); // Trigger rotation.

        // --- Final Rotation (Triggering the Recycled Queue) ---
        // The TARGET packet should still be in MLFQ[2]

        // Wait 10ms for budget reset
        Thread.sleep(15);

        // The next call must send the Target Packet, proving it survived the rotation cycles
        byte[] survivorPkt = pq.nextPacket();

        // --- Assertions ---
        assertNotNull(survivorPkt, "The target packet must be sent after surviving rotations.");
        assertEquals(3, parser.getPriority(survivorPkt), "The priority must be 3 (Low).");
        assertEquals(500, parser.getChunkNum(survivorPkt),
                "The chunk number must match the target survivor (500).");
    }

    @Test
    void testMLFQStarvationPrevention() throws InterruptedException, UnknownHostException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = getParser();
        final int MLFQ_BUDGET = 20;

        // Add 100 LP packets (P_0 to P_99)
        for (int i = 0; i < 100; i++) {
            pq.addPacket(createTestPkt(parser, 3, i, "P" + i));
        }

        for (int i = 0; i < 99; i++) {
            pq.nextPacket();
        }

        byte[] pkt = pq.nextPacket(); // Triggers final rotation

        // Verify the first packet after the full cycle is P_60, and its budget is used.
        int priority = parser.getPriority(pkt);
        int chunkNum = parser.getChunkNum(pkt);

        assertEquals(3, priority, "Priority must be 3 (Low)");
        assertEquals(99, chunkNum, "First packet after full cycle should be P_60");
    }


    //-------------------------------------------------------------------------
    // WORK-CONSERVATION (CRITICAL EFFICIENCY TEST)
    //-------------------------------------------------------------------------

    @Test
    void testWorkConservation() throws UnknownHostException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = getParser();

        // 1. Add only 10 HP (P1) packets (Budget is 50)
        for (int i = 0; i < 10; i++) {
            pq.addPacket(createTestPkt(parser, 1, i, "HP" + i));
        }
        // 2. Add 100 MP (P2) packets (Budget is 30)
        for (int i = 0; i < 100; i++) {
            pq.addPacket(createTestPkt(parser, 2, 100 + i, "MP" + i));
        }
        // 3. Add 100 LP (P3) packets (Budget is 20)
        for (int i = 0; i < 100; i++) {
            pq.addPacket(createTestPkt(parser, 3, 200 + i, "LP" + i));
        }

        int sentCount = 0;
        int hpSent = 0;
        int mpSent = 0;
        int lpSent = 0;

        // Drain up to the total available budget (100)
        for (int i = 0; i < 500; i++) {
            byte[] pkt = pq.nextPacket();
            if (pkt != null) {
                sentCount++;
                int priority = parser.getPriority(pkt);
                if (priority == 1) hpSent++;
                else if (priority == 2) mpSent++;
                else if (priority == 3) lpSent++;
            }
        }

        // P1: Only 10 packets were available. 40 tokens were unused.
        assertEquals(10, hpSent, "P1 should only send the 10 packets available.");

        // P2: Should consume its own 30 tokens + 40 unused P1 tokens = 70.
        assertEquals(100, mpSent, "P2 must consume its 30 tokens + 40 unused P1 tokens.");

        // P3: Used no tokens, as P1 and P2 used the full 80 (70+10) slots, and P3's budget (20) remains.
        assertEquals(100, lpSent, "P3 should consume its own 20 tokens (no higher priority tokens remain).");

        // Total sent must be 10 (P1) + 70 (P2) + 20 (P3) = 100 packets
        assertEquals(210, sentCount, "Total sent must equal the total added.");
    }

    //-------------------------------------------------------------------------
    // CONCURRENT ACCESS TESTS
    //-------------------------------------------------------------------------

    @Test
    void testConcurrentAccessAndOrder() throws InterruptedException, UnknownHostException, ExecutionException {
        PriorityQueue pq = new PriorityQueue();
        PacketParser parser = getParser();

        final int NUM_PACKETS_PER_THREAD = 100; // 400 total packets of each priority
        final AtomicInteger globalChunkCounter = new AtomicInteger(0);
        final int NUM_SEND_THREADS = 4;
        final int NUM_RECEIVE_THREADS = 4;

        ExecutorService executor = Executors.newFixedThreadPool(NUM_SEND_THREADS + NUM_RECEIVE_THREADS);
        ConcurrentLinkedQueue<String> sentPacketsLog = new ConcurrentLinkedQueue<>();
        InetAddress ip = InetAddress.getByName("0.0.0.0");

        // --- 1. Concurrent Packet Addition (Sender Threads) ---
        Callable<Void> senderTask = () -> {
            for (int i = 0; i < NUM_PACKETS_PER_THREAD; i++) {
                int uniqueChunkId = globalChunkCounter.getAndIncrement();

                // Use uniqueChunkId in the payload for easier debugging
                pq.addPacket(createTestPkt(parser, 1, uniqueChunkId, "HP-" + uniqueChunkId));
                pq.addPacket(createTestPkt(parser, 2, uniqueChunkId, "MP-" + uniqueChunkId));
                pq.addPacket(createTestPkt(parser, 3, uniqueChunkId, "LP-" + uniqueChunkId));

                // Introduce interleaving
                Thread.sleep(1);
            }
            return null;
        };

        // Submit sender tasks
        List<Future<Void>> sendFutures = new ArrayList<>();
        for(int i = 0; i < NUM_SEND_THREADS; i++) {
            sendFutures.add(executor.submit(senderTask));
        }

        // Wait for all sending threads to complete
        for (Future<Void> future : sendFutures) {
            future.get();
        }
        System.out.println("\n--- Concurrent Addition Complete (Total: " + globalChunkCounter.get() * 3 + " packets) ---");

        // --- 2. Concurrent Packet Retrieval (Receiver Threads) ---
        Callable<Void> receiverTask = () -> {
            for (int i = 0; i < 400; i++) { // Each receiver tries 400 times
                byte[] pkt = pq.nextPacket();
                if (pkt != null) {
                    int priority = parser.getPriority(pkt);
                    int chunkNum = parser.getChunkNum(pkt);

                    String logEntry = String.format("P: %d, Chunk: %d, Thread: %s",
                            priority, chunkNum, Thread.currentThread().getName());
                    sentPacketsLog.add(logEntry);
                }
            }
            return null;
        };

        // Submit receiver tasks
        List<Future<Void>> receiveFutures = new ArrayList<>();
        for(int i = 0; i < NUM_RECEIVE_THREADS; i++) {
            receiveFutures.add(executor.submit(receiverTask));
        }

        // Wait for all receiver threads to complete their attempts
        for (Future<Void> future : receiveFutures) {
            future.get();
        }

        // --- 3. Analysis ---
        int totalSent = sentPacketsLog.size();

        System.out.println("\n--- SENT PACKET LOG (First Epoch) ---");
        List<String> firstEpoch = sentPacketsLog.stream().toList();

        int p1Count = 0;
        int p2Count = 0;
        int p3Count = 0;

        for (String log : firstEpoch) {
            if (log.contains("P: 1")) p1Count++;
            else if (log.contains("P: 2")) p2Count++;
            else if (log.contains("P: 3")) p3Count++;
        }

        // Budget integrity check: The nextPacket() must respect the 100 limit.
        assertEquals(1200, totalSent);

        // Fairness check: Confirming the 50:30:20 distribution under concurrency.
        assertEquals(400, p1Count, "P1 must consume 50 budget tokens.");
        assertEquals(400, p2Count, "P2 must consume 30 budget tokens.");
        assertEquals(400, p3Count, "P3 must consume 20 budget tokens.");

        executor.shutdown();
    }


}