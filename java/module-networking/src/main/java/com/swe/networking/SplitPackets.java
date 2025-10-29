package com.swe.networking;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Classs to split the received data into packets.
 */
public class SplitPackets {
    /**
     * Singleton design pattern to prevent repeating class instantiations.
     *
     */
    private static SplitPackets splitPackets = null;

    private SplitPackets() {
    }

    /**
     * Function to get the statically instantiated class object.
     *
     * @return SplitPackets the statically instantiated class.
     */
    public static SplitPackets getSplitPackets() {
        if (splitPackets == null) {
            System.out.println("Creating new SplitPackets object...");
            splitPackets = new SplitPackets();
        }
        System.out.println("Passing already instantiated SplitPackets object...");
        return splitPackets;
    }

    /**
     * Function to split the packet received.
     *
     * @param data the data to be split
     * @return the list of packets
     */
    public List<byte[]> split(final byte[] data) {
        final List<byte[]> packets = new ArrayList<>();
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            final int len = buffer.getShort();
            buffer.position(buffer.position() - 2);
            final byte[] packet = new byte[len];
            buffer.get(packet);
            packets.add(packet);
        }
        return packets;
    }
}
