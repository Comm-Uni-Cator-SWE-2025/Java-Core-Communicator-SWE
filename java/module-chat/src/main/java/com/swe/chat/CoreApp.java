package com.swe.chat;

import com.swe.RPC.AbstractRPC;
import com.swe.RPC.SocketryServerRPC;
import com.swe.chat.ChatManager;
import com.swe.networking.ClientNode;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

public class CoreApp {
    public static void main(String[] args) {
        try {
            // 1. Create the wrappers (but don't start them yet)
            AbstractRPC rpcServer = new SocketryServerRPC(7000);
            SimpleNetworking network = SimpleNetworking.getSimpleNetwork();

            // 2. Configure Networking
            // (In reality, Controller determines these IPs)
            network.addUser(new ClientNode("127.0.0.1", 1234), new ClientNode("127.0.0.1", 1234));

            // 3. Create Managers (They will SUBSCRIBE now)
            // This pushes "chat:send-message" into the SocketryServerRPC's hashmap
            new ChatManager(network, rpcServer);

            // 4. CONNECT LAST (Starts the server with all subscriptions ready)
            System.out.println("--- CORE STARTING ---");
            Thread rpcThread = rpcServer.connect();

            // Keep main thread alive
            rpcThread.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}