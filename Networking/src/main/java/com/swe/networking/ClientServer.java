package com.swe.networking;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * Implement the following.
 * > a send function from client and server perspective
 * > a receive function from the server
 * > The server send function can use the interface abstractTopology
 * to send it to the topology for sending to another cluster
 *
 */

public class ClientServer {
    /**
     * A variable for: Is this client a server?.
     */
    private boolean isServer;

    /**
     * A variable for : Is this client a main server?
     */
    private boolean isMainServer;

    /**
     * Socket reserver for this client/server.
     */
//    private DatagramSocket socket;

    // topology singleton
    private Topology topology = Topology.getTopology();

    private ClientNode deviceIp = null;
    private ClientNode mainServerIp = null;

    private ServerSocket recieveSocket;
    private Socket sendSocket = new Socket();
    private BufferedReader readData;
    private PrintWriter writeData;

    // open an udp server socket for this client
    public ClientServer(ClientNode deviceAddress, ClientNode mainServerAddress) {
        this.isServer = false;
        deviceIp = deviceAddress;
        mainServerIp = mainServerAddress;

        if (deviceAddress == mainServerAddress) {
            this.isMainServer = true;
        }
        try {
            this.recieveSocket = new ServerSocket(deviceAddress.port());
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // TODO: wait for routing packet from the server

    /**
     * Topology server will implement this func.
     *
     * @param dest Destination IP.
     */
    boolean destInCluster(final String dest) {
        return true;
    }

    public void recieveFrom() {
        // testing pending
        try {
            // Buffer to store incoming packet data
            byte[] buffer = new byte[1024];
//            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            sendSocket = this.recieveSocket.accept();
            DataInputStream dataIn = new DataInputStream(sendSocket.getInputStream());
            byte[] packet = dataIn.readAllBytes();
            // Extracting data, destination, and port
            InetAddress dest = sendSocket.getInetAddress();
            int port = sendSocket.getPort();
            PacketParser parser = PacketParser.getPacketParser();
            System.out.println(dest);
            System.out.println(port);
            System.out.println(dest);
            System.out.println(port);
            if (this.isMainServer) {
                int type = parser.getType(packet);
                int connectionType = parser.getConnectionType(packet);
                if (type == 3 && connectionType == 0) {
                    byte[] networkData = topology.getNetwork().toString().getBytes();
                    sendTo(networkData, dest.toString(), port);
                }
            } else if (this.isServer) {
//                if (destInCluster(dest)) {
//                    DatagramPacket response = new DatagramPacket(
//                            data.getBytes(),
//                            data.getBytes().length,
//                            packet.getAddress(),
//                            port
//                    );
//                    writeData.println(Arrays.toString(packet.getData()));
//                } else {
//                    throw new RuntimeException("Destination not in cluster: " + dest);
//                }
            } else {
                // if it is client instead
                // TODO: callMessageListener()
            }
            System.out.println("Well it worked but dont know how....");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to send data to dest:port.
     *
     * @param data Data to be sent across
     * @param dest Destination IP.
     * @param port Destination PORT.
     */
    public int sendTo(final byte[] data, final String dest, final Integer port) throws IOException {
        if (this.isServer) {
            if (destInCluster(dest)) {
                try {
//                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(dest), port);
                    sendSocket.connect(new InetSocketAddress(dest, port));
                    DataOutputStream dataOut = new DataOutputStream(sendSocket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                try {
                    final String hostAddress = topology.getServer(dest).hostName();
                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(hostAddress), port);
                    // TODO: getServer to be implemented by Topology.
                    sendSocket.connect(new InetSocketAddress(hostAddress, port));
                    DataOutputStream dataOut = new DataOutputStream(sendSocket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            if (destInCluster(dest)) {
                try {
                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(dest), port);
                    sendSocket.connect(new InetSocketAddress(dest, port));
                    DataOutputStream dataOut = new DataOutputStream(sendSocket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // TODO: get server ip from routing packet.
                sendSocket.connect(new InetSocketAddress(dest, port));
                DataOutputStream dataOut = new DataOutputStream(sendSocket.getOutputStream());
                dataOut.write(data);
            }
        }
        return 0;
    }
}
