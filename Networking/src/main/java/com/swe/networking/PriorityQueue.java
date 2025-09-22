package com.swe.networking;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class PriorityQueue {
    /**
     * Deadline for least priority packets in Multi Level Queue.
     */
    private static final long AGING_THRESHOLD_MS = 2000;
    /**
     * Number of levels in Multi Level Queue.
     */
    private static final int MLFQ_LEVELS = 3;
    /**
     * Time for each epoch.
     */
    private static final int EPOCH_MS = 100;
    /**
     * Total number of packets to be sent in one epoch.
     */
    private static final int TOTAL_BUDGET = 100;
    /**
     * Queue for video packet (highest priority).
     */
    private final Deque<Packet> highestPriorityQueue = new ArrayDeque<>();
    /**
     * Queue for screen share packet (mid-priority).
     */
    private final Deque<Packet> midPriorityQueue = new ArrayDeque<>();
    /**
     * Multilevel Feedback Queue (MLFQ) for other packets (low priority).
     */
    private final List<Deque<Packet>> mlfq = new ArrayList<>();
    /**
     * Bandwidth share for different priority levels.
     */
    private final Map<String, Integer> budgetShare = Map.of(
            "priority1", 50,
            "priority2", 30,
            "priority3", 20
    );

    /**
     * A map which keeps track of current bandwidth of different priorities.
     */
    private final Map<String, Integer> currentBudget = new HashMap<>();

    /**
     * Last time budgets were reset (epoch marker).
     */
    private long lastEpochReset = System.currentTimeMillis();

    /**
     * Priority Number for Highest Priority Packet.
     */
    private static final int PRIORITY_ONE = 1;
    /**
     * Priority Number for Mid-Priority Packet.
     */
    private static final int PRIORITY_TWO = 2;
    /**
     * Priority Number for Low-Priority Packet.
     */
    private static final int PRIORITY_THREE = 3;
    /**
     * Total Number of priorities.
     */
    private static final int PRIORITY_RANGE = 3;

    /**
     * Creates a priority queue and initializes budgets and queues.
     */
    public PriorityQueue() {
        System.out.println("MLFQ has been created");
        for (int i = 0; i < MLFQ_LEVELS; i++) {
            mlfq.add(new ArrayDeque<>());
        }
        resetBudgets();
    }

    /**
     * Resets budgets at the beginning of each epoch.
     */
    private void resetBudgets() {
        for (Map.Entry<String, Integer> e : budgetShare.entrySet()) {
            final int percent = e.getValue();
            final int tokens = (TOTAL_BUDGET * percent) / 100;
            currentBudget.put(e.getKey(), tokens);
        }
        lastEpochReset = System.currentTimeMillis();
    }

    /**
     * Returns the priority of a received packet.
     *
     * @param rcvdPkt the packet data
     * @return priority value between PRIORITY_ONE and PRIORITY_THREE
     */
    private int getPriority(final byte[] rcvdPkt) {
        // Dummy function
        return (int) System.currentTimeMillis() % PRIORITY_RANGE + 1;
    }

    /**
     * @return whether all queues are empty.
     */
    private boolean isQueueEmpty() {
        boolean mlfqEmpty = true;
        for (final Deque<Packet> q : mlfq) {
            if (!q.isEmpty()) {
                mlfqEmpty = false;
                break;
            }
        }
        return highestPriorityQueue.isEmpty()
                && midPriorityQueue.isEmpty()
                && mlfqEmpty;
    }

    /**
     * Adds a packet to the appropriate queue based on its priority.
     *
     * @param data the packet payload
     */
    public void addPacket(final byte[] data) {
        final int priority = getPriority(data);
        final Packet pkt = new Packet(data, priority);
        switch (priority) {
            case PRIORITY_ONE:
                highestPriorityQueue.add(pkt);
                System.out.println("Packet added to highest priority queue");
                break;
            case PRIORITY_TWO:
                midPriorityQueue.add(pkt);
                System.out.println("Packet added to mid priority queue");
                break;
            case PRIORITY_THREE:
                mlfq.get(0).add(pkt);
                System.out.println("Packet added to MLFQ level 0");
                break;
            default:
                System.out.println("Incorrect Priority");
                break;
        }
    }

    /**
     * Applies aging to MLFQ packets.
     * Moves them up a level if they wait too long.
     */
    private void applyAging() {
        final long now = System.currentTimeMillis();
        for (int i = 1; i < mlfq.size(); i++) {
            final Deque<Packet> q = mlfq.get(i);
            final Iterator<Packet> it = q.iterator();
            while (it.hasNext()) {
                final Packet pkt = it.next();
                if (now - pkt.arrivalTime >= AGING_THRESHOLD_MS) {
                    mlfq.get(i - 1).add(pkt);
                    it.remove();
                }
            }
        }
    }

    /**
     * Retrieves the next packet to process.
     * By considering budgets and MLFQ.
     *
     * @return the next packet's data, or null if none available
     */
    public byte[] nextPacket() {
        final long now = System.currentTimeMillis();

        if (now - lastEpochReset >= EPOCH_MS) {
            resetBudgets();
        }

        // Highest priority first
        if (!highestPriorityQueue.isEmpty()
                && currentBudget.get("priority1") > 0) {
            currentBudget.put("priority1", currentBudget.get("priority1") - 1);
            System.out.println("Highest Priority Packet sent");
            return highestPriorityQueue.pollFirst().data;
        }

        // Mid-priority next
        if (!midPriorityQueue.isEmpty() && currentBudget.get("priority2") > 0) {
            currentBudget.put("priority2", currentBudget.get("priority2") - 1);
            System.out.println("Mid Priority Packet sent");
            return midPriorityQueue.pollFirst().data;
        }

        // Apply aging before serving MLFQ
        applyAging();

        // MLFQ (low priority, multiple levels)
        for (int i = 0; i < mlfq.size(); i++) {
            final Deque<Packet> q = mlfq.get(i);
            if (!q.isEmpty() && currentBudget.get("priority3") > 0) {
                currentBudget.put("priority3",
                        currentBudget.get("priority3") - 1);
                System.out.println(
                        "Low Priority Packet sent from MLFQ level " + i);
                return q.pollFirst().data; // remove it, no demotion after send
            }
        }

        return null; // nothing available
    }

    /**
     * Representation of a packet.
     */
    static class Packet {
        /**
         * The Data is in the form byte array.
         */
        private final byte[] data;
        /**
         * The priority of the received packet.
         */
        private final int priority;
        /**
         * Arrival time of the packet.
         */
        private final long arrivalTime;

        /**
         * Creates a new packet.
         *
         * @param rcvdData     the packet payload
         * @param rcvdPriority the priority level
         */
        Packet(final byte[] rcvdData,
               final int rcvdPriority) {
            System.out.println("Packet Created");
            this.data = rcvdData;
            this.priority = rcvdPriority;
            this.arrivalTime = System.currentTimeMillis();
        }
    }
}
