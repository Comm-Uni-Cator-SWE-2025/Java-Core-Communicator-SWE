/*
 * -----------------------------------------------------------------------------
 *  File: PriorityQueue.java
 *  Owner: Vishwaa
 *  Roll Number : 112201030
 *  Module : Networking
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.networking;

import java.net.UnknownHostException;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

// File owned by Vishwaa.
/**
 * Priority Queue with simple Multi-Level Feedback Queue (MLFQ).
 */
public class PriorityQueue {

    /**
     * Variable to store the name of the module.
     */
    private static final String MODULENAME = "[PRIORITY QUEUE]";
    /**
     * The static class object for the priority queue.
     */
    private static PriorityQueue priorityQueue = null;
    /**
     * Time for each epoch (budget reset).
     */
    private static final int EPOCH_MS = 50;
    /**
     * Total number of packets to be sent in one epoch.
     */
    private static final int TOTAL_BUDGET = 100;
    /**
     * Time difference required for each rotation of MLFQ.
     */
    private static final int ROTATION_TIME = 1000;
    /**
     * Queue for highest priority packet.
     */
    private final Deque<byte[]> highestPriorityQueue = new ConcurrentLinkedDeque<>();
    /**
     * Queue for mid-priority packet.
     */
    private final Deque<byte[]> midPriorityQueue = new ConcurrentLinkedDeque<>();
    /**
     * Queue for low priority packet.
     */
    private final Deque<byte[]> lowPriorityQueue = new ConcurrentLinkedDeque<>();
    /**
     * Queue for chat packet.
     */
    private final Deque<byte[]> chatQueue = new ConcurrentLinkedDeque<>();
    /**
     * Current bandwidth tokens.
     */
    private final Map<PacketPriority, Integer> currentBudget = new EnumMap<>(PacketPriority.class);
    /**
     * Last time budgets were reset (epoch marker).
     */
    private long lastEpochReset = System.currentTimeMillis();
    /**
     * Private variable to store the start time of the Priority Queue.
     */
    private long startTime;
    /**
     * Private Variable to store the number of packets sent.
     */
    private long numPacketsSent;

    /**
     * Creates a priority queue and initializes budgets and queues.
     */
    private PriorityQueue() {
        // System.out.println("Networking][Priority Queue] MLFQ has been created");
        startTime = System.currentTimeMillis();
        numPacketsSent = 0;
        resetBudgets();
    }

    /**
     * Function to return the singleton Priority Queue object.
     *
     * @return the singleton object
     */
    public static PriorityQueue getPriorityQueue() {
        if (priorityQueue == null) {
            priorityQueue = new PriorityQueue();
            NetworkLogger.printInfo(MODULENAME, "Instantiated a new priority Queue...");
            return priorityQueue;
        }
        NetworkLogger.printInfo(MODULENAME, "Passing already instantiated priority Queue...");
        return priorityQueue;
    }

    /**
     * This function returns the current remaining total budget.
     *
     * @return the remaining total budget.
     */
    private int getTotalRemainingBudget() {
        // Sums up all remaining tokens from the HIGHEST,MID, LOW, CHAT buckets.
        return currentBudget.values().stream()
                .mapToInt(Integer::intValue).sum();
    }

    /**
     * Empties the priority queue for each test.
     */
    public void clear() {
        highestPriorityQueue.clear();
        midPriorityQueue.clear();
        lowPriorityQueue.clear();
        chatQueue.clear();
        resetBudgets();
    }

    /**
     * This function says whether there are any packets to be sent.
     *
     * @return true if there are packets left.
     */
    public boolean isEmpty() {
        // Checking the highest and mid-priority queue
        if (!highestPriorityQueue.isEmpty() || !midPriorityQueue.isEmpty() || !lowPriorityQueue.isEmpty()
                || !chatQueue.isEmpty()) {
            return false;
        }

        // If everything is empty
        return true;
    }

    /**
     * Resets budgets at the beginning of each epoch.
     */
    private void resetBudgets() {
        for (PacketPriority p : PacketPriority.values()) {
            final int tokens = (TOTAL_BUDGET * p.getShare()) / TOTAL_BUDGET;
            currentBudget.put(p, tokens);
        }
        NetworkLogger.printInfo(MODULENAME, "Reset Initiated...");
        lastEpochReset = System.currentTimeMillis();
    }

    /**
     * This function gives the approx throughput of the Priority Queue. This
     * assumes that there are enough number of packets.
     *
     * @return The minimum throughput of the Priority Queue
     */
    public long getThroughput() {

        final long currTime = System.currentTimeMillis();
        final long timeTaken = currTime - startTime;
        if (timeTaken == 0) {
            return 0;
        }
        final int packetLength = 1000;

        return (packetLength * numPacketsSent) / timeTaken;
    }

    /**
     * Adds a packet to the appropriate queue based on its priority.
     *
     * @param data the packet payload
     */
    public synchronized void addPacket(final byte[] data) throws UnknownHostException {
        final PacketParser parser = PacketParser.getPacketParser();
        final PacketInfo info = parser.parsePacket(data);
        final int priorityLevel = info.getPriority();
        final PacketPriority priority = PacketPriority.fromLevel(priorityLevel);

        switch (priority) {
            case ZERO, ONE, TWO:
                highestPriorityQueue.add(data);
                NetworkLogger.printInfo(MODULENAME, "Packet added to the Highest priority queue");
                break;
            case THREE, FOUR, FIVE:
                midPriorityQueue.add(data);
                NetworkLogger.printInfo(MODULENAME, "Packet added to mid priority queue");
                break;
            case SIX:
                lowPriorityQueue.add(data);
                NetworkLogger.printInfo(MODULENAME, "Packet added to low priority queue");
                break;
            default:
                chatQueue.add(data);
                NetworkLogger.printInfo(MODULENAME, "Packet added to chat queue");
                break;
        }
    }

    /**
     * Processes and sends a packet from the Highest Priority Queue (P1/ZERO).
     *
     * @return The packet data, or null.
     */
    private byte[] processHighestPriority() {
        if (highestPriorityQueue.isEmpty()) {
            return null;
        }
        final byte[] packet = selectHighestPriority();
        return packet;
    }

    /**
     * Selects a packet from the Highest Priority Queue (P1/ZERO) based on
     * budget.
     *
     * @return The packet data, or null if budget exhausted
     */
    private byte[] selectHighestPriority() {
        if (currentBudget.get(PacketPriority.ZERO) > 0) {
            currentBudget.put(PacketPriority.ZERO, currentBudget.get(PacketPriority.ZERO) - 1);
            NetworkLogger.printInfo(MODULENAME, "Highest Priority Packet sent from High Priority Budget");
            return highestPriorityQueue.pollFirst();
        } else if (midPriorityQueue.isEmpty() && currentBudget.get(PacketPriority.ONE) > 0) {
            currentBudget.put(PacketPriority.ONE, currentBudget.get(PacketPriority.ONE) - 1);
            NetworkLogger.printInfo(MODULENAME, "Highest Priority Packet sent from Mid Priority Budget");
            return highestPriorityQueue.pollFirst();
        } else if (midPriorityQueue.size() < highestPriorityQueue.size()
                && lowPriorityQueue.isEmpty() && currentBudget.get(PacketPriority.TWO) > 0) {
            currentBudget.put(PacketPriority.TWO, currentBudget.get(PacketPriority.TWO) - 1);
            NetworkLogger.printInfo(MODULENAME, "Highest Priority Packet sent from Low Priority Budget");
            return highestPriorityQueue.pollFirst();
        } else if (chatQueue.isEmpty() && currentBudget.get(PacketPriority.THREE) > 0
                && highestPriorityQueue.size() > Math.max(midPriorityQueue.size(), lowPriorityQueue.size())) {
            currentBudget.put(PacketPriority.THREE, currentBudget.get(PacketPriority.THREE) - 1);
            NetworkLogger.printInfo(MODULENAME, "Highest Priority Packet sent from Chat budget");
            return highestPriorityQueue.pollFirst();
        }
        return null;
    }

