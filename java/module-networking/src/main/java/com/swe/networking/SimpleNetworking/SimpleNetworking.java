package com.swe.networking.SimpleNetworking;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Vector;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.NetworkConnectionType;
import com.swe.networking.PacketInfo;

// File owned by Loganath
/**
 * The main module of the simple networking class.
 */
public final class SimpleNetworking
        implements AbstractController, AbstractNetworking {

    /**
     * Variable to store the name of the module.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("NETWORKING");

    private static final String MODULENAME = "[SIMPLENETWORKING]";
    /**
     * The singeton variable to store the class object.
     */
    private static SimpleNetworking simpleNetwork;
    /**
     * The variable to store the user object.
     */
    private static IUser user;
    /**
     * The variable to store all the listeners subscribed to the module.
     */
    private final HashMap<ModuleType, MessageListener> listeners = new HashMap<>();
    /**
     * The variable to store the main host IP address.
     */
    private ClientNode serverAddr;
    /**
     * The variable to store the thread to receive the messages from.
     */
    private Thread receiveThread;
    /**
     * The variable to store the user object.
     */
    private boolean exit = false;

    /**
     * The variable to store chunk Manager.
     */
    private final SimpleChunkManager chunkManager;

    /**
     * The variable to store chunk Manager payload size.
     */
    private final int payloadSize = 15 * 1024;

    /** The variable to store the RPC instance. */
    private AbstractRPC rpc;

    private SimpleNetworking() {
        chunkManager = SimpleChunkManager.getChunkManager(payloadSize);
        LOG.info("SimpleNetworking initialized...");
    }

    /**
     * Function to call when we want to stop networking module.
     */
    @Override
    public void closeNetworking() {
        exit = true;
        user.closeUser();
        LOG.info("Closing Networking module...");
    }

    /**
     * Function to return the singleton simple Network object.
     *
     * @return the singleton object
     */
    public static SimpleNetworking getSimpleNetwork() {
        if (simpleNetwork == null) {
            simpleNetwork = new SimpleNetworking();
            return simpleNetwork;
        }
        LOG.info("Passing already instantiated SimpleNetworking module...");
        return simpleNetwork;
    }

    /**
     * Function to add Ip address details about the current user. Must be called
     * only once
     *
     * @param deviceAddress the IP address of the device
     * @param mainServerAddress the IP address of the mainServer
     */
    @Override
    public void addUser(final ClientNode deviceAddress,
            final ClientNode mainServerAddress) {
        serverAddr = mainServerAddress;
        LOG.info("Device details : " + deviceAddress);
        LOG.info("Server details : " + mainServerAddress);
        if (deviceAddress.equals(mainServerAddress)) {
            user = new Server(deviceAddress);
            LOG.info("Device initialized as a server...");
        } else {
            user = new Client(deviceAddress);
            LOG.info("Device initialized as a client...");
        }
        receiveThread = new Thread(() -> receiveData());
        receiveThread.start();
    }

    /**
     * Method to consume the RPC.
     *
     * @param rpcInstance the RPC to consume.
     */
    @Override
    public void consumeRPC(final AbstractRPC rpcInstance) {
        this.rpc = rpcInstance;
        LOG.info("RPC consumed by SimpleNetworking...");
    }

    /**
     * Function to chunk the given data by the chunk manager.
     *
     * @param data the data to be sent
     * @param dest the dest to send the packet
     * @param module the module to be sent to
     * @param priority the priority of the packet
     * @param broadcast the data should broadcasted or not
     * @return the chunks of the data
     */
    private Vector<byte[]> getChunks(final byte[] data, final ClientNode[] dest, final int module, final int priority,
            final int broadcast) {
        final PacketInfo pkt = new PacketInfo();
        pkt.setModule(module);
        pkt.setPriority(priority);
        pkt.setBroadcast(broadcast);
        pkt.setPayload(data);
        Vector<byte[]> chunks = new Vector<>();
        for (ClientNode client : dest) {
            try {
                pkt.setType(0);
                pkt.setIpAddress(InetAddress.getByName(client.hostName()));
                pkt.setPortNum(client.port());
                pkt.setConnectionType(NetworkConnectionType.MODULE.ordinal());
                chunks = chunkManager.chunk(pkt);
            } catch (UnknownHostException ex) {
                LOG.error("Error on chunking data...");
            }
        }
        return chunks;
    }

    /**
     * Function to send the given data to a list of destinations.
     *
     * @param data the data to be sent
     * @param destIp the list of destination to whom the data is sent
     * @param module the destination module id
     * @param priority the priority of the send message
     */
    @Override
    public void sendData(final byte[] data, final ClientNode[] destIp,
            final ModuleType module, final int priority) {
        final Vector<byte[]> chunks = getChunks(data, destIp, module.ordinal(), priority, 0);
        for (byte[] payload : chunks) {
            user.send(payload, destIp, serverAddr, module);
        }
    }

    /**
     * Function to receiveData until the thread stops.
     */
    private void receiveData() {
        while (!exit) {
            try {
                user.receive();
            } catch (IOException e) {
                LOG.error("Error on receiving data...");
            }
        }
        LOG.warn("Thread exited from receiving data...");
    }

    /**
     * Function to add a module function to subscribers.
     *
     * @param name Name of the module.
     * @param func the function to call on receiveing data.
     */
    @Override
    public void subscribe(final ModuleType name, final MessageListener func) {
        if (!listeners.containsKey(name)) {
            listeners.put(name, func);
            LOG.info("Added new subscriber : " + name.toString() + " ...");
            return;
        }
        LOG.error("The name " + name.toString() + " already exists...");
    }

    /**
     * Function to remove subscription from.
     *
     * @param name The module to remove subscription.
     */
    @Override
    public void removeSubscription(final ModuleType name) {
        if (listeners.containsKey(name)) {
            listeners.remove(name);
            LOG.info("Removed Subsrciber : " + name.toString() + " ...");
            return;
        }
        LOG.info("The name " + name.toString() + " doesnot exists...");
    }

    /**
     * The Function to call the function mentioned in the subscribers list.
     *
     * @param data the data that is received
     * @param module the module to be called
     */
    public void callSubscriber(final byte[] data, final ModuleType module) {
        LOG.info("Size of message received : " + data.length);
        LOG.info("Calling subscriber : " + module.toString());
        final MessageListener listener = listeners.get(module);
        listener.receiveData(data);
    }
}
