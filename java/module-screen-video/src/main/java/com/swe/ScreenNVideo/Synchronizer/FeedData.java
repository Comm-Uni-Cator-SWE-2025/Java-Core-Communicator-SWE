/**
 * Contributed by @chirag9528.
 */

package com.swe.ScreenNVideo.Synchronizer;

import com.swe.ScreenNVideo.Model.CPackets;


/**
 * Represents a single video feed packet entry used in synchronization.
 * Stores the packet's feed number (for ordering) and its compressed data.
 */
public class FeedData {

    /** The sequence number of this packet.*/
    private final int feedNumber;

    /** The compressed packets.*/
    private final CPackets packets;

    /**
     * Creates a FeedData entry with its feed number and packet content.
     *
     * @param feedNum the sequence/feed number for ordering
     * @param packetData the compressed packets associated with this sequence
     */
    public FeedData(final int feedNum, final CPackets packetData) {
        this.feedNumber = feedNum;
        this.packets = packetData;
    }

    /**
     * Returns the feed/sequence number of this packet.
     * @return the feed/sequence number of this packet
     */
    public int getFeedNumber() {
        return this.feedNumber;
    }

    /**
     * Returns the compressed packet data associated with this entry.
     * @return the compressed Packet data
     */
    public CPackets getFeedPackets() {
        return this.packets;
    }
}
