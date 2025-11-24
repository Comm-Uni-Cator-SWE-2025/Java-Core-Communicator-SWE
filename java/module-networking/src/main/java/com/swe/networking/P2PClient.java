/*
 * -----------------------------------------------------------------------------
 *  File: P2PClient.java
 *  Owner: Shubham Yadav
 *  Roll Number : 112201032
 *  Module : Metworking
 *
 * -----------------------------------------------------------------------------
 */
package com.swe.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import com.swe.core.ClientNode;

/**
 * The Client belonging to a certain cluster.
 */
public class P2PClient implements P2PUser {

    /**
     * base Commumicator class to send , recieve and close.
     */
    private final ProtocolBase communicator;
    /**
     * get parser to decode packet.
     */
    private final PacketParser parser = PacketParser.getPacketParser();
    /**
     * network topology.
     */
    private final Topology topology = Topology.getTopology();
    /**
     * self client node.
     */
    private final ClientNode deviceAddress;
    /**
     * main server node.
     */
    private final ClientNode mainServerAddress;
    /**
     * cluster server node.
     */
    private ClientNode clusterServerAddress;

    /**
     * current running status.
     */
    private boolean running = true;
    /**
     * recieve thread.
     */
    private final Thread receiveThread;

    /**
     * alive thread manager.
     */
    private ScheduledExecutorService aliveScheduler = null;

    /**
     * time interval gap to send alive packet.
     */
    private static final long ALIVE_INTERVAL_SECONDS = 2;

    /**
     * serializer.
     */
    private final NetworkSerializer serializer = NetworkSerializer.getNetworkSerializer();

    /**
     * variable to store the fixed packet header size in bytes.
     */
    private final int packetHeaderSize = 22;

    /**
     * Variable to store chunk manager.
     */
    private final ChunkManager chunkManager;

    /**
     * Packet handler to process the packet.
     */
    private final PacketHandler packetHandler;

    /**
     * Creates a new P2PClient.
     *
     * @param device The ClientNode info for this device.
     * @param server The ClientNode info for the mainServer.
     * @param tcpCommunicator the communicator for the communication.
     */
    public P2PClient(final ClientNode device, final ClientNode server, final ProtocolBase tcpCommunicator) {
        this.deviceAddress = device;
        this.mainServerAddress = server;
        this.communicator = tcpCommunicator;

        // Initialize ChunkManager with the centralized constant
        chunkManager = ChunkManager.getChunkManager(packetHeaderSize);
        // Initialize the new PacketHandler, passing dependencies
        this.packetHandler = new PacketHandler(this.parser, this.topology, this.serializer, this.chunkManager, this);

        updateClusterServer();

        // Starting the continuous receive loop
        this.receiveThread = new Thread(this::receive);
        this.receiveThread.setName("P2PClient-Receive-Thread");
        this.receiveThread.start();

        // start a scheduled ALIVE packets to the cluster server
//          this.aliveScheduler = Executors.newSingleThreadScheduledExecutor();
//          this.aliveScheduler.scheduleAtFixedRate(this::sendAlivePacket,
//                  ALIVE_INTERVAL_SECONDS, ALIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void send(final byte[] data, final ClientNode[] destClientNode) {
        for (ClientNode dest : destClientNode) {
            sendToSingleNode(data, dest);
        }
        return;
    }

    @Override
    public void send(final byte[] data, final ClientNode destNode) {
        sendToSingleNode(data, destNode);
    }

    /**
     * Helper method to send data to a single destination node.
     *
     * @param data     The raw byte data to send.
     * @param destNode The intended final destination node.
     */
    private void sendToSingleNode(final byte[] data, final ClientNode destNode) {

        final ClientNode sendDest = topology.getDestination(mainServerAddress, destNode);
        System.out.println("p2pclient sending data to: " + sendDest);
        communicator.sendData(data, sendDest);
    }

    @Override
    public void receive() {
        while (running) {
            try {
                final byte[] packet = communicator.receiveData();
                if (packet == null) {
                    continue;
                }
                final List<byte[]> packets = SplitPackets.getSplitPackets().split(packet);
                for (byte[] p : packets) {
                    packetHandler.packetRedirection(p);
                }
            }
            catch (Exception e) {
                System.err.println("p2pclient received exception while processing packet");
            }
        }
    }

