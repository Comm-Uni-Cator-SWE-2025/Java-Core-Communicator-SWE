package com.swe.networking;

import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Class for the new priorityQueue.
 */
public class NewPriorityQueue {

    /**
     * Global priority queue instance used across the class.
     */
    private static NewPriorityQueue priorityQueue = null;

    /**
     * Total number of packets currently stored across all priority queues.
     * Helps track overall load and capacity.
     */
    private int totalPackets = 0;

    /**
     * Constant representing the highest priority level (0 = highest).
     */
    private final int zeroPriority = 0;

    /**
     * Constant representing priority level 1 (second highest).
     */
    private final int firstPriority = 1;

    /**
     * Constant representing priority level 2 (medium priority).
     */
    private final int secondPriority = 2;

    /**
     * Constant representing the lowest priority level (3).
     */
    private final int thirdPriority = 3;

    /**
     * Maximum number of packets allowed in the priority-0 queue.
     */
    private final int zeroLimit = 2;

    /**
     * Maximum number of packets allowed in the priority-1 queue.
     */
    private final int firstLimit = 4;

    /**
     * Maximum number of packets allowed in the priority-2 queue.
     */
    private final int secondLimit = 2;

    /**
     * Maximum number of packets allowed in the priority-3 queue.
     */
    private final int thirdLimit = 2;

    /**
     * Array holding the per-priority queue limits in order: index 0 →
     * zeroPriority, index 1 → firstPriority, index 2 → secondPriority, index 3
     * → thirdPriority.
     */
    private int[] limits = {zeroLimit, firstLimit, secondLimit, thirdLimit};

    /**
     * Queue for packets with priority 0 (highest priority).
     */
    private Deque<byte[]> zeroPriorityQueue;

    /**
     * Queue for packets with priority 1.
     */
    private Deque<byte[]> firstPriorityQueue;

    /**
     * Queue for packets with priority 2.
     */
    private Deque<byte[]> secondPriorityQueue;

    /**
     * Queue for packets with priority 3 (lowest priority).
     */
    private Deque<byte[]> thirdPriorityQueue;

    private NewPriorityQueue() {
        zeroPriorityQueue = new ArrayDeque<>();
        firstPriorityQueue = new ArrayDeque<>();
        secondPriorityQueue = new ArrayDeque<>();
        thirdPriorityQueue = new ArrayDeque<>();
    }

    /**
     * Function to get the priorty queue instance.
     *
     * @return the instance object
     */
    public static NewPriorityQueue getPriorityQueue() {
        if (priorityQueue == null) {
            priorityQueue = new NewPriorityQueue();
        }
        return priorityQueue;
    }

    /**
     * The function to add packet.
     *
     * @param data the data to add
     */
    public void addPacket(final byte[] data) {
        try {
            System.out.println("Added a packet to the priority queue...");
            totalPackets++;
            final PacketParser parser = PacketParser.getPacketParser();
            final PacketInfo info = parser.parsePacket(data);
            final int priorityLevel = info.getPriority();
            switch (priorityLevel) {
                case zeroPriority ->
                    zeroPriorityQueue.add(data);
                case firstPriority ->
                    firstPriorityQueue.add(data);
                case secondPriority ->
                    secondPriorityQueue.add(data);
                case thirdPriority ->
                    thirdPriorityQueue.add(data);
                default -> {
                    System.out.println("No a defined priority level. Adding to last priority...");
                    thirdPriorityQueue.add(data);
                }
            }
        } catch (UnknownHostException ex) {
            System.out.println("Cannot add packet " + ex.getMessage());
        }
    }

    /**
     * Function to get the next highest priority packet.
     *
     * @return the packet
     */
    public byte[] getPacket() {
        // System.out.println(Arrays.toString(limits));
        byte[] packet = null;
        if (!zeroPriorityQueue.isEmpty()) {
            if (limits[zeroPriority] > 0) {
                packet = zeroPriorityQueue.pop();
                limits[zeroPriority] -= 1;
                System.out.println("Dequeued from zero priority...");
            }
        } else if (!firstPriorityQueue.isEmpty()) {
            if (limits[firstPriority] > 0) {
                packet = firstPriorityQueue.pop();
                limits[firstPriority] -= 1;
                System.out.println("Dequeued from first priority...");
            }
        } else if (!secondPriorityQueue.isEmpty()) {
            if (limits[secondPriority] > 0) {
                packet = secondPriorityQueue.pop();
                limits[secondPriority] -= 1;
                System.out.println("Dequeued from second priority...");
            }
        } else if (!thirdPriorityQueue.isEmpty()) {
            if (limits[thirdPriority] > 0) {
                packet = thirdPriorityQueue.pop();
                limits[thirdPriority] -= 1;
                System.out.println("Dequeued from third priority...");
            }
        }
        resetLimits();
        totalPackets -= 1;
        return packet;
    }

    /**
     * Function to check if priority queue is empty.
     *
     * @return the boolean state
     */
    public boolean isEmpty() {
        return totalPackets != 0;
    }

    void resetLimits() {
        if (limits[zeroPriority] == 0 && limits[firstPriority] == 0
                && limits[secondPriority] == 0 && limits[thirdPriority] == 0) {
            limits = new int[]{zeroLimit, firstLimit, secondLimit, thirdLimit};
        }
    }
}
