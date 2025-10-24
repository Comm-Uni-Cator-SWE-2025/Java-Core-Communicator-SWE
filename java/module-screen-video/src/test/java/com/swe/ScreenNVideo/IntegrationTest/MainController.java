package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.Networking.AbstractNetworking;
import com.swe.RPC.AbstractRPC;
import com.swe.ScreenNVideo.CaptureManager;
import com.swe.ScreenNVideo.MediaCaptureManager;

import java.io.IOException;
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
    static void main(final String[] args) throws InterruptedException {
        final AbstractNetworking networking = new DummyNetworkingWithQueue();
        final AbstractRPC rpc = new DummyRPC();

        final CaptureManager screenNVideo = new MediaCaptureManager(networking, rpc, 30000);

        Thread handler = null;
        try {
            handler = rpc.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
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
    }
}
