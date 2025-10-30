package com.swe.networking;
import java.net.UnknownHostException;

import org.junit.Test;


public class P2PMainServerTestDummy {

    private int mainServerPort = 9000;
    private String loopBackAddress = "127.0.0.1";

    @Test
    public void testMainServerInitialization() throws UnknownHostException {
        ClientNode mainServerNode = new ClientNode(loopBackAddress, mainServerPort);
        ClientNode p2pserverNode = new ClientNode(loopBackAddress, mainServerPort + 1);
        Topology topology = Topology.getTopology();
        P2PCluster cluster0 = new P2PCluster();
        // P2PServer p2pServer = new P2PServer(p2pserverNode, mainServerNode);
        cluster0.addUser(p2pserverNode, mainServerNode);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        topology.closeTopology();
    }

}