    /**
     * Processes and sends a packet from the Mid-Priority Queue (P2/ONE).
     * Work-conserving: uses P2's budget, then P1's unused budget.
     *
     * @return The packet data, or null.
     */
    private byte[] processMidPriority() {
        if (midPriorityQueue.isEmpty()) {
            return null;
        }
        final int p2Current = currentBudget.get(PacketPriority.ONE);
        final int p1Current = currentBudget.get(PacketPriority.ZERO);
        final int totalP2Budget = p2Current + p1Current;

        if (totalP2Budget > 0) {
            // Check current tokens again

            if (p2Current > 0) {
                // Use P2's own budget
                currentBudget.put(PacketPriority.ONE, p2Current - 1);
                NetworkLogger.printInfo(MODULENAME, "Mid-priority sent from Mid-Priority budget");
            } else if (p1Current > 0) {
                // Use P1's unused budget
                currentBudget.put(PacketPriority.ZERO, p1Current - 1);
                NetworkLogger.printInfo(MODULENAME, "Mid-priority sent from High Priority budget");
            }
            return midPriorityQueue.pollFirst();
        } else if (lowPriorityQueue.isEmpty() && currentBudget.get(PacketPriority.TWO) > 0) {
            currentBudget.put(PacketPriority.TWO, currentBudget.get(PacketPriority.TWO) - 1);
            NetworkLogger.printInfo(MODULENAME, "Mid-prioity sent from Low Priority budget");
            return midPriorityQueue.pollFirst();
        } else if (chatQueue.isEmpty() && currentBudget.get(PacketPriority.THREE) > 0
                && midPriorityQueue.size() > lowPriorityQueue.size()) {
            currentBudget.put(PacketPriority.THREE, currentBudget.get(PacketPriority.THREE) - 1);
            NetworkLogger.printInfo(MODULENAME, "Mid-Priority Packet Sent from chat budget");
            return midPriorityQueue.pollFirst();
        }
        return null;
    }

    /**
     * Processes and sends a packet from the Low Priority (P3/TWO).
     * Work-conserving: uses P3, then P1, then P2 budget.
     *
     * @return The packet data, or null.
     */
    private byte[] processLowPriority() {
        if (lowPriorityQueue.isEmpty()) {
            // NetworkLogger.printInfo(MODULENAME, "low is accessed");
            return null;
        }
        final int p3Current = currentBudget.get(PacketPriority.TWO);
        final int p2Current = currentBudget.get(PacketPriority.ONE);
        final int p1Current = currentBudget.get(PacketPriority.ZERO);
        final int totalP3Budget = p3Current + p2Current + p1Current;

        if (totalP3Budget > 0) {
            // Check current tokens again

            if (p3Current > 0) {
                // Use P3's own budget
                currentBudget.put(PacketPriority.TWO, p3Current - 1);
                NetworkLogger.printInfo(MODULENAME, "Low priority packet sent from Low Priority budget");
            } else if (p2Current > 0) {
                // Use P2's unused budget
                currentBudget.put(PacketPriority.ONE, p2Current - 1);
                NetworkLogger.printInfo(MODULENAME, "Low priority packet sent from Mid Priority budget");
            } else if (p1Current > 0) {
                // Use P1's unused budget
                currentBudget.put(PacketPriority.ZERO, p1Current - 1);
                NetworkLogger.printInfo(MODULENAME, "Low priority packet sent from High Priority budget");
            }
            return lowPriorityQueue.pollFirst();
        } else if (chatQueue.isEmpty() && currentBudget.get(PacketPriority.THREE) > 0) {
            currentBudget.put(PacketPriority.THREE, currentBudget.get(PacketPriority.THREE) - 1);
            NetworkLogger.printInfo(MODULENAME, "Low prioity sent from Chat budget");
            return lowPriorityQueue.pollFirst();
        }
        return null; // nothing available or budget exhausted
    }

    /**
     * Processes and sends a packet from the Chat Priority (P4/THREE).
     * Work-conserving: uses P4, then P1, then P2, then P3 budget.
     *
     * @return The packet data, or null.
     */
    private byte[] processChat() {
        if (chatQueue.isEmpty()) {
            // NetworkLogger.printInfo(MODULENAME, "chat is accessed");
            return null;
        }
        final int p4Current = currentBudget.get(PacketPriority.THREE);
        final int p3Current = currentBudget.get(PacketPriority.TWO);
        final int p2Current = currentBudget.get(PacketPriority.ONE);
        final int p1Current = currentBudget.get(PacketPriority.ZERO);
        final int totalP4Budget = p4Current + p3Current + p2Current + p1Current;

        if (totalP4Budget > 0) {
            // Check current tokens again
            if (p4Current > 0) {
                // Use P4's own budget
                currentBudget.put(PacketPriority.THREE, p4Current - 1);
                NetworkLogger.printInfo(MODULENAME, "Chat packet sent from Chat Budget");
            } else if (p3Current > 0) {
                // Use P3's unused budget
                currentBudget.put(PacketPriority.TWO, p3Current - 1);
                NetworkLogger.printInfo(MODULENAME, "Chat priority packet sent from Low Priority budget");
            } else if (p2Current > 0) {
                // Use P2's unused budget
                currentBudget.put(PacketPriority.ONE, p2Current - 1);
                NetworkLogger.printInfo(MODULENAME, "Chat priority packet sent from Mid-Priority budget");
            } else if (p1Current > 0) {
                // Use P1's unused budget
                currentBudget.put(PacketPriority.ZERO, p1Current - 1);
                NetworkLogger.printInfo(MODULENAME, "Chat priority packet sent from High Priority budget");
            }
            return chatQueue.pollFirst();
        }
        return null; // nothing available or budget exhausted
    }

