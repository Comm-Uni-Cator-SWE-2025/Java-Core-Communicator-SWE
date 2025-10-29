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
     * variable to store the main cluster index.
     */
    private final int mainServerClusterIdx;

    /**
     * The timer object to monitor client timeouts.
     */
    private final Timer timer;

    /**
     * Variable to start the timer.
     */
    private final int timerTimeoutMilliSeconds = 2 * 1000;

    /** Variable to store the server IP address. */
    private final ClientNode mainserver;

    /** Variable to store the static serializer. */
    private final NetworkSerializer serializer;

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
        mainserver = mainServerAddress;
        mainServerClusterIdx = 0;
        serializer = NetworkSerializer.getNetworkSerializer();
        timer = new Timer(timerTimeoutMilliSeconds, this::handleClientTimeout);
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

    /**
     * Function to receive the data from the sockets.
     */
    @Override
    public void receive() {
        while (true) {
            final byte[] packet = communicator.receiveData();
            if (packet != null) {
                String payload = "";
                try {
                    payload = new String(parser.parsePacket(packet).getPayload());
                } catch (UnknownHostException e) {
                }
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
            final int connectionType = parser.parsePacket(packet).getConnectionType();
            final int type = parser.parsePacket(packet).getType();
            final InetAddress destInet = parser.parsePacket(packet).getIpAddress();
            final String destinationIp = destInet.getHostAddress();
            final int destinationPort = parser.parsePacket(packet).getPortNum();
            final ClientNode dest = new ClientNode(destinationIp,
                    destinationPort);
            System.out.println("Packet received of type "
                    + type + " and connection type " + connectionType + "...");
            if (type == NetworkType.USE.ordinal()) {
                if (connectionType == NetworkConnectionType.HELLO.ordinal()) {
                    handleHello(dest);
                } else if (connectionType == NetworkConnectionType.REMOVE.ordinal()) {
                    handleRemove(packet, dest);
                } else if (connectionType == NetworkConnectionType.ALIVE.ordinal()) {
                    timer.updateTimeout(dest);
                    System.out.println("Received alive packet from " + dest);
                } else if (connectionType == NetworkConnectionType.CLOSE.ordinal()) {
                    System.out.println("Closing the Main Server");
                }
            } else if (type == NetworkType.OTHERCLUSTER.ordinal()) {
                // might new to change the type to SAMECLUSTER
                final ClientNode newDest = topology.getServer(dest);
                send(packet, newDest);
            } else if (type == NetworkType.SAMECLUSTER.ordinal()) {
                send(packet, dest);
            }
        } catch (UnknownHostException ex) {
        }
    }

    /**
     * Function add client to timer.
     *
     * @param client the client to be added.
     * @param idx    the index of the cluster it belongs
     */
    private void addClientToTimer(final ClientNode client, final int idx) {
        if (idx == mainServerClusterIdx) {
            timer.addClient(client);
        } else if (topology.getAllClusterServers().contains(client)) {
            timer.addClient(client);
        }
    }

    /**
     * Function to send the network strcture to the requested client.
     *
     * @param dest the destination to send the packet
     */
    private void sendNetworkPktResponse(final ClientNode dest) {
        try {
            final NetworkStructure network = topology.getNetwork();
            final int randomFactor = (int) Math.pow(10, 6);

            final PacketInfo responsePacket = new PacketInfo();
            responsePacket.setType(NetworkType.USE.ordinal());
            responsePacket.setPriority(0);
            responsePacket.setModule(ModuleType.NETWORKING.ordinal());
            responsePacket.setConnectionType(NetworkConnectionType.NETWORK.ordinal());
            responsePacket.setBroadcast(0);
            responsePacket.setIpAddress(InetAddress.getByName(dest.hostName()));
            responsePacket.setPortNum(dest.port());
            responsePacket.setMessageId((int) (Math.random() * randomFactor));
            responsePacket.setChunkNum(0);
            responsePacket.setChunkLength(1);
            responsePacket.setPayload(serializer.serializeNetworkStructure(network));
            System.out.println("Sending current network details...");
            System.out.println(network);
            final byte[] responsePkt = parser.createPkt(responsePacket);
            send(responsePkt, dest);
        } catch (UnknownHostException ex) {
        }
    }

    /**
     * Function to send add packet to the given destination.
     *
     * @param dest       the new client to add
     * @param server     the server to send to
     * @param clusterIdx the index of cluster it belongs to
     */
    private void sendAddPktResponse(final ClientNode dest, final ClientNode server, final int clusterIdx) {
        try {
            final int randomFactor = (int) Math.pow(10, 6);
            final ClientNetworkRecord addClient = new ClientNetworkRecord(dest, clusterIdx);
            final PacketInfo addPacket = new PacketInfo();
            addPacket.setType(NetworkType.USE.ordinal());
            addPacket.setPriority(0);
            addPacket.setModule(ModuleType.NETWORKING.ordinal());
            addPacket.setConnectionType(NetworkConnectionType.ADD.ordinal());
            addPacket.setBroadcast(0);
            addPacket.setIpAddress(InetAddress.getByName(server.hostName()));
            addPacket.setPortNum(server.port());
            addPacket.setMessageId((int) (Math.random() * randomFactor));
            addPacket.setChunkNum(0);
            addPacket.setChunkLength(1);
            addPacket.setPayload(serializer.serializeClientNetworkRecord(addClient));
            final byte[] addPkt = parser.createPkt(addPacket);
            send(addPkt, server);
        } catch (UnknownHostException ex) {
        }
    }

    /**
     * Function to remove client from the network.
     *
     * @param packet the packe containing data
     * @param dest   the destination to be removed
     * @throws UnknownHostException when host is unknown
     */
    private void handleRemove(final byte[] packet, final ClientNode dest) throws UnknownHostException {
        final PacketInfo packetInfo = parser.parsePacket(packet);
        final ClientNetworkRecord remClient = serializer.deserializeClientNetworkRecord(packetInfo.getPayload());
        topology.removeClient(remClient);
        if (remClient.clusterIndex() == topology.getClusterIndex(mainserver)) {
            timer.removeClient(dest);
        }
        final int myCluster = topology.getClusterIndex(mainserver);
        for (ClientNode c : topology.getClients(myCluster)) {
            if (c.equals(mainserver)) {
                continue;
            }
            send(packet, c);
        }
        for (ClientNode s : topology.getAllClusterServers()) {
            if (s.equals(mainserver)) {
                continue;
            }
            send(packet, s);
        }
        System.out.println("Client " + remClient.client().hostName()
                + " removed from cluster"
                + remClient.clusterIndex());
    }

    /**
     * Function to handel hello packets.
     *
     * @param dest the destination to the network structure to
     */
    private void handleHello(final ClientNode dest) {
        final int clusterIdx = topology.addClient(dest);
        addClientToTimer(dest, clusterIdx);
        sendNetworkPktResponse(dest);
        // send add packet to all cluster servers.
        final List<ClientNode> servers = topology.getAllClusterServers();
        for (ClientNode server : servers) {
            if (server.equals(mainserver)) {
                continue;
            }
            sendAddPktResponse(dest, server, clusterIdx);
        }

        // send add packet to all cluster clients of this cluster
        final List<ClientNode> clients = topology.getClients(mainServerClusterIdx);
        for (ClientNode client : clients) {
            if (client.equals(mainserver)) {
                continue;
            }
            sendAddPktResponse(dest, client, clusterIdx);
        }
    }

    /**
     * Function to close the socekt of a client.
     *
     * @param client the client to close
     */
    public void closeClient(final ClientNode client) {
        timer.removeClient(client);
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
        timer.close();
    }

    /**
     * Function to handle timeout of clients.
     *
     * @param client the client which was timeout
     */
    private void handleClientTimeout(final ClientNode client) {
        try {
            System.out.println("Reached timeout for client " + client + " ...");
            // send remove packet to all clients in the cluster and main server
            final ClientNetworkRecord remClient = new ClientNetworkRecord(client,
                    topology.getClusterIndex(client));
            topology.removeClient(remClient);
            timer.removeClient(client);
            final PacketInfo packetInfo = new PacketInfo();
            packetInfo.setType(NetworkType.USE.ordinal());
            packetInfo.setConnectionType(NetworkConnectionType.REMOVE.ordinal());
            packetInfo.setPayload(client.toString().getBytes());
            // used when removing clients the destination does not matter here but give to
            // avoid error
            packetInfo.setIpAddress(InetAddress.getByName(client.hostName()));
            packetInfo.setPortNum(client.port());
            final byte[] removePacket = parser.createPkt(packetInfo);
            final List<ClientNode> servers = topology.getAllClusterServers();
            for (ClientNode server : servers) {
                if (server.equals(mainserver)) {
                    continue;
                }
                send(removePacket, server);
            }
            final List<ClientNode> clients = topology.getClients(mainServerClusterIdx);
            for (ClientNode newClient : clients) {
                if (newClient.equals(mainserver)) {
                    continue;
                }
                send(removePacket, newClient);
            }
        } catch (UnknownHostException ex) {
        }
    }
}
