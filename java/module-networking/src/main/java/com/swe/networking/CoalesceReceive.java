package com.swe.networking;

import java.nio.ByteBuffer;

/**
 * The class implementing coalescing receive and
 * passing data to corresponding subscribed module.
 */
public class CoalesceReceive {

    /**
     * function to parse coalesce packet and pass to corresponding listener.
     *
     * @param coalescedData  coalesced payload.
     */
    public void receiveCoalescedPacket(final ByteBuffer coalescedData) {
        while (coalescedData.hasRemaining()) {
            // Get the size of the packet
            final int packetSize = coalescedData.getInt();

            // Get the module type
            final byte moduleTypeByte = coalescedData.get();
            final int moduleTypeInt = moduleTypeByte;

            // Get the payload
            final int payloadSize = packetSize - 4 - 1;
            final byte[] payload = new byte[payloadSize];
            coalescedData.get(payload);

            // Call the module message listener based on the module type
            Networking.getNetwork().callSubscriber(moduleTypeInt, payload);
        }
    }
}