/*
 * -----------------------------------------------------------------------------
 *  File: Networking.java
 *  Owner: Shubham Yadav
 *  Roll Number : 112201032
 *  Module : Networking
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.networking;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

/**
 * The main class of the networking module.
 */
public class Networking implements AbstractNetworking, AbstractController {

    /**
     * The singeton variable to store the class object.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("NETWORKING");

    /**
     * The singleton object.
     */
    private static Networking networking;
    /**
     * The variable to store all the listeners subscribed to the module.
     */
    private final HashMap<Integer, MessageListener> listeners = new HashMap<>();

    /**
     * The variable to store the client details.
     */
    private ClientNode user;

    /**
     * The variable to store singleton chunk manager.
     */
    private final ChunkManager chunkManager;

    /**
     * The variable to store singleton priority queue.
     */
    private PriorityQueue priorityQueue;
    /**
     * The variable to store singleton priority queue.
     */
    private PacketParser parser;

    /**
     * The variable to store singleton topology.
     */
    private final Topology topology;

    /**
     * The variable to store maximum packet size to chunk.
     */
    private final int payloadSize = 10 * 1024; // 10 KB

    /**
     * Variable to store the rpc for the app.
     */
    private AbstractRPC moduleRPC = null;

    /**
     * The thread to send data continuously.
     */
    private Thread sendThread;

    /**
     * Private constructor for Netwroking class.
     */
    private Networking() {
        chunkManager = ChunkManager.getChunkManager(payloadSize);
        priorityQueue = PriorityQueue.getPriorityQueue();
        parser = PacketParser.getPacketParser();
        topology = Topology.getTopology();
        sendThread = new Thread(this::start);
        sendThread.start();
    }

    /**
     * Function to return the singleton Networking object.
     *
     * @return the singleton object
     */
    public static Networking getNetwork() {
        if (networking == null) {
            LOG.info("Instantiated Networking module...");
            networking = new Networking();
            return networking;
        }
        LOG.info("Already instantiated Networking module...");
        return networking;
    }

    /**
     * Function to chunk and send the recevied data to the queue.
     *
     * @param data the data to be sent
     * @param dest the dest to send to
     * @param module the module to be sent to
     * @param priority the priority of the data
     */
    @Override
    public void sendData(final byte[] data, final ClientNode[] dest, final int module, final int priority) {
        if (dest == null) {
            LOG.info("No destination to send to...");
            return;
        }
        LOG.info("Data length : " + data.length);
        LOG.info("Destination : " + Arrays.toString(dest));
        final Vector<byte[]> chunks = getChunks(data, dest, module, priority, 0);
        LOG.info("chunk number : " + chunks.size());
        for (byte[] chunk : chunks) {
            try {
                final PacketInfo pktInfo = parser.parsePacket(chunk);
                final InetAddress addr = pktInfo.getIpAddress();
                final int port = pktInfo.getPortNum();
                final ClientNode newdest = new ClientNode(addr.getHostAddress(), port);
                // System.out.println("Time to create new dest: " + (endTime - startTime) + " ms");
                System.out.println("Destination " + newdest);
                // topology.sendPacket(chunk, newdest);
                priorityQueue.addPacket(chunk);
            } catch (UnknownHostException ex) {
                System.out.println("Unknown host exception: " + ex.getMessage());
            }
        }
    }

    /**
     * Function to continuously send data.
     */
    public void start() {
        while (!Thread.currentThread().isInterrupted()) {
            if (!priorityQueue.isEmpty()) {
                final byte[] packet = priorityQueue.nextPacket();
                try {
                    final PacketInfo pktInfo = parser.parsePacket(packet);
                    final InetAddress addr = pktInfo.getIpAddress();
                    final int port = pktInfo.getPortNum();
                    final ClientNode dest = new ClientNode(addr.getHostAddress(), port);
                    topology.sendPacket(packet, dest);
                } catch (UnknownHostException e) {
                    LOG.error("Exception", e);
                }
            }
        }
    }

    /**
     * Function to chunk the given data by the chunk manager.
     *
     * @param data the data to be sent
     * @param dest the dest to send the packet
     * @param module the module to be sent to
     * @param priority the priority of the packet
     * @param broadcast the data should b broadcasted or not
     * @return the chunks of the data
     */
    private Vector<byte[]> getChunks(final byte[] data, final ClientNode[] dest, final int module, final int priority,
            final int broadcast) {
        final PacketInfo pkt = new PacketInfo();
        pkt.setModule(module);
        pkt.setPriority(priority);
        pkt.setBroadcast(broadcast);
        final Vector<byte[]> chunks = new Vector<>();
        for (ClientNode client : dest) {
            try {
                pkt.setPayload(data);
                final int type = topology.getNetworkType(user, client);
                pkt.setType(type);
                pkt.setIpAddress(InetAddress.getByName(client.hostName()));
                pkt.setPortNum(client.port());
                pkt.setConnectionType(NetworkConnectionType.MODULE.ordinal());
                chunks.addAll(chunkManager.chunk(pkt));
            } catch (UnknownHostException ex) {
            }
        }
        return chunks;
    }

