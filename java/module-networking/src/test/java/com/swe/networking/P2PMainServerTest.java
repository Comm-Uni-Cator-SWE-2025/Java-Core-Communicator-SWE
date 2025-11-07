package com.swe.networking;
import java.net.UnknownHostException;

import org.junit.Test;


public class P2PMainServerTest {

    private int mainServerPort = 8000;
    private String loopBackAddress = "10.128.10.1";

    @Test
    public void testMainServerInitialization() throws UnknownHostException {
        ClientNode mainServerNode = new ClientNode(loopBackAddress, mainServerPort);
        Topology topology = Topology.getTopology();
        topology.addUser(mainServerNode, mainServerNode);
        try {
            Thread.sleep(500000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
