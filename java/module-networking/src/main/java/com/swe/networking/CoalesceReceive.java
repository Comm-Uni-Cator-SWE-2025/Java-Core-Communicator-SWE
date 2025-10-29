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
            switch (ModuleType.NETWORKING.getType(moduleTypeInt)) {
                case NETWORKING:
                    // Call networking message listener
                    break;
                case SCREENSHARING:
                    // Call screensharing message listener
                    break;
                case CANVAS:
                    // Call canvas message listener
                    break;
                case UIUX:
                    // Call uiux message listener
                    break;
                case CONTROLLER:
                    // Call controller message listener
                    break;
                case AI:
                    // Call ai message listener
                    break;
                case CLOUD:
                    // Call cloud message listener
                    break;
                case CHAT:
                    // Call chat message listener
                    break;
                default:
                    // Handle unknown module type
                    break;
            }
        }
    }
}