package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.RPC.AbstractRPC;
import com.swe.ScreenNVideo.MediaCaptureManager;
import com.swe.networking.SimpleNetworking.AbstractNetworking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    static final int SERVERPORT = 40000;


    static void main(final String[] args) throws InterruptedException {
        final AbstractNetworking networking = new DummyNetworkingWithQueue();

        List<String> allNetworks = new ArrayList<>();
//        allNetworks.add("");

        // Get IP address as string
//        final String ipAddress = getSelfIP();

        final AbstractRPC rpc = new DummyRPC();

        final MediaCaptureManager screenNVideo = new MediaCaptureManager(networking, rpc, SERVERPORT);

//        System.out.println(allNetworks);

        screenNVideo.broadcastJoinMeeting(allNetworks);
        System.out.println("Connection RPC..");

        Thread handler = null;
        try {
            handler = rpc.connect();
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
//        networking.closeNetworking();
    }
}
