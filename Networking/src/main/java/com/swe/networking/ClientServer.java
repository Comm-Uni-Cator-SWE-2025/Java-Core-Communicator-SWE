package com.swe.networking;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.IOException;
import java.net.InetAddress;

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
    private DatagramSocket socket;

    // topology singleton
    private Topology topology = Topology.getTopology();

    // open an udp server socket for this client
    public ClientServer(ClientNode destination, ClientNode mainServer) {
        this.isServer = false;
        if(destination == mainServer) {
            this.isMainServer = true;
        }
        try {
            this.socket = new DatagramSocket();
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
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            this.socket.receive(packet);

            // Extracting data, destination, and port
            String data = new String(packet.getData(), 0, packet.getLength());
            String dest = packet.getAddress().getHostAddress();
            int port = packet.getPort();
            PacketParser parser = PacketParser.getPacketParser();

            if(this.isMainServer){
                int type = parser.getType(packet.getData());
                int connectionType = parser.getConnectionType(packet.getData());
                if(type == 3 && connectionType == 0){
                    byte[] networkData = topology.getNetwork().toString().getBytes();
                    sendTo(networkData, dest, port);
                }
            }else if (this.isServer) {
                if (destInCluster(dest)) {
                    DatagramPacket response = new DatagramPacket(
                            data.getBytes(),
                            data.getBytes().length,
                            packet.getAddress(),
                            port
                    );
                    this.socket.send(response);
                } else {
                    throw new RuntimeException("Destination not in cluster: " + dest);
                }
            } else {
                // if it is client instead
                // TODO: callMessageListener()
            }

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
    public void sendTo(final byte[] data, final String dest, final Integer port) {
        if (this.isServer) {
            if (destInCluster(dest)) {
                try {
                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(dest), port);
                    try {
                        this.socket.send(sendPacket);
                    } catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                try {
                    final String hostAddress = topology.getServer(dest).hostName();
                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(hostAddress), port);
                    // TODO: getServer to be implemented by Topology.
                    try {
                        this.socket.send(sendPacket);
                    } catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            if (destInCluster(dest)) {
                try {
                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(dest), port);
                    try {
                        this.socket.send(sendPacket);
                    } catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // TODO: get server ip from routing packet.
                // send(data, clusterServerIP, port);
            }
        }
    }
}