    /**
     * Function to chunk the given data by the chunk manager to all clients.
     * here the dest does not matter
     *
     * @param data the data to be sent
     * @param module the module to be sent to
     * @param priority the priority of the packet
     */
    @Override
    public void broadcast(final byte[] data, final int module, final int priority) {
        // Get all the destinations to send the broadcast
        List<ClientNode> dest = new ArrayList<>();
        final List<ClientNode> clientDests = new ArrayList<>(topology.getClients(topology.getClusterIndex(user)));
        System.out.println("Clientdest " + clientDests);
        dest = clientDests;
        System.out.println("dest " + dest + " user: " + user + " server " + topology.getServer(user));
        dest.remove(user);

        if (user == topology.getServer(user)) {
            final List<ClientNode> servers = new ArrayList<>(topology.getAllClusterServers());
            dest.addAll(servers);
            dest.remove(user);
            System.out.println("Servers " + servers);
        }

        final ClientNode[] destArray = dest.toArray(ClientNode[]::new);
        System.out.println("Broadcasting clients " + Arrays.toString(destArray));
        final Vector<byte[]> chunks = getChunks(data, destArray, module, priority, 1);
        for (byte[] chunk : chunks) {
            for (ClientNode client : dest) {
                try {
                    // topology.sendPacket(chunk, client);
                    priorityQueue.addPacket(chunk);
                } catch (UnknownHostException ex) {
                }
            }
        }
    }

    /**
     * Function that other modules subscribe to.
     *
     * @param name the nameId of the module.
     * @param function the function to be called
     */
    @Override
    public void subscribe(final int name, final MessageListener function) {
        if (!listeners.containsKey(name)) {
            listeners.put(name, function);
            LOG.info("Added a new subscriber...");
            return;
        }
        LOG.info("The name already exist...");
    }

    /**
     * Function to remove Subscription from the networking.
     *
     * @param name the nameId of the module
     */
    @Override
    public void removeSubscription(final int name) {
        if (listeners.containsKey(name)) {
            listeners.remove(name);
            LOG.info("The module " + name + " is removed...");
            return;
        }
        LOG.info("The name doesnot exist...");
    }

    @Override
    public void addUser(final ClientNode deviceAddress, final ClientNode mainServerAddress) {
        user = deviceAddress;
        topology.addUser(deviceAddress, mainServerAddress);
    }

    /**
     * Function to call the subscirbed modules.
     *
     * @param module the module to call
     * @param data the data to sent
     */
    public void callSubscriber(final int module, final byte[] data) {
        final MessageListener function = listeners.get(module);
        if (function == null) {
            System.out.println("No function found for module: " + module);
        } else {
            function.receiveData(data);
        }
    }

    /**
     * Function called to close the networking module.
     */
    @Override
    public void closeNetworking() {
        LOG.info("Closing Networking module...");
        topology.closeTopology();
        sendThread.interrupt();
    }

    /**
     * Function to consume the RPC.
     *
     * @param rpc the rpc to consume by the networking
     */
    @Override
    public void consumeRPC(final AbstractRPC rpc) {
        moduleRPC = rpc;
        final NetworkRPC networkRPC = NetworkRPC.getNetworkRPC();
        moduleRPC.subscribe("getNetworkRPCAddUser", networkRPC::networkRPCAddUser);
        moduleRPC.subscribe("networkRPCBroadcast", networkRPC::networkRPCBroadcast);
        moduleRPC.subscribe("networkRPCRemoveSubscription", networkRPC::networkRPCRemoveSubscription);
        moduleRPC.subscribe("networkRPCSendData", networkRPC::networkRPCSendData);
        moduleRPC.subscribe("networkRPCSubscribe", networkRPC::networkRPCSubscribe);
        moduleRPC.subscribe("networkRPCCloseNetworking", networkRPC::networkRPCCloseNetworking);
    }

    /**
     * Function to get the RPC.
     *
     * @return the moduleRPC
     */
    public AbstractRPC getRPC() {
        return moduleRPC;
    }

    @Override
    public boolean isClientAlive(final ClientNode client) {
        return topology.checkClientPresent(client);
    }

    /**
     * Function to check if the main server is live by attempting to connect to
     * a high availability public DNS server (Google or Cloudflare). This serves
     * as a network connectivity check to determine if the main server could
     * potentially be reachable.
     *
     * @return true if connection fails (network appears down), false otherwise
     */
    @Override
    public boolean isMainServerLive() {
        final int timeout = 2000;
        final String[] dnsServers = {
            "8.8.8.8", // Google DNS
            "8.8.4.4", // Google DNS secondary
            "1.1.1.1", // Cloudflare DNS
            "1.0.0.1", // Cloudflare DNS secondary
        };
        final int[] ports = {53, 80}; // DNS port and HTTP port

        // Try to connect to each DNS server on each port
        for (String dnsServer : dnsServers) {
            for (int port : ports) {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(dnsServer, port), timeout);
                    // Connection succeeded
                    System.out.println("Successfully connected to " + dnsServer + ":" + port);
                    return false; // Connection succeeded, return false
                } catch (Exception e) {
                    // Connection failed, continue to next server/port
                    System.out.println("Failed to connect to " + dnsServer + ":" + port + " - " + e.getMessage());
                }
            }
        }

        // All connection attempts failed
        System.out.println("All connection attempts to public DNS servers failed");
        return true; // All connections failed, return true
    }
}
