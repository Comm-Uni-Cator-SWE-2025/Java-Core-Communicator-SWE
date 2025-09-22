package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.Networking.AbstractNetworking;
import com.swe.RPC.AbstractRPC;
import com.swe.RPC.RProcedure;
import com.swe.ScreenNVideo.CaptureManager;
import com.swe.ScreenNVideo.MediaCaptureManager;
import com.swe.ScreenNVideo.Serializer.Serializer;
import com.swe.ScreenNVideo.Utils;
import javafx.application.Application;

public class MainController {
    public static void main(String[] args) throws InterruptedException {
        AbstractNetworking networking = new DummyNetworking();
        AbstractRPC rpc = new DummyRPC();

        rpc.subscribe(Utils.UPDATE_UI, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                int[][] image = Serializer.deserializeImage(args);
                VideoUI.displayFrame(image);
                System.out.println("Image Updated");
                return new byte[0];
            }
        });

        CaptureManager screenNVideo = new MediaCaptureManager(networking, rpc, 30000);

        Thread screenNVideoThread = new Thread(screenNVideo::startCapture);

        screenNVideoThread.start();

        // Start UI
        Application.launch(VideoUI.class, args);

        screenNVideoThread.join();
    }
}
