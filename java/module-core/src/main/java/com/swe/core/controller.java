package com.swe.core;

import com.swe.core.RPCinterface.AbstractRPC;

public class controller {
    public static void main(String[] args) {
        final AbstractRPC rpc = new RPC();

        SimpleNetworking networking = new SimpleNetworking();

        dummyCloud cloud = new dummyCloud(rpc);

        canvasUI canUI = new canvasUI(rpc);

        controllerServices services = new controllerServices(rpc, networking, cloud);

        Thread handler = null;
        try {
            handler = rpc.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Thread controllerThread = new Thread(services::runController);
        controllerThread.start();

        try {
            controllerThread.join();
            handler.join();
        } catch (Exception e) {

        }
    }
}