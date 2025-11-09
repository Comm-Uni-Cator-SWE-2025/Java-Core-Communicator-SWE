package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.ScreenNVideo.MediaCaptureManager;
import com.swe.networking.AbstractController;
import com.swe.networking.AbstractNetworking;
import com.swe.networking.ClientNode;
import com.swe.networking.Networking;
//import com.swe.networking.SimpleNetworking.AbstractController;
//import com.swe.networking.SimpleNetworking.AbstractNetworking;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.swe.ScreenNVideo.Utils.getSelfIP;

/**
 * Entry point for the screen and video integration test.
 * <p>
 *     The {@code MainController} sets up dummy networking and RPC components,
 *     initializes a {@link MediaCaptureManager}, and starts the screen capture process
 *     in a separate thread for testing purposes.
 * </p>
 */
public class MainController {
    /**
     * Server port for ScreenNVideo.
     */
    static final int SERVERPORT = 40000 ;


    static void main(final String[] args) throws InterruptedException {
//        final SimpleNetworking networking = SimpleNetworking.getSimpleNetwork();
//        final AbstractNetworking networking = Networking.getNetwork();
        final AbstractNetworking networking = new DummyNetworking();

        List<String> allNetworks = new ArrayList<>();
        allNetworks.add("10.32.2.172");
//        allNetworks.add("10.128.15.115");

        // Get IP address as string
        final String ipAddress = getSelfIP();
        final ClientNode deviceNode = new ClientNode(ipAddress, SERVERPORT);
        final ClientNode serverNode = new ClientNode("10.32.1.250", SERVERPORT);
//        networking.addUser(deviceNode, deviceNode);

        final AbstractRPC rpc = new DummyRPC();

        final MediaCaptureManager screenNVideo = new MediaCaptureManager((AbstractNetworking) networking, rpc, SERVERPORT);

//        AbstractController networkingCom = (AbstractController) Networking.getNetwork();
//         networkingCom.addUser(deviceNode, serverNode); // DummyNetworking doesn't need this
//        System.out.println(allNetworks);

        screenNVideo.broadcastJoinMeeting(allNetworks);
        System.out.println("Connection RPC..");


        Thread handler = null;
        try {
            handler = rpc.connect(SERVERPORT);
        } catch (IOException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        final Thread screenNVideoThread = new Thread(() -> {
            try {
                screenNVideo.startCapture();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        screenNVideoThread.start();

        // Start UI
//        Thread uiThread = new Thread(() -> {
//            Application.launch(VideoUI.class, args);
//        });

//        uiThread.join();
        screenNVideoThread.join();
        handler.join();
        
        // Cleanup
//        if (networking instanceof DummyNetworking) {
//            ((DummyNetworking) networking).shutdown();
//        }
    }
}
