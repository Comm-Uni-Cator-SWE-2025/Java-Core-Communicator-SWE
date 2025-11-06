package com.swe.networking.SimpleNetworking;

import java.io.IOException;
import java.util.HashMap;

import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;

/**
 * The main module of the simple networking class.
 */
public final class SimpleNetworking
        implements AbstractController, AbstractNetworking {
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
    private final HashMap<ModuleType, MessageListener> listeners =
        new HashMap<>();
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

    private SimpleNetworking() {
    }

    /**
     * Function to call when we want to stop networking module.
     */
    @Override
    public void closeNetworking() {
        exit = true;
        user.closeUser();
        System.out.println("Closing Networking module...");
    }

    /**
     * Function to return the singleton simple Network object.
     *
     * @return the singleton object
     */
    public static SimpleNetworking getSimpleNetwork() {
        if (simpleNetwork == null) {
            System.out.println("Instantiated SimpleNetworking module...");
            simpleNetwork = new SimpleNetworking();
            return simpleNetwork;
        }
        System.out.println("Already instantiated SimpleNetworking module...");
        return simpleNetwork;
    }

    /**
     * Function to add Ip address details about the current user.
     * Must be called only once
     *
     * @param deviceAddress     the IP address of the device
     * @param mainServerAddress the IP address of the mainServer
     */
    @Override
    public void addUser(final ClientNode deviceAddress,
            final ClientNode mainServerAddress) {
        serverAddr = mainServerAddress;
        if (deviceAddress.equals(mainServerAddress)) {
            user = new Server(deviceAddress);
            System.out.println("Server has been instantiated...");
        } else {
            user = new Client(deviceAddress);
            System.out.println("Client has been instantiated...");
        }
        receiveThread = new Thread(() -> receiveData());
        receiveThread.start();
    }

    /**
     * Function to send the given data to a list of destinations.
     *
     * @param data     the data to be sent
     * @param destIp   the list of destination to whom the data is sent
     * @param module   the destination module id
     * @param priority the priority of the send message
     */
    @Override
    public void sendData(final byte[] data, final ClientNode[] destIp,
            final ModuleType module, final int priority) {
        user.send(data, destIp, serverAddr, module);
    }

    /**
     * Function to receiveData until the thread stops.
     */
    private void receiveData() {
        while (!exit) {
            try {
                user.receive();
            } catch (IOException e) {
                System.err.println("Error on receiving data...");
            }
        }
        System.out.println("Thread exited...");
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
            System.out.println("Added a new subscriber...");
            return;
        }
        System.out.println("The name already exist...");
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
            return;
        }
        System.out.println("The name doesnot exist...");
    }

    /**
     * The Function to call the function mentioned in the subscribers list.
     *
     * @param data   the data that is received
     * @param module the module to be called
     */
    public void callSubscriber(final byte[] data, final ModuleType module) {
        final MessageListener listener = listeners.get(module);
        listener.receiveData(data);
    }
}
