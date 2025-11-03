package com.swe.networking;

import java.nio.ByteBuffer;

import com.swe.networking.ModuleType;

public class CoalesceReceive {

    public void receiveCoalescedPacket(ByteBuffer coalescedData) {
        while (coalescedData.hasRemaining()) {
            // Get the size of the packet
            int packetSize = coalescedData.getInt();

            // Get the module type
            byte moduleTypeByte = coalescedData.get();
            int moduleTypeInt = moduleTypeByte;

            // Get the payload
            byte[] payload = new byte[packetSize - 4 - 1];
            coalescedData.get(payload);

            // Call the module message listener based on the module type
            }
        }
    }
}