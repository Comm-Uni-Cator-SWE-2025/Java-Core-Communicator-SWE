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
 * */

public class ClientServer {
    /**
     * A variable for: Is this client a server?.
     */
    private boolean isServer;

    /**
     * Socket reserver for this client/server.
     */
    private DatagramSocket socket;

    // open a udp server socket for this client
    public  ClientServer() {
        this.isServer = false;
        try {
            this.socket = new DatagramSocket();
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // TODO: wait for routing packet from the server

    /**
     * com.swe.networking.Topology server will implement this func.
     * @param dest Destination IP.
     */
    boolean destInCluster(final String dest) {
        return true;
    }

    /**
     * Function to send data to dest:port.
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
                    }  catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }  catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                try {
                    AbstractTopology topology = new Topology();
                    String hostAddress = topology.GetServer(dest).hostName();
                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(hostAddress), port); // TODO: getServer to be implemented by com.swe.networking.Topology.
                    try {
                        this.socket.send(sendPacket);
                    }  catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }  catch (IOException e) {
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
                    }  catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }  catch (IOException e) {
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