    /**
     * A private function which gets the packet from the queue. The packet could
     * be null, and it signals to wait.
     *
     * @return The Packet data or null
     */
    private byte[] trySendNext() {
        // 1. Reset budgets every epoch
        if (getTotalRemainingBudget() <= 0) {
            resetBudgets();
            NetworkLogger.printInfo(MODULENAME, "Reset initiated due to Budget");
        }

        final long now = System.currentTimeMillis();

        if (now - lastEpochReset >= EPOCH_MS) {
            resetBudgets();
            NetworkLogger.printInfo(MODULENAME, "Reset initiated due to Time");
        }

        // 3. Process priorities in order: P1 > P2 > P3
        // Highest Priority (P1/ZERO)
        byte[] packet = processHighestPriority();
        if (packet != null) {
            return packet;
        }

        // Mid-Priority (P2/ONE)
        packet = processMidPriority();
        if (packet != null) {
            return packet;
        }

        // Low Priority (P3/TWO)
        packet = processLowPriority();
        if (packet != null) {
            return packet;
        }

        // Chat Packets (P4/THREE)
        packet = processChat();
        if (packet != null) {
            return packet;
        }
        return null;
    }

    /**
     * Retrieves the next packet to process.
     *
     * @return the next packet's data, or null if none available
     */
    public synchronized byte[] nextPacket() {
        byte[] packet;

        while (true) {
            // If the queue is empty it return null.
            if (isEmpty()) {
                return null;
            }

            // Gets the Packet from tryNextSend.
            packet = trySendNext();
            if (packet != null) {
                numPacketsSent++;
                return packet;
            }

            // If the packet is null it makes the thread wait and they retry.
            try {
                NetworkLogger.printInfo(MODULENAME, "No packets has been received, Thread going to sleep");
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }

    }

    /**
     * Enum representing packet priorities and corresponding budget shares.
     */
    public enum PacketPriority {
        /**
         * Highest priority packet (50% budget share).
         */
        ZERO(0, 10),
        /**
         * High priority packet (30% budget share).
         */
        ONE(1, 10),
        /**
         * Low priority packets handled by MLFQ (20% budget share).
         */
        TWO(2, 70),
        /**
         * Future extension: low priority.
         */
        THREE(3, 10),
        /**
         * Future extension: very low priority.
         */
        FOUR(4, 0),
        /**
         * Future extension: bulk priority.
         */
        FIVE(5, 0),
        /**
         * Future extension: background priority.
         */
        SIX(6, 0),
        /**
         * Future extension: lowest priority.
         */
        SEVEN(7, 0);

        /**
         * Priority level (1–8).
         */
        private final int priorityLevel;

        /**
         * Percentage share of total budget.
         */
        private final int budgetShare;

        /**
         * Constructor for PacketPriority.
         *
         * @param level priority level (0-7)
         * @param share percentage of total budget for this priority
         */
        PacketPriority(final int level, final int share) {
            this.priorityLevel = level;
            this.budgetShare = share;
        }

        /**
         * Gets the budget share for this priority.
         *
         * @return percentage of total budget
         */
        public int getShare() {
            return budgetShare;
        }

        /**
         * Returns the PacketPriority corresponding to a given level.
         *
         * @param searchLevel the level to look up
         * @return the PacketPriority enum
         * @throws IllegalArgumentException if level is invalid
         */
        public static PacketPriority fromLevel(final int searchLevel) {
            for (PacketPriority p : values()) {
                if (p.priorityLevel == searchLevel) {
                    return p;
                }
            }
            throw new IllegalArgumentException(
                    "Invalid priority level: " + searchLevel);
        }
    }
}
