package com.swe.networking;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

public class NetworkRPCTest {

    @Test
    public void testNetworkRPCAddUser() {
        try {
            final NetworkRPC rpc = NetworkRPC.getNetworkRPC();
            String deviceHost = "192.168.1.55";
            int devicePort = 50001;
            String serverHost = "10.32.12.11";
            int serverPort = 60000;
            
            byte[] deviceHostBytes = deviceHost.getBytes(StandardCharsets.UTF_8);
            byte[] serverHostBytes = serverHost.getBytes(StandardCharsets.UTF_8);
            
            ByteBuffer buffer = ByteBuffer.allocate(
                    1 + deviceHostBytes.length + 4
                            + // device: len + host + port
                            1 + serverHostBytes.length + 4 // server: len + host + port
            );
            buffer.put((byte) deviceHostBytes.length);
            buffer.put(deviceHostBytes);
            buffer.putInt(devicePort);
            buffer.put((byte) serverHostBytes.length);
            buffer.put(serverHostBytes);
            buffer.putInt(serverPort);
            byte[] inputBytes = buffer.array();
            rpc.networkRPCAddUser(inputBytes);
            Thread.sleep(1000);
            rpc.networkRPCCloseNetworking(new byte[0]);
        } catch (InterruptedException ex) {
        }
    }
}