    /**
     * Periodically sends an ALIVE (001) packet to this client's ClusterServer.
     */
    private void sendAlivePacket() {
        if (clusterServerAddress == null) {
            System.err.println("p2pclient: Cannot send ALIVE packet. Cluster server address is null.");
            return;
        }

        try {
            System.out.println("p2pclient sending alive packet to: " + clusterServerAddress);
            final InetAddress selfIp = InetAddress.getByName(deviceAddress.hostName());
            final byte[] emptyPayload = new byte[0];

            final PacketInfo aliveInfo = new PacketInfo();
            aliveInfo.setLength(PacketParser.getHeaderSize());
            aliveInfo.setType(NetworkType.USE.ordinal());
            aliveInfo.setPriority(0);
            aliveInfo.setModule(ModuleType.NETWORKING.ordinal());
            aliveInfo.setConnectionType(NetworkConnectionType.ALIVE.ordinal());
            aliveInfo.setBroadcast(0);
            aliveInfo.setIpAddress(selfIp);
            aliveInfo.setPortNum(deviceAddress.port());
            aliveInfo.setMessageId(0);
            aliveInfo.setChunkNum(0);
            aliveInfo.setChunkLength(0);
            aliveInfo.setPayload(emptyPayload);

            final byte[] alivePacket = parser.createPkt(aliveInfo);
            communicator.sendData(alivePacket, clusterServerAddress);

        } catch (UnknownHostException e) {
            System.err.println("p2pclient failed to send alive packet");
        }
    }

    /**
     * Dedicated handler for parsing and redirecting incoming packets.
     * This handles complex application logic.
     */
    private static class PacketHandler {

        private final PacketParser parser;
        private final Topology topology;
        private final NetworkSerializer serializer;
        private final ChunkManager chunkManager;
        private final P2PClient clientContext;

        public PacketHandler(final PacketParser parser, final Topology topology, final NetworkSerializer serializer,
                             final ChunkManager chunkManager, final P2PClient clientContext) {
            this.parser = parser;
            this.topology = topology;
            this.serializer = serializer;
            this.chunkManager = chunkManager;
            this.clientContext = clientContext;
        }

        /**
         * Main packet parsing logic based on the user's specification.
         *
         * @param packet The raw packet data.
         */
        public void packetRedirection(final byte[] packet) {
            System.out.println("p2pclient received packet from: " + clientContext.deviceAddress.hostName());
            try {
                final PacketInfo info = parser.parsePacket(packet);
                final int typeInt = info.getType();
                final NetworkType type = NetworkType.getType(typeInt);

                switch (type) {
                    case CLUSTERSERVER:
                    case SAMECLUSTER:
                    case OTHERCLUSTER:
                        // These packets are dropped by the P2PClient
                        System.out.println("p2pclient received packet and dropping of type :" + type);
                        break;
                    case USE:
                        parseUsePacket(info, packet);
                        break;
                    default:
                        break;
                }
            } catch (UnknownHostException e) {
                System.err.println("p2pclient failed to parse packet IP: " + e.getMessage());
            }
        }

        /**
         * Handles Type 11 (USE) packets based on the connection type.
         *
         * @param info   The raw packet data.
         * @param packet The raw packet data.
         */
        private void parseUsePacket(final PacketInfo info, final byte[] packet) throws UnknownHostException {
            final int connectionTypeInt = info.getConnectionType();
            final NetworkConnectionType connection = NetworkConnectionType.getType(connectionTypeInt);

            System.out.println("p2pclient received connection type: " + connection);

            switch (connection) {
                case HELLO: // 000 drop it only to be received by main server
                case ALIVE: // 001 drop it only to received by cluster server and main server
                    System.out.println("p2pclient received HELLO or ALIVE packet (dropping)");
                    break;

                case ADD: // 010 : update the current network
                    handleUpdateNetwork(info);
                    break;

                case REMOVE: // 011 : update the current network
                    handleRemoveClient(info);
                    break;

                case NETWORK: // 100 : replace the current network
                    handleReplaceNetwork(info);
                    break;

                case MODULE:
                    handleModulePacket(packet);
                    break;

                case CLOSE: // 111 : close the client terminate
                    System.out.println("p2pclient received CLOSE packet");
                    clientContext.close();
                    break;

                default:
                    break;
            }
        }

