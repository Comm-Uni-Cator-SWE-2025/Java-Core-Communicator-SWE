import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class PriorityQueue {
    /**
     * Deadline for video packets in milliseconds.
     */
    private static final int VIDEO_DEADLINE_MS = 200;
    /**
     * Deadline for screen share packets in milliseconds.
     */
    private static final int SCREEN_DEADLINE_MS = 250;
    /**
     * Deadline for other packets like chat, file in Multi Level Queue.
     */
    private static final long AGING_THRESHOLD_MS = 2000;
    /**
     * Number of levels in Multi Level Queue.
     */
    private static final int MLFQ_LEVELS = 3;
    /**
     *  Time for each epoch.
     */
    private static final int EPOCH_MS = 100;
    /**
     * Total number of packets to be sent in one epoch.
     */
    private static final int TOTAL_BUDGET = 100;
    /**
     * Queue for video packet.
     */
    private final Deque<Packet> videoQueue = new ArrayDeque<>();
    /**
     * Queue for screen share packet.
     */
    private final Deque<Packet> screenQueue = new ArrayDeque<>();
    /**
     * Multilevel Feedback queue for other packets.
     */
    private final List<Deque<Packet>> mlfq = new ArrayList<>();
    /**
     * Bandwidth for different priority levels.
     */
    private final Map<String, Integer> budgetShare = Map.of(
            "video", 50,
            "screen", 25,
            "chat", 15,
            "file", 10
    );
    /**
     * A map which keeps track of current bandwidth of different packets.
     */
    private final Map<String, Integer> currentBudget = new HashMap<>();
    /**
     * Deadline for video packets in milliseconds.
     */
    private long lastEpochReset = System.currentTimeMillis();
    /**
     * Priority Number for Video Packet.
     */
    private static final int PRIORITY_VIDEO = 1;
    /**
     * Priority Number for screen share Packet.
     */
    private static final int PRIORITY_SCREEN = 2;
    /**
     * Priority Number for chat Packet.
     */
    private static final int PRIORITY_CHAT = 3;
    /**
     * Priority Number for File Packet.
     */
    private static final int PRIORITY_FILE = 4;
    /**
     * Total Number of priority.
     */
    private static final int PRIORITY_RANGE = 4;


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
     * @return priority value between PRIORITY_VIDEO and PRIORITY_FILE
     */
    private int getPriority(final byte[] rcvdPkt) {
        return (int) System.currentTimeMillis() % PRIORITY_RANGE + 1;
    }

    /**
     * Adds a packet to the appropriate queue based on its priority.
     *
     * @param data the packet payload
     */
    public void addPacket(final byte[] data) {
        final int priority = getPriority(data);
        final Packet pkt;
        switch (priority) {
            case PRIORITY_VIDEO:
                pkt = new Packet(data, priority, VIDEO_DEADLINE_MS);
                videoQueue.add(pkt);
                System.out.println("Packet added to video queue");
                break;
            case PRIORITY_SCREEN:
                pkt = new Packet(data, priority, SCREEN_DEADLINE_MS);
                System.out.println("Packet added to Screen queue");
                screenQueue.add(pkt);
                break;
            case PRIORITY_CHAT: // chat
                pkt = new Packet(data, priority, -1);
                System.out.println("Packet added to MLFQ queue");
                mlfq.get(0).add(pkt);
                break;
            case PRIORITY_FILE: // file
                pkt = new Packet(data, priority, -1);
                System.out.println("Packet added to MLFQ queue");
                mlfq.get(0).add(pkt);
                break;
            default:
                System.out.println("Unknown priority " + priority);
                break;
        }
    }

    /**
     * Applies aging to MLFQ packets.
     *  By moving them up a level if they wait too long.
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
     * By considering deadlines, budgets, and MLFQ.
     *
     * @return the next packet's data, or null if none available
     */
    public byte[] nextPacket() {
        final long now = System.currentTimeMillis();

        if (now - lastEpochReset >= EPOCH_MS) {
            resetBudgets();
        }

        Packet pkt = pollRealtimeQueue(videoQueue, now, "video");
        if (pkt != null) {
            System.out.println("Video Packet sent");
            return pkt.data;
        }

        pkt = pollRealtimeQueue(screenQueue, now, "screen");
        if (pkt != null) {
            System.out.println("Video Packet sent");
            return pkt.data;
        }

        applyAging();

        for (int i = 0; i < mlfq.size(); i++) {
            final Deque<Packet> q = mlfq.get(i);
            if (!q.isEmpty()) {
                final Packet p = q.pollFirst();
                final String key =
                        (p.priority == PRIORITY_CHAT) ? "chat" : "file";

                if (currentBudget.get(key) > 0) {
                    currentBudget.put(key, currentBudget.get(key) - 1);
                    if (i + 1 < mlfq.size()) {
                        mlfq.get(i + 1).add(p);
                    }
                    return p.data;
                } else {
                    q.addFirst(p);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the next packet from a real-time queue.
     * By checking if it meets deadline and budget.
     *
     * @param queue the queue to poll
     * @param now   current time
     * @param type  traffic type (video/screen)
     * @return a packet or null if not available
     */
    private Packet pollRealtimeQueue(final Deque<Packet> queue,
                                     final long now,
                                     final String type) {
        while (!queue.isEmpty()) {
            final Packet pkt = queue.pollFirst();
            if (now <= pkt.deadline) {
                if (currentBudget.get(type) > 0) {
                    currentBudget.put(type, currentBudget.get(type) - 1);
                    return pkt;
                } else {
                    queue.addFirst(pkt);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Representation of a packet.
     * Represented with data, priority, arrival time, and deadline.
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
         * The deadline of the received packet.
         */
        private final long deadline;

        /**
         * Creates a new packet.
         *
         * @param rcvdData     the packet payload
         * @param rcvdPriority the priority level
         * @param rcvdDeadline the deadline in ms (or -1 for no deadline)
         */
        Packet(final byte[] rcvdData,
               final int rcvdPriority,
               final long rcvdDeadline) {
            System.out.println("Packet Created");
            this.data = rcvdData;
            this.priority = rcvdPriority;
            this.arrivalTime = System.currentTimeMillis();
            this.deadline = (rcvdDeadline > 0)
                    ? this.arrivalTime + rcvdDeadline : Long.MAX_VALUE;
        }
    }
}
