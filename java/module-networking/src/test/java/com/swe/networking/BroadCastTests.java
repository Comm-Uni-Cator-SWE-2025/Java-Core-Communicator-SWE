package com.swe.networking;

import com.swe.core.ClientNode;

public class BroadCastTests {

    private String mainServerAddress = "127.0.0.1";
    private int mainServerPort = 8000;

//    @Test
    public void testBroadcast() {
        try {
            final ClientNode node = new ClientNode(mainServerAddress, mainServerPort);
            final Networking networking = Networking.getNetwork();
            final MessageListener func = (byte[] data) -> {
                System.out.println("This Server Received data: " + data.length);
            };
            networking.subscribe(0, func);
            networking.addUser(node, node);
            // new Thread(() -> {
            //     networking.start();
            // }).start();
            // try {
            //     Thread.sleep(20000);
            // } catch (InterruptedException ex) {
            //     ex.printStackTrace();
            // }
            final String data = "Hello Sekai !!!";
            Thread.sleep(5000);
            networking.broadcast(data.getBytes(), 0, 0);
        } catch (InterruptedException ex) {
        }
    }

//    @Test
    public void testListenForBroadcast0() {

        final String ip = "127.0.0.1";
        final int port = 8004;
        final ClientNode node = new ClientNode(ip, port);
        final ClientNode mainServerNode = new ClientNode(mainServerAddress, mainServerPort);
        final Networking networking = Networking.getNetwork();
        networking.addUser(node, mainServerNode);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
