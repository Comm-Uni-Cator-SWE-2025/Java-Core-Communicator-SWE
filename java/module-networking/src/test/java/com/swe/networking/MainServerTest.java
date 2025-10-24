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
            final ClientNode[] dests = {dest1, dest2 };
            final String data = "Good morning !!!";
            mainServer.send(data.getBytes(), dests);
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
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final String data = "Hello World !!!";
            final PacketParser parser = PacketParser.getPacketParser();
            final byte[] packet = parser.createPkt(3, 0, 0, 0, 0,
                    InetAddress.getByName("127.0.0.1"), port, (int) (Math.random() * 1000),
                    0, 1, data.getBytes());
            dataOut.write(packet);
            final byte[] packet1 = parser.createPkt(1, 0, 0, 0, 0,
                    InetAddress.getByName("127.0.0.1"), destSocket.getLocalPort(), (int) (Math.random() * 1000),
                    0, 1, data.getBytes());
            dataOut.write(packet1);
            destSocket.close();
        } catch (IOException ex) {
        }
    }

}
