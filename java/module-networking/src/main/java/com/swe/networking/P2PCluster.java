package com.swe.networking;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
    public void addUser(final ClientNode client, final ClientNode server) throws UnknownHostException {
        System.out.println("Adding new user to the network...");
        // send hello to server
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.USE.ordinal());
        packetInfo.setConnectionType(NetworkConnectionType.HELLO.ordinal());
        packetInfo.setIpAddress(InetAddress.getByName(client.hostName()));
        packetInfo.setPortNum(client.port());
        packetInfo.setPayload(new byte[0]);
        final byte[] helloPacket = packetParser.createPkt(packetInfo);

        try {
            final Socket socket = new Socket(server.hostName(), server.port(), InetAddress.getByName(client.hostName()), client.port());
            final OutputStream out = socket.getOutputStream();
            out.write(helloPacket);
            out.flush();
            // read response from server
            final InputStream in = socket.getInputStream();
            final byte[] packet = new byte[4096];
            final int bytesRead = in.read(packet);
            socket.close();
            if (bytesRead > 0) {
                System.out.println("Received structure from server: " + server.hostName());
                clients.add(client);
                // deserialize the object from packet
                packetInfo = packetParser.parsePacket(packet);
                final NetworkStructure networkStructure =
                    NetworkSerializer.getNetworkSerializer()
                        .deserializeNetworkStructure(packetInfo.getPayload());
                for (int i = 0; i < networkStructure.servers().size(); i++) {
                    if (networkStructure.servers().get(i).equals(client)) {
                        clusterServer = client;
                        user = new P2PServer(client, server);
                        for (ClientNode c : networkStructure.clusters().get(i)) {
                            ((P2PServer) user).monitor(c);
                        }
                        // send network packet to the P2P server
                        final Socket serverSocket =
                            new Socket(client.hostName(), client.port());
                        final OutputStream serverOut = serverSocket.getOutputStream();
                        final PacketInfo netPacketInfo = new PacketInfo();
                        netPacketInfo.setType(NetworkType.USE.ordinal());
                        netPacketInfo.setConnectionType(NetworkConnectionType.NETWORK.ordinal());
                        netPacketInfo.setIpAddress(InetAddress.getByName(client.hostName()));
                        netPacketInfo.setPortNum(client.port());
                        netPacketInfo.setPayload(
                            NetworkSerializer.getNetworkSerializer()
                                .serializeNetworkStructure(networkStructure));
                        final byte[] networkPacket = packetParser.createPkt(netPacketInfo);
                        serverOut.write(networkPacket);
                        serverOut.flush();
                        serverSocket.close();
                        this.isServer = true;
                        break;
                    }
                    if (networkStructure.clusters().get(i).contains(client)) {
                        clusterServer = networkStructure.servers().get(i);
                        this.isServer = false;
                        // user = new P2PClient(client, server);
                        break;
                    }
                }
            }
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