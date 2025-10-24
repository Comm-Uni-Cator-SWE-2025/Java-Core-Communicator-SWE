package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.RPC.AbstractRPC;
import com.swe.ScreenNVideo.CaptureManager;
import com.swe.ScreenNVideo.MediaCaptureManager;
import com.swe.networking.ClientNode;
import com.swe.networking.SimpleNetworking.AbstractNetworking;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class MainController {
    private static String getSelfIP() {
        // Get IP address as string
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    static void main(String[] args) throws InterruptedException {
        SimpleNetworking networking =  SimpleNetworking.getSimpleNetwork();

        // Get IP address as string
        String ipAddress = getSelfIP();
        ClientNode deviceNode = new ClientNode(ipAddress, 40000);
        ClientNode serverNode = new ClientNode(ipAddress, 40000);

        AbstractRPC rpc = new DummyRPC();

        CaptureManager screenNVideo = new MediaCaptureManager(networking, rpc, 40000);

        networking.addUser(deviceNode,serverNode);
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
