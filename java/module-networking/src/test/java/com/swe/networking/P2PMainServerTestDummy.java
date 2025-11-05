package com.swe.networking;
import java.net.UnknownHostException;

import org.junit.Test;


public class P2PMainServerTestDummy {

    private int mainServerPort = 8000;
    private String loopBackAddress = "127.0.0.1";
    private String loganAddr = "10.32.0.41";

    @Test
    public void testMainServerInitialization() throws UnknownHostException {
        ClientNode mainServerNode = new ClientNode(loopBackAddress, mainServerPort);
        ClientNode p2pserverNode = new ClientNode(loopBackAddress, mainServerPort + 3);
        Topology topology = Topology.getTopology();
        topology.addUser(p2pserverNode, mainServerNode);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        topology.closeTopology();
    }

}
