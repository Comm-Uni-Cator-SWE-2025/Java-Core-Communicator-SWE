package com.swe.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * The mainserver across all clusters.
 */
public class MainServer implements P2PUser {
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
    private final int serverPort;

    /**
     * The thread to run the receive function.
     */
    private final Thread receiveThread;

    /**
     * Constructor function for the main server class.
     *
     * @param deviceAddress     the IP address of the device
     * @param mainServerAddress the IP address of the main server
     */
    public MainServer(final ClientNode deviceAddress,
            final ClientNode mainServerAddress) {
        System.out.println("Creating a new Main Server...");
        serverPort = deviceAddress.port();
        System.out.println("Listening at port:" + serverPort + " ...");
        communicator = new TCPCommunicator(serverPort);
        receiveThread = new Thread(() -> receive());
        receiveThread.start();
    }

    /**
     * Function to send to list of a destinations.
     *
     * @param data   the data to be sent
     * @param destIp the destination to which the data is sent
     */
    @Override
    public void send(final byte[] data, final ClientNode[] destIp) {
        for (ClientNode dest : destIp) {
            System.out.println("Sending data");
            communicator.sendData(data, dest); // check of this should be dest
        }
    }

    /**
     * Function to send to a single destination.
     *
     * @param data   the data to be sent
     * @param destIp the destination to which the data is sent
     */
    @Override
    public void send(final byte[] data, final ClientNode destIp) {
        System.out.println("Sending data");
        communicator.sendData(data, destIp); // check of this should be dest
    }

    @Override
    public void send(byte[] data) {

    }

    /**
     * Function to receive the data from the sockets.
     */
    @Override
    public void receive() {
        while (true) {
            final byte[] packet = communicator.receiveData();
            if (packet != null) {
                final String payload = new String(parser.getPayload(packet));
                System.out.println("Data received : " + payload);
                parsePacket(packet);
            }
        }
    }

    /**
     * Function to parse the packet received and take necessary action.
     *
     * @param packet the packet received
     */
    private void parsePacket(final byte[] packet) {
        try {
            final int connectionType = parser.getConnectionType(packet);
            final int type = parser.getType(packet);
            final InetAddress destInet = parser.getIpAddress(packet);
            final String destinationIp = destInet.getHostAddress();
            final int destinationPort = parser.getPortNum(packet);
            final ClientNode dest = new ClientNode(destinationIp,
                    destinationPort);
            System.out.println("Packet received of type "
                    + Integer.toBinaryString(type)
                    + " and connection type "
                    + Integer.toBinaryString(connectionType) + "...");
            if (type == NetworkType.USE.ordinal()) {
                if (connectionType == NetworkConnectionType.HELLO.ordinal()) {
                    final int clusterIdx = topology.addClient(dest);
                    final NetworkStructure network = topology.getNetwork();
                    final byte[] responsePacket = parser.createPkt(3, 0, 0,
                            4, 0, destInet, destinationPort,
                            (int) (Math.random() * 1000), 0, 1,
                            network.toString().getBytes());
                    System.out.println("Sending current network details...");
                    send(responsePacket, dest);
                    final List<ClientNode> servers = topology.getAllClusterServers();
                    for (ClientNode server : servers) {
                        final ClientNetworkRecord addClient = new ClientNetworkRecord(dest, clusterIdx);
                        final byte[] addPacket = parser.createPkt(3, 0, 0,
                                4, 0, InetAddress.getByName(server.hostName()),
                                server.port(), (int) (Math.random() * 1000),
                                0, 1, addClient.toString().getBytes());
                        send(addPacket, server);
                    }
                } else if (connectionType == NetworkConnectionType.ALIVE.ordinal()) {
                    // TODO Create a timer class object
                } else if (connectionType == NetworkConnectionType.CLOSE.ordinal()) {
                    // TODO Decide whether to close the networking class
                    System.out.println("Closing the Main Server");
                }
            } else if (type == NetworkType.OTHERCLUSTER.ordinal()) {
                final ClientNode newDest = topology.getServer(dest);
                send(packet, newDest);
            } else if (type == NetworkType.SAMECLUSTER.ordinal()) {
                send(packet, dest);
            }
        } catch (UnknownHostException ex) {
        }
    }

    /**
     * Function to close the socekt of a client.
     *
     * @param client the client to close
     */
    public void closeClient(final ClientNode client) {
        communicator.closeSocket(client);
    }

    /**
     * Function to close the main server class.
     *
     */
    @Override
    public void close() {
        receiveThread.interrupt();
        communicator.close();
    }
}
