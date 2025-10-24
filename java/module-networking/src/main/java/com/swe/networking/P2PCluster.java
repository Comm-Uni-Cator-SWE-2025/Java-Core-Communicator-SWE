package com.swe.networking;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The Cluster class to store details of a given cluster.
 *
 */
public class P2PCluster {
    /**
     * List of all clients belonging to the same cluster.
     */
    private List<ClientNode> clients;

    /**
     * The status of current device as client / server.
     */
    private P2PUser user;

    /**
     * The server details of the current cluster.
     */
    private ClientNode clusterServer;

    /**
     * To check if the current device is a server or not.
     */
    private boolean isServer = false;

    /**
     * Packet parser singleton object.
     */
    private static PacketParser packetParser = PacketParser.getPacketParser();

    public P2PCluster() {
        System.out.println("Creating a new P2P Cluster...");
        clients = new ArrayList<>();
    }

    /**
     * Function to add current user to the network.
     * 
     * @param client details of the current client
     * @param server details of the mainserver
     */
    public void addUser(final ClientNode client, final ClientNode server) {
        System.out.println("Adding new user to the network...");
        // send hello to server
        final byte[] helloPacket = packetParser.createPkt(
            NetworkType.USE.ordinal(), 0, 0, NetworkConnectionType.HELLO.ordinal(), 
            0, null, 0, 0, 0, 0, null); 

        try {
            final Socket socket = new Socket(client.hostName(), client.port());
            final OutputStream out = socket.getOutputStream();
            out.write(helloPacket);
            out.flush();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            final Socket socket = new Socket(client.hostName(), client.port());
            final InputStream in = socket.getInputStream();
            final byte[] packet = new byte[4096];
            final int bytesRead = in.read(packet);
            if (bytesRead > 0) {
                System.out.println("Received structure from server: " + server.hostName());
                clients.add(client);
                // deserialize the object from packet
                final NetworkStructure networkStructure = packetParser.getPayload(packet);
                for (int i = 0; i < networkStructure.servers().size(); i++) {
                    if (networkStructure.servers().get(i).equals(client)) {
                        clusterServer = client;
                        user = new P2PServer(client, server);
                        for (ClientNode c : networkStructure.clusters().get(i)) {
                            ((P2PServer) user).monitor(c);
                        }
                        break;
                    }
                    if (networkStructure.clusters().get(i).contains(client)) {
                        clusterServer = networkStructure.servers().get(i);
                        this.isServer = false;
                        user = new P2PClient(client, server);
                        break;
                    }
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to resize the cluster dynamically.
     * 
     * @param size The required size of cluster
     */
    public void resizeCluster(final Integer size) {
        if (clients.size() < size - 1) {
            System.out.println("Cannot resize cluster...");
        }
    }    
}