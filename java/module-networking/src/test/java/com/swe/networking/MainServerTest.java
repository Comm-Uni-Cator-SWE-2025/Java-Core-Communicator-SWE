package com.swe.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Test class for MainServer.
 */
public class MainServerTest {
    /**
     * Function to test send Function.
     */
    @org.junit.jupiter.api.Test
    public void testSend() {
        try {
            final int serverport1 = 8001;
            final int serverport2 = 8002;
            final Thread recieveThread1 = new Thread(() -> receive(serverport1));
            final Thread recieveThread2 = new Thread(() -> receive(serverport2));
            recieveThread1.start();
            recieveThread2.start();
            final Integer sleepTime = 500;
            Thread.sleep(sleepTime);
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final MainServer mainServer = new MainServer(server, server);
            final ClientNode dest1 = new ClientNode("127.0.0.1", 8001);
            final ClientNode dest2 = new ClientNode("127.0.0.1", 8002);
            final String data = "Good morning !!!";
            mainServer.send(data.getBytes(), dest1);
            mainServer.send(data.getBytes(), dest2);
            mainServer.closeClient(dest1);
            mainServer.closeClient(dest2);
            recieveThread1.join();
            recieveThread2.join();
            mainServer.close();
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Test function to check send is working.
     * 
     * @param serverPort to set the port of server
     */
    public void receive(final int serverPort) {
        try {
            final ServerSocket serverSocket = new ServerSocket(serverPort);
            serverSocket.setSoTimeout(0);
            final Socket socket = serverSocket.accept();
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final byte[] packet = dataIn.readAllBytes();
            System.out.println("Client port:" + serverPort + " data : " + new String(packet));
            System.out.println("Data received successfully...");
            serverSocket.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Function to test the receive functionality.
     */
    @org.junit.jupiter.api.Test
    public void testReceive() {
        try {
            final int sleepTime = 1000;
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final MainServer mainServer = new MainServer(server, server);
            send();
            Thread.sleep(sleepTime);
            mainServer.close();
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
            final int randomFactor = (int) Math.pow(10, 6);
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final String data = "Hello World !!!";
            final PacketParser parser = PacketParser.getPacketParser();
            final PacketInfo pkt = new PacketInfo();
            pkt.setType(NetworkType.USE.ordinal());
            pkt.setPriority(0);
            pkt.setModule(0);
            pkt.setConnectionType(NetworkConnectionType.HELLO.ordinal());
            pkt.setBroadcast(0);
            pkt.setIpAddress(InetAddress.getByName("127.0.0.1"));
            pkt.setPortNum(port);
            pkt.setMessageId((int) (Math.random() * randomFactor));
            pkt.setChunkNum(0);
            pkt.setChunkLength(1);
            pkt.setPayload(data.getBytes());
            final byte[] packet = parser.createPkt(pkt);
            dataOut.write(packet);
            final PacketInfo pkt1 = new PacketInfo();
            pkt.setType(NetworkType.SAMECLUSTER.ordinal());
            pkt.setPriority(0);
            pkt.setModule(0);
            pkt.setConnectionType(NetworkConnectionType.HELLO.ordinal());
            pkt.setBroadcast(0);
            pkt.setIpAddress(InetAddress.getByName("127.0.0.1"));
            pkt.setPortNum(port);
            pkt.setMessageId((int) (Math.random() * randomFactor));
            pkt.setChunkNum(0);
            pkt.setChunkLength(1);
            pkt.setPayload(data.getBytes());
            // final byte[] packet1 = parser.createPkt(pkt1);
            // dataOut.write(packet1);
            destSocket.close();
        } catch (IOException ex) {
        }
    }

}
