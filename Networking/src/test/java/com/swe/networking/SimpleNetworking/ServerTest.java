package com.swe.networking.SimpleNetworking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;

public class ServerTest {

    private static final String Client1 = "127.0.0.1";
    private static final String Client2 = "127.0.0.1";
    private static final String ServerIp = "127.0.0.1";
    private static final int Client1Port = 9001;
    private static final int Client2Port = 9002;
    private static final int ServerPort = 9000;
    private static final SimpleNetworking simpleNetworking = SimpleNetworking.getSimpleNetwork();
    private static final PacketParser parser = PacketParser.getPacketParser();

    @Test
    public void testServerIf() {
        
        try {
            ClientNode clientNode = new ClientNode(Client1, Client1Port);
            ClientNode serverNode = new ClientNode(ServerIp, ServerPort);

            Client sender = new Client(clientNode);
            Server server = new Server(serverNode);
            MessageListener func = (byte[] data) -> {
                System.out.println("Received data: " + new String(parser.getPayload(data), StandardCharsets.UTF_8));
            };
            simpleNetworking.subscribe(ModuleType.CHAT, func);

            Thread serverThread = new Thread(() -> {
                try {
                    server.receive();
                } catch (IOException e) {
                    System.err.println("Server receive error: " + e.getMessage());
                }
            });
            serverThread.start();
            Thread.sleep(500);

            ClientNode[] destinations = {serverNode};
            String msg = "Direct message to server";
            byte[] testData = msg.getBytes(StandardCharsets.UTF_8);

            sender.send(testData, destinations, serverNode);
            serverThread.join(2000);
            System.out.println("Test1 completed");
            
        } catch (Exception e) {
            System.err.println("Test1 failed: " + e.getMessage());
        }
    }

    @Test
    public void testServerElse() {
        
        try {
            ClientNode serverNode = new ClientNode(ServerIp, ServerPort);
            ClientNode clientNode1 = new ClientNode(Client1, Client1Port);
            ClientNode clientNode2 = new ClientNode(Client2, Client2Port);

            Client sender = new Client(clientNode1);
            Server server = new Server(serverNode);
            Client receiver = new Client(clientNode2);

            MessageListener func = (byte[] data) -> {
                System.out.println("Received data: " + new String(parser.getPayload(data), StandardCharsets.UTF_8));
            };
            simpleNetworking.subscribe(ModuleType.CHAT, func);

            Thread serverThread = new Thread(() -> {
                try {
                    server.receive();
                } catch (IOException e) {
                    System.err.println("Server receive error: " + e.getMessage());
                }
            });
            Thread receThread = new Thread(() -> {
                try{
                    receiver.receive();
                } catch (IOException e) {
                    System.err.println("Server receive error: " + e.getMessage());
                }
            });
            serverThread.start();
            receThread.start();
            Thread.sleep(500);

            ClientNode[] destinations = {clientNode2};
            String msg = "Message received via server";
            byte[] testData = msg.getBytes(StandardCharsets.UTF_8);

            sender.send(testData, destinations, serverNode);
            serverThread.join(2000);
            System.out.println("Test2 completed");

        } catch (Exception e) {
            System.err.println("Test2 failed: " + e.getMessage());
        }
    }

}