package com.swe.networking;

import java.util.LinkedList;
import java.util.Queue;

class CoalescedPacket {
    /**
     * Queue storing packets to be coalesced.
     */
    private final Queue<byte[]> queue = new LinkedList<byte[]>();
    /**
     * Current total size of the queue in bytes.
     */
    private int totalSize = 0;
    /**
     * The time at which the coalescedPacket is created.
     */
    private long startTime;

    public void addToQueue(final byte[] data) {
        if (totalSize == 0) {
            this.startTime = System.currentTimeMillis();
        }
        queue.add(data);
        totalSize += data.length;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public int getTotalSize() {
        return this.totalSize;
    }

    public byte[] getQueueHead() {
        final byte[] head = this.queue.poll();
        if (head != null) {
            this.totalSize -= head.length;
        }
        return head;
    }
}