        /**
         * Helper function to handle add packet
         * @param info received packet info
         */
        private void handleUpdateNetwork(final PacketInfo info) {
            System.out.println("p2pclient received ADD packet: updating network structure.");
            final ClientNetworkRecord newClient = serializer.deserializeClientNetworkRecord(info.getPayload());
            topology.updateNetwork(newClient);
            clientContext.updateClusterServer();
        }

        /**
         * Helper function to handle remove packet
         * @param info received packet info
         */
        private void handleRemoveClient(final PacketInfo info) {
            System.out.println("p2pclient received REMOVE packet.");
            final ClientNetworkRecord oldClient = serializer.deserializeClientNetworkRecord(info.getPayload());
            topology.removeClient(oldClient);
            chunkManager.cleanChunk(oldClient.client());
            clientContext.updateClusterServer();
        }

        /**
         * Helper function to handle Network packet
         * @param info received packet info
         */
        private void handleReplaceNetwork(final PacketInfo info) {
            System.out.println("p2pclient received NETWORK packet: replacing current network structure.");
            final NetworkStructure newNetwork = serializer.deserializeNetworkStructure(info.getPayload());
            topology.replaceNetwork(newNetwork);
            clientContext.updateClusterServer();
        }

        /**
         * Helper function to handle Module packet
         * @param packet received packet
         */
        private void handleModulePacket(final byte[] packet) throws UnknownHostException {
            System.out.println("MODULE packet received.");
            final int module = parser.parsePacket(packet).getModule();
            final byte[] data = chunkManager.addChunk(packet);
            final Networking networking = Networking.getNetwork();
            System.out.println("Data received: " + Arrays.toString(data));
            if (data != null) {
                final PacketInfo destpktInfo = parser.parsePacket(data);
                networking.callSubscriber(module, destpktInfo.getPayload());
            }
        }
    }

    /**
     * this is helper function to update cluster server address. may be changed
     * after changing network structure (add, remove, replace)
     */
    void updateClusterServer() {
        System.out.println("p2pclient after updating server");
        this.clusterServerAddress = topology.getServer(this.deviceAddress);
        if (this.clusterServerAddress == null) {
            System.err.println("p2pclient: Not find my cluster server in topology.");
        }
        return;
    }

    @Override
    public void close() {
        if (!running) {
            return; // multiple closes
        }
        running = false;

        System.out.println("p2pclient started closing");

        // Stop sending ALIVE packets
//        if (aliveScheduler != null) {
//            aliveScheduler.shutdownNow();
//        }

        // Close all network sockets
        if (communicator != null) {
            communicator.close();
        }

        // Stop the receive thread
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
        final byte[] removePkt = createRemovePacket(deviceAddress);
        SplitPackets.getSplitPackets().emptyBuffer();
        System.out.println("p2pclient closed");
    }

    /**
     * Function to create a remove packet.
     *
     * @param client the client to remove
     * @return the packet
     */
    public byte[] createRemovePacket(final ClientNode client) {
        try {
            final PacketInfo packetInfo = new PacketInfo();
            packetInfo.setLength(packetHeaderSize);
            packetInfo.setType(NetworkType.USE.ordinal());
            packetInfo.setConnectionType(NetworkConnectionType.REMOVE.ordinal());
            packetInfo.setPayload(client.toString().getBytes());
            packetInfo.setIpAddress(InetAddress.getByName(client.hostName()));
            packetInfo.setPortNum(client.port());
            packetInfo.setBroadcast(0);
            final byte[] removePacket = parser.createPkt(packetInfo);
            return removePacket;
        } catch (UnknownHostException ex) {
            return null;
        }
    }
}
