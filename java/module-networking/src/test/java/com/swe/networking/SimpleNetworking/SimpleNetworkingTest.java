package com.swe.networking.SimpleNetworking;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;

/**
 * Test class for simplenetworking class.
 */
public class SimpleNetworkingTest {
    /**
     * Function to test the simpleNetwokring server receive.
     */
    @org.junit.jupiter.api.Test
    public void simpleNetworkingServerTestReceive() {
        try {
            final Integer sleepTime = 2000;
            final SimpleNetworking network = SimpleNetworking.getSimpleNetwork();
            final ClientNode device = new ClientNode("127.0.0.1", 8000);
            final ClientNode mainServer = new ClientNode("127.0.0.1", 8000);
            network.addUser(device, mainServer);
            final MessageListener func = (byte[] data) -> {
                System.out.println("Received data: " + new String(data, StandardCharsets.UTF_8));
            };
            network.subscribe(ModuleType.CHAT, func);
            Thread.sleep(sleepTime);
            sendServer();
            network.closeNetworking();
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Function to test the simpleNetwokring client receive.
     */
    @org.junit.jupiter.api.Test
    public void simpleNetworkingClientTestReceive() {
        try {
            final Integer sleepTime = 2000;
            final SimpleNetworking network = SimpleNetworking.getSimpleNetwork();
            final ClientNode device = new ClientNode("127.0.0.1", 9001);
            final ClientNode mainServer = new ClientNode("127.0.0.1", 9000);
            final Server server = new Server(mainServer);
            final Thread serverThread = new Thread(() -> {
                try {
                    while (true) {
                        server.receive();
                    }
                } catch (IOException e) {
                    System.err.println("Server receive error: " + e.getMessage());
                }
            });
            serverThread.start();
            network.addUser(device, mainServer);
            final MessageListener func = (byte[] data) -> {
                System.out.println("Received data: " + new String(data, StandardCharsets.UTF_8));
            };
            network.subscribe(ModuleType.CHAT, func);
            sendClient();
            Thread.sleep(sleepTime);
            network.closeNetworking();
            serverThread.interrupt();
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Test function to send message.
     */
    public void sendServer() {
        final Socket destSocket = new Socket();
        try {
            final Integer port = 8000;
            final Integer timeout = 5000;
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final String data = "Hello World !!!";
            final PacketParser parser = PacketParser.getPacketParser();
            final byte[] packet = parser.createPkt(0, ModuleType.CHAT.ordinal(), 0, 0,
                    InetAddress.getByName("127.0.0.1"), 8000, data.getBytes());
            dataOut.write(packet);
            destSocket.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Test function to send message.
     */
    public void sendClient() {
        final Socket destSocket = new Socket();
        try {
            final Integer port = 9000;
            final Integer timeout = 5000;
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final String data = "Hello World !!!";
            final PacketParser parser = PacketParser.getPacketParser();
            final byte[] packet = parser.createPkt(0, ModuleType.CHAT.ordinal(), 0, 0,
                    InetAddress.getByName("127.0.0.1"), 9001, data.getBytes());
            dataOut.write(packet);
            destSocket.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Function to test subscribe in simplenetworking.
     */
    @org.junit.jupiter.api.Test
    public void simpleNetworkingSubscribeTest() {
        final SimpleNetworking network = SimpleNetworking.getSimpleNetwork();
        final MessageListener func = (byte[] data) -> {
            System.out.println("Received data: " + new String(data, StandardCharsets.UTF_8));
        };
        network.subscribe(ModuleType.CHAT, func);
        network.subscribe(ModuleType.CHAT, func);
        network.removeSubscription(ModuleType.CHAT);
        network.removeSubscription(ModuleType.CHAT);
    }

    /**
     * Function to test simpleNetworking server send.
     */
    @org.junit.jupiter.api.Test
    public void simpleNetworkingServerSendTest() {
        final ClientNode cdevice = new ClientNode("127.0.0.1", 8101);
        final Client client = new Client(cdevice);
        final Thread clientThread = new Thread(() -> {
            try {
                while (true) {
                    client.receive();
                }
            } catch (IOException e) {
                System.err.println("Server receive error: " + e.getMessage());
            }
        });
        clientThread.start();
        final SimpleNetworking network = SimpleNetworking.getSimpleNetwork();
        final ClientNode device = new ClientNode("127.0.0.1", 8100);
        final ClientNode mainServer = new ClientNode("127.0.0.1", 8100);
        network.addUser(device, mainServer);
        final MessageListener func = (byte[] data) -> {
            System.out.println("Received data: " + new String(data, StandardCharsets.UTF_8));
        };
        network.subscribe(ModuleType.CHAT, func);
        final String data = "Hello from server !!!";
        final ClientNode dest = new ClientNode("127.0.0.1", 8101);
        final ClientNode[] dests = { dest };
        network.sendData(data.getBytes(), dests, ModuleType.CHAT, 0);
        clientThread.interrupt();
        network.closeNetworking();
        // client.closeUser();
    }

    /**
     * Test to check socket exceptions.
     */
    @org.junit.jupiter.api.Test
    public void simpleNetworkingIOTest() {
        final ClientNode device = new ClientNode("127.0.0.1", 8100);
        final Server server = new Server(device);
        final String data = "Hello from server !!!";
        final ClientNode dest = new ClientNode("127.0.0.1", 8000);
        final ClientNode[] dests = { dest };
        server.send(data.getBytes(), dests, device, ModuleType.CHAT);
        server.sendPkt(data.getBytes(), dests, device);
        server.closeUser();
        // network.closeNetworking();
    }
}
