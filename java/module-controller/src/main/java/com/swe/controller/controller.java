package com.swe.controller;

import com.swe.controller.RPCinterface.AbstractRPC;

public class controller {
    public static void main(String[] args) {
        AbstractRPC rpc = new RPC();

        SimpleNetworking networking = new SimpleNetworking();

        controllerServices services = new controllerServices(rpc, networking);

        dummyCloud cloud = new dummyCloud(rpc);

        canvasUI canUI = new canvasUI(rpc);

        Thread controllerThread = new Thread(services::runController);

        controllerThread.start();

        Thread handler = null;
        try {
            handler = rpc.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        rpc.call("cloudMethod", new byte[] {5,6,7,8,9,1,3,5,6}).thenAccept(response -> {
            System.out.println("Received response of length: " + response.length);
        });

        handler.start();
    }
}