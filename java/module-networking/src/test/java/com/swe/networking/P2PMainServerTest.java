package com.swe.networking;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class P2PMainServerTest {

    private int mainServerPort = 8000;
    private String loopBackAddress = "127.0.0.1";

    @Test
    public void testMainServerInitialization() throws UnknownHostException {
        ClientNode mainServerNode = new ClientNode(loopBackAddress, mainServerPort);
        Topology topology = Topology.getTopology();
        topology.addUser(mainServerNode, mainServerNode);
        try {
            Thread.sleep(50000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
