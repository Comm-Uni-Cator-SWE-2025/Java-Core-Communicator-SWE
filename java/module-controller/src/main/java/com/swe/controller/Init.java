package com.swe.controller;
import com.swe.networking.SimpleNetworking.*;

public class Init {
    public static void main(String[] args) throws Exception {
        String port = args[0];
        int portNumber = Integer.parseInt(port);
        RPC rpc = new RPC();
        
        ControllerServices controllerServices = ControllerServices.getInstance();

        // Provide ControllerServices somehow here
        AbstractNetworking networking = SimpleNetworking.getSimpleNetwork();

        controllerServices.networking = networking;
    }
}
