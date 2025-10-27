package com.swe.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final ScheduledExecutorService aliveScheduler;

    /**
     * time interval gap to send alive packet.
     */
    private static final long ALIVE_INTERVAL_SECONDS = 2;

    /**
     * Creates a new P2PClient.
     *
     * @param device The ClientNode info for this device.
     * @param server The ClientNode info for the mainServer.
     */
    public P2PClient(final ClientNode device, final ClientNode server) {
        this.deviceAddress = device;
        this.mainServerAddress = server;

        this.communicator = new TCPCommunicator(device.port());

        // Starting the continuous receive loop
        this.receiveThread = new Thread(this::receive);
        this.receiveThread.setName("P2PClient-Receive-Thread");
        this.receiveThread.start();

        // send the initial HELLO packet to the mainServer
        sendHelloToMainServer();

        // start a scheduled ALIVE packets to the cluster server
        this.aliveScheduler = Executors.newSingleThreadScheduledExecutor();
        
        this.aliveScheduler.scheduleAtFixedRate(this::sendAlivePacket,
                ALIVE_INTERVAL_SECONDS, ALIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void send(final byte[] data, final ClientNode[] destIp) {
        for (ClientNode dest : destIp) {
            System.out.println("p2pclient sending data to: " + dest);
            communicator.sendData(data, dest);
        }
        return;
    }

    @Override
    public void send(final byte[] data, final ClientNode destIp) {

        System.out.println("p2pclient sending data to: " + destIp);
        communicator.sendData(data, destIp);
        return;
    }

    /**
     * Sends the initial HELLO (000) packet to the MainServer.
     */
    private void sendHelloToMainServer() {
        try {
            System.out.println("p2pclient sending hello to main server");

            final InetAddress selfIp = InetAddress.getByName(deviceAddress.hostName());

            final byte[] emptyPayload = new byte[0];

            final PacketInfo helloInfo = new PacketInfo();
            helloInfo.setType(NetworkType.USE.ordinal());
            helloInfo.setPriority(0);
            helloInfo.setModule(ModuleType.NETWORKING.ordinal());
            helloInfo.setConnectionType(NetworkConnectionType.HELLO.ordinal());
            helloInfo.setBroadcast(0);
            helloInfo.setIpAddress(selfIp);
            helloInfo.setPortNum(deviceAddress.port());
            helloInfo.setMessageId(0);
            helloInfo.setChunkNum(0);
            helloInfo.setChunkLength(0);
            helloInfo.setPayload(emptyPayload);

            final byte[] helloPacket = parser.createPkt(helloInfo);
            communicator.sendData(helloPacket, mainServerAddress);

        } catch (UnknownHostException e) {
            System.err.println("p2pclient failed to send hello to main server");
        }
    }

    /**
     * Periodically sends an ALIVE (001) packet to this client's ClusterServer.
     */
    private void sendAlivePacket() {
        if (clusterServerAddress == null) {
            return;
        }

        try {
            System.out.println("p2pclient sending alive packet to: " + clusterServerAddress);
            final InetAddress selfIp = InetAddress.getByName(deviceAddress.hostName());
            final byte[] emptyPayload = new byte[0];


            final PacketInfo aliveInfo = new PacketInfo();
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

    @Override
    public void receive() {
        while (running) {
            try {
                final byte[] packet = communicator.receiveData();
                if (packet == null) {
                    if (running) {
                        System.err.println("p2pclient received empty packet");
                    }
                    continue;
                }
                packetRedirection(packet);

            } catch (Exception e) {
                if (running) {
                    System.err.println("p2pclient received exception while processing packet");
                }
            }
        }
    }

    /**
     * Main packet parsing logic based on the user's specification.
     *
     * @param packet The raw packet data.
     */
    private void packetRedirection(final byte[] packet) {
        try {
            final PacketInfo info = parser.parsePacket(packet);

            final int typeInt = info.getType();
            final NetworkType type = NetworkType.getType(typeInt);

            switch (type) {
                case CLUSTERSERVER:
                case SAMECLUSTER:
                case OTHERCLUSTER:
                    // dropping the packet
                    break;

                case USE:
                    parseUsePacket(info);
                    break;

                default:
                    System.err.println("p2pclient received unknown packet type");
            }
        } catch (UnknownHostException e) {
            System.err.println("p2pclient failed to parse packet IP: " + e.getMessage());
        }
    }

    /**
     * Handles Type 11 (USE) packets based on the connection type.
     *
     * @param info The raw packet data.
     */
    private void parseUsePacket(final PacketInfo info) {
        final int connType = info.getConnectionType();
        final NetworkConnectionType connection = NetworkConnectionType.getType(connType);
        final NetworkSerializer serializer = NetworkSerializer.getNetworkSerializer();

        switch (connection) {
            case HELLO: // 000 drop it only to be received by  main server
            case ALIVE: // 001 drop it only to received by cluster server and main server

                System.out.println("p2pclient received HELLO packet");
                break;

            case ADD: // 010 : update the current network
                System.out.println("p2pclient received ADD packet : updating network structure");

                ClientNetworkRecord newClient = serializer.deserializeClientNetworkRecord(info.getPayload());
                topology.updateNetwork(newClient);

                updateClusterServer();
                break;
            case REMOVE: // 011 : update the current network

                System.out.println("p2pclient received ADD or REMOVE packet");
                ClientNetworkRecord oldClient = serializer.deserializeClientNetworkRecord(info.getPayload());
                topology.removeClient(oldClient);

                updateClusterServer();
                break;

            case NETWORK: // 100 : replace the current network

                System.out.println("p2pclient received NETWORK packet");
                NetworkStructure newNetwork = serializer.deserializeNetworkStructure(info.getPayload());
                topology.replaceNetwork(newNetwork);

                updateClusterServer();

                break;

            case CLOSE: // 111 : close the client terminate

                System.out.println("p2pclient received CLOSE packet");
                close();
                break;

            default:
                System.err.println("p2pclient received unknown packet type");
        }
    }

    /**
     * this is helper function to update cluster server address
     * may be changed after changing network structure (add, remove, replace)
     */

    void updateClusterServer(){
        this.clusterServerAddress = topology.getServer(this.deviceAddress);
        if(this.clusterServerAddress == null){
            System.err.println("p2pclient: Not find my cluster server in topology.");
        }
        return;
    }

    @Override
    public void close() {
        if (!running) {
            return; //  multiple closes
        }
        running = false;

        System.out.println("p2pclient started closing");

        // Stop sending ALIVE packets
        if (aliveScheduler != null) {
            aliveScheduler.shutdownNow();
        }

        // Stop the receive thread
        if (receiveThread != null) {
            receiveThread.interrupt();
        }

        // Close all network sockets
        if (communicator != null) {
            communicator.close();
        }

        System.out.println("p2pclient closed");
    }
}
