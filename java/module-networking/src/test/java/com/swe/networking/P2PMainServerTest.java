package com.swe.networking;

import java.net.UnknownHostException;

import org.junit.Test;

public class P2PMainServerTest {

    private int mainServerPort = 8000;
    private String loopBackAddress = "10.32.0.41";

    @Test
    public void testMainServerInitialization() throws UnknownHostException {
        ClientNode mainServerNode = new ClientNode(loopBackAddress, mainServerPort);
        Networking networking = Networking.getNetwork();
        // Topology topology = Topology.getTopology();
        // topology.addUser(mainServerNode, mainServerNode);
        networking.addUser(mainServerNode, mainServerNode);
        final MessageListener func = (byte[] data) -> {
            System.out.println("Server Received data: " + data.length);
        };
        networking.subscribe(0, func);
        try {
            Thread.sleep(500000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
