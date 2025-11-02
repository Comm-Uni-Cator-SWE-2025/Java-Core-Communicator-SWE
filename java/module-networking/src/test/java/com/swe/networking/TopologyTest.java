package com.swe.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Test class for the topology class.
 */
class TopologyTest {

    /**
     * Function to test the receive by the main server.
     */
    @org.junit.jupiter.api.Test
    void mainServerReceiveTest() {
        try {
            final int sleepTime = 1000;
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final Topology topology = Topology.getTopology();
            topology.addUser(server, server);
            send();
            Thread.sleep(sleepTime);
            topology.closeTopology();
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Test function to send data.
     */
    public void send() {
        try {
            final Socket destSocket = new Socket();
            final Integer port = 8000;
            final Integer timeout = 5000;
            final int sleepTime = 1000;
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            final Thread receiveThread = new Thread(() -> receive(destSocket));
            receiveThread.start();
            Thread.sleep(sleepTime);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final String data = "Hello World !!!";
            final PacketParser parser = PacketParser.getPacketParser();
            final byte[] packet = parser.createPkt(NetworkType.USE.ordinal(), 0, 0,
                    NetworkConnectionType.HELLO.ordinal(), 0,
                    InetAddress.getByName("127.0.0.1"),
                    destSocket.getLocalPort(), (int) (Math.random() * 1000),
                    0, 1, data.getBytes());
            dataOut.write(packet);
            // receiveThread.interrupt();
            // destSocket.close();
        } catch (IOException | InterruptedException ex) {
        }
    }

    public void receive(final Socket socket) {
        try {
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final int byteBufferSize = 1024;
            final byte[] buffer = new byte[byteBufferSize];
            int bytesRead;
            while ((bytesRead = dataIn.read(buffer)) != -1) {
                System.out.println("Response: " + new String(buffer, 0, bytesRead));
            }
        } catch (IOException ex) {
        }
    }
}