package com.swe.networking.SimpleNetworking;

import java.io.IOException;
import java.util.HashMap;

import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;

public class SimpleNetworking extends Thread implements AbstractController, AbstractNetworking {
    private static SimpleNetworking simpleNetwork;
    private static IUser user;
    private final HashMap<ModuleType, MessageListener> listeners = new HashMap<>();
    private ClientNode serverAddr;
    private Thread receiveThread;
    private boolean exit = false;

    private SimpleNetworking() {
    }

    @Override
    protected void finalize() {
        exit = true;
    }

    public static SimpleNetworking getSimpleNetwork() {
        if (simpleNetwork == null) {
            simpleNetwork = new SimpleNetworking();
        }
        return simpleNetwork;
    }

    @Override
    public void addUser(final ClientNode deviceAddress, final ClientNode mainServerAddress) {
        serverAddr = mainServerAddress;
        if (deviceAddress.hostName().equals(mainServerAddress.hostName())) {
            user = new Server(deviceAddress);
            System.out.println("Server has been instantiated...");
            receiveThread = new Thread(() -> receiveData());
            receiveThread.start();
            return;
        }
        user = new Client(deviceAddress);
        System.out.println("Client has been instantiated...");
        receiveThread = new Thread(() -> receiveData());
        receiveThread.start();
    }

    @Override
    public void sendData(byte[] data, ClientNode[] destIp, ModuleType module, int priority) {
        user.send(data, destIp, serverAddr);
    }

    private void receiveData() {
        while (!exit) {
            try {
                user.receive();
            } catch (IOException e) {
                System.err.println("Error on receiving data...");
            }
        }
        System.out.println("Thread exited without any issues...");
    }

    @Override
    public void subscribe(ModuleType name, MessageListener func) {
        if (!listeners.containsKey(name)) {
            listeners.put(name, func);
            System.out.println("Added a new subscriber...");
            return;
        }
        System.out.println("The name already exist...");
    }

    @Override
    public void removeSubscription(ModuleType name) {
        if (listeners.containsKey(name)) {
            listeners.remove(name);
            return;
        }
        System.out.println("The name doesnot exist...");
    }

    public void callSubscriber(byte[] data, ModuleType module) {
        MessageListener listener = listeners.get(module);
        listener.receiveData(data);
    }
}
