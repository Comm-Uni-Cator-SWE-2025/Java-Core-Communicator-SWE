package com.swe.ScreenNVideo.IntegrationTest;

import com.socketry.SocketryServer;
import com.swe.Networking.AbstractNetworking;
import com.swe.RPC.AbstractRPC;
import com.swe.RPC.RProcedure;
import com.swe.ScreenNVideo.CaptureManager;
import com.swe.ScreenNVideo.MediaCaptureManager;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainController {
    static void main(String[] args) throws InterruptedException {
        AbstractNetworking networking = new DummyNetworkingWithQueue();
        AbstractRPC rpc = new DummyRPC();

        CaptureManager screenNVideo = new MediaCaptureManager(networking, rpc, 30000);

        Thread handler = null;
        try {
            handler = rpc.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }



        Thread screenNVideoThread = new Thread(() -> {
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
