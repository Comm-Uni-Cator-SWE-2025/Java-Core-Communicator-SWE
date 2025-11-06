package com.swe.controller;

import com.swe.core.ControllerServices;
import com.swe.core.RPC;
import com.swe.networking.ClientNode;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

public class Init {
    public static void main(String[] args) throws Exception {
        String port = args[0];
        int portNumber = Integer.parseInt(port);
        RPC rpc = new RPC();
        
        ControllerServices controllerServices = ControllerServices.getInstance();
        controllerServices.rpc = rpc;

        // Provide RPC somehow here
        SimpleNetworking networking = SimpleNetworking.getSimpleNetwork();

        controllerServices.networking = networking;

        // We need to get all subscriptions from frontend to also finish before this
        Thread rpcThread = rpc.connect(portNumber);

        ClientNode localClientNode = Utils.getLocalClientNode();
        ClientNode serverClientNode = Utils.getServerClientNode();
        networking.addUser(localClientNode, serverClientNode);

        rpcThread.join();
    }
}

