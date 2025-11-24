package com.swe.networking;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.swe.core.ClientNode;
import org.junit.jupiter.api.Test;

public class NetworkRPCTest {

    @Test
    public void testNetworkRPCAddUser() {
        try {
            final NetworkRPC rpc = NetworkRPC.getNetworkRPC();
            final String deviceHost = "192.168.1.55";
            final int devicePort = 60000;
            final String serverHost = "192.168.1.55";
            final int serverPort = 60000;

            final byte[] deviceHostBytes = deviceHost.getBytes(StandardCharsets.UTF_8);
            final byte[] serverHostBytes = serverHost.getBytes(StandardCharsets.UTF_8);

            final ByteBuffer buffer = ByteBuffer.allocate(
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
            final byte[] inputBytes = buffer.array();
            rpc.networkRPCAddUser(inputBytes);
            Thread.sleep(1000);
            rpc.networkRPCCloseNetworking(new byte[0]);
        } catch (InterruptedException ex) {
        }
    }

    @Test
    public void testNetworkRPCSubscribe() {
        final NetworkRPC rpc = NetworkRPC.getNetworkRPC();
        final ByteBuffer args = ByteBuffer.allocate(4);
        args.putInt(2);
        rpc.networkRPCSubscribe(args.array());
//        Networking.getNetwork().callSubscriber(2, new byte[0]);
    }

    @Test
    public void testNetworkRPCRemoveSubscription() {
        final NetworkRPC rpc = NetworkRPC.getNetworkRPC();
        final ByteBuffer args1 = ByteBuffer.allocate(4);
        args1.putInt(2);
        rpc.networkRPCSubscribe(args1.array());
        final ByteBuffer args2 = ByteBuffer.allocate(4);
        args2.putInt(2);
        rpc.networkRPCRemoveSubscription(args2.array());
    }

    @Test
    public void testNetworkRPCBroadcast() {
        final NetworkRPC rpc = NetworkRPC.getNetworkRPC();
        final byte[] payload = "hello".getBytes();
        final int module = 42;
        final int priority = 7;

        final ByteBuffer args = ByteBuffer.allocate(4 + payload.length + 4 + 4);
        args.putInt(payload.length);
        args.put(payload);
        args.putInt(module);
        args.putInt(priority);
        ClientNode user = new ClientNode("127.0.0.1", 8888);
        Networking.getNetwork().addUser(user, user);
        rpc.networkRPCBroadcast(args.array());
    }

    @Test
    public void testNetworkRPCSendData() {
        final NetworkRPC rpc = NetworkRPC.getNetworkRPC();
        final int destCount = 1;
        final String host = "127.0.0.1";
        final byte[] hostBytes = host.getBytes(StandardCharsets.UTF_8);
        final byte hostLen = (byte) hostBytes.length;
        final int port = 8080;

        // Payload
        final byte[] payload = "testdata".getBytes();
        final int module = 5;
        final int priority = 2;

        // Build buffer: [destCount]
        // For each dest: [hostLen][hostBytes][port]
        // Then: [dataLength][data][module][priority]
        final ByteBuffer buf = ByteBuffer.allocate(4 + (1 + hostBytes.length + 4) + 4 + payload.length + 4 + 4);

        buf.putInt(destCount);
        buf.put(hostLen);
        buf.put(hostBytes);
        buf.putInt(port);

        buf.putInt(payload.length);
        buf.put(payload);
        buf.putInt(module);
        buf.putInt(priority);

        rpc.networkRPCSendData(buf.array());
    }


}
