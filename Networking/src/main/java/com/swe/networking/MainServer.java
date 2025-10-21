package com.swe.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The mainserver across all clusters.
 */
public class MainServer implements P2PUser {
    // TODO Must send on receiving a hello packet the current network to the
    // destination
    /**
     * Communicator object to send and receive data.
     */
    private final ProtocolBase communicator;

    /**
     * Singleton packetparser object.
     */
    private final PacketParser parser = PacketParser.getPacketParser();

    /**
     * Singleton Topology object.
     */
    private final Topology topology = Topology.getTopology();

    /**
     * The port at which the TCP server runs.
     */
    private final int serverPort = 8000;

    /**
     * The thread to run the receive function.
     */
    private final Thread receiveThread;

    public MainServer() {
        System.out.println("Creating a new Main Server...");
        communicator = new TCPCommunicator(serverPort);
        receiveThread = new Thread(() -> receive());
        receiveThread.start();
    }

    @Override
    public void send(final byte[] data, final ClientNode[] destIp) {
        for (ClientNode dest : destIp) {
            System.out.println("Sending data");
            communicator.sendData(data, dest); // check of this should be dest
        }
    }

    @Override
    public void receive() {
        while (true) {
            final byte[] packet = communicator.receiveData();
            if (packet != null) {
                parsePacket(packet);
            }
        }
    }

    private void parsePacket(final byte[] packet) {
        try {
            final int connectionType = parser.getConnectionType(packet);
            final int type = parser.getType(packet);
            final InetAddress destInet = parser.getIpAddress(packet);
            final String destinationIp = destInet.getHostAddress();
            final int destinationPort = parser.getPortNum(packet);
            System.out.println("Packet received of type " + Integer.toBinaryString(type)
                    + " and connection type " + Integer.toBinaryString(connectionType) + "...");
            if (type == NetworkType.USE.ordinal()) {
                if (connectionType == NetworkConnectionType.HELLO.ordinal()) {
                    final NetworkStructure network = topology.getNetwork();
                    final ClientNode dest = new ClientNode(destinationIp, destinationPort);
                    final ClientNode[] dests = {dest };
                    final byte[] responsePacket = parser.createPkt(3, 0, 0,
                            4, 0, destInet, destinationPort,
                            (int) (Math.random() * 1000), 0, 1,
                            network.toString().getBytes());
                    send(responsePacket, dests);
                    // Send add packet to all cluster servers
                }
            }
        } catch (UnknownHostException ex) {
        }
    }

    public void closeClient(final ClientNode client) {
        communicator.closeSocket(client);
    }

    @Override
    public void close() {
        receiveThread.interrupt();
        communicator.close();
    }
}
