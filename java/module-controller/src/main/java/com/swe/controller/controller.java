package com.swe.controller;

import com.swe.RPC.AbstractRPC;
import com.swe.ScreenNVideo.CaptureManager;
import com.swe.ScreenNVideo.MediaCaptureManager;
import com.swe.networking.ClientNode;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class controller {
    final static int SERVERPORT = 40000;
    final static int PINGPORT = 10002;

    public static void main(String[] args) {
        final SimpleNetworking networking = SimpleNetworking.getSimpleNetwork();

        String ownIpAddress = "127.0.0.1"; // Default value

        try {
            ownIpAddress = getOwnIpAddress();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Own IP Address: " + ownIpAddress);

        ClientNode client = new ClientNode(ownIpAddress, SERVERPORT);
        ClientNode host = new ClientNode(ownIpAddress, SERVERPORT);

        final RPC rpc = new RPC();

        networking.addUser(client, host);

        final ControllerServices controllerServices = new ControllerServices(rpc);

        final Thread controllerServiceThread = new Thread(() -> {
            try {
                controllerServices.startService();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        controllerServiceThread.start();

        System.out.println("RPC connected");
//
        final CaptureManager screenNVideo = new MediaCaptureManager(networking, rpc, SERVERPORT);

        final Thread screenShareThread = new Thread(() -> {
            try {
                screenNVideo.startCapture();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("Screen share started");

        screenShareThread.start();

        Thread rpcThread = null;
        try {
            rpcThread = rpc.connect();
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
//
        try {
            screenShareThread.join();
            controllerServiceThread.join();
            rpcThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Screen share ended");

    }

    private static String getOwnIpAddress() throws IOException, InterruptedException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), PINGPORT);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}