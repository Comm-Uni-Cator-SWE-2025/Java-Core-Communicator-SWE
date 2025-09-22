package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.Networking.AbstractNetworking;
import com.swe.RPC.AbstractRPC;
import com.swe.RPC.RProcedure;
import com.swe.ScreenNVideo.CaptureManager;
import com.swe.ScreenNVideo.MediaCaptureManager;
import com.swe.ScreenNVideo.Serializer.Serializer;
import com.swe.ScreenNVideo.Utils;
import javafx.application.Application;

import java.util.concurrent.ExecutionException;

public class MainController {
    static void main(String[] args) throws InterruptedException {
        AbstractNetworking networking = new DummyNetworkingWithQueue();
        AbstractRPC rpc = new DummyRPC();

        rpc.subscribe(Utils.UPDATE_UI, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                int[][] image = Serializer.deserializeImage(args);
                VideoUI.displayFrame(image);
                return new byte[0];
            }
        });

        CaptureManager screenNVideo = new MediaCaptureManager(networking, rpc, 30000);

        Thread screenNVideoThread = new Thread(() -> {
            try {
                screenNVideo.startCapture();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        screenNVideoThread.start();

        // Start UI
        Application.launch(VideoUI.class, args);

        screenNVideoThread.join();
    }
}
