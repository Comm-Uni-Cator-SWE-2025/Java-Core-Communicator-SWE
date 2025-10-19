package com.swe.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

/**
 * Test class to test TCPCommunicator class.
 */
public class TCPCommunicatorTest {

    /**
     * Test for checking if message is sent correctly.
     */
    @org.junit.jupiter.api.Test
    public void testSend() {
        try {
            final Thread recieveThread = new Thread(() -> receive());
            recieveThread.start();
            final Integer sleepTime = 500;
            Thread.sleep(sleepTime);
            final ProtocolBase tcp = new TCPCommunicator(8000);
            final String data = "Welcome to the new world!!!";
            final ClientNode dest = new ClientNode("127.0.0.1", 8001);
            final ClientNode dest1 = new ClientNode("127.0.0.1", 8002);
            tcp.sendData(data.getBytes(), dest);
            tcp.sendData(data.getBytes(), dest);
            tcp.sendData(data.getBytes(), dest1);
            System.out.println("Data sent successfully...");
            tcp.close();
            tcp.closeSocket(dest);
            recieveThread.join();
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Sample function to receive data.
     */
    public void receive() {
        try {
            final ServerSocket serverSocket = new ServerSocket(8001);
            serverSocket.setSoTimeout(0);
            final Socket socket = serverSocket.accept();
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final byte[] packet = dataIn.readAllBytes();
            System.out.println(new String(packet));
            assertEquals(new String(packet), "Welcome to the new world!!!Welcome to the new world!!!");
            System.out.println("Data received successfully...");
            serverSocket.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Test for checking if message is received correctly.
     */
    @org.junit.jupiter.api.Test
    public void testReceive() {
        try {
            final ProtocolBase tcp = new TCPCommunicator(8000);
            final Thread receiveThread = new Thread(() -> {
                try {
                    tcp.receiveData();
                    final Integer sleepTime = 1000;
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                }
            });
            receiveThread.start();
            send();
            receiveThread.join();
        } catch (InterruptedException ex) {
            System.out.println("Receive thread is interrupted...");
        }
    }

    /**
     * Sample function to receive data.
     */
    public void send() {
        final Socket destSocket = new Socket();
        try {
            final Integer port = 8000;
            final Integer timeout = 8000;
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final String data = "Hello World !!!";
            dataOut.write(data.getBytes());
            destSocket.close();
        } catch (IOException ex) {
        }
    }
}
