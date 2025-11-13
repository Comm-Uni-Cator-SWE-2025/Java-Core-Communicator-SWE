package com.swe.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.ScreenNVideo.MediaCaptureManager;
import com.swe.core.RPC;
import com.swe.core.Auth.AuthService;
import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.SessionMode;
import com.swe.core.Meeting.UserProfile;
import com.swe.core.serialize.DataSerializer;
import com.swe.networking.ClientNode;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

public class Init {
    public static void main(String[] args) throws Exception {
        int portNumber = 6942;

        if (args.length > 0) { 
            String port = args[0];
            portNumber = Integer.parseInt(port);
        }

        RPC rpc = new RPC();
        
        ControllerServices controllerServices = ControllerServices.getInstance();
        controllerServices.rpc = rpc;

        // Provide RPC somehow here
        NetworkingInterface networking = new SimpleNetworkingAdapter(SimpleNetworking.getSimpleNetwork());
        
        networking.consumeRPC(rpc);

        controllerServices.networking = networking;

        MediaCaptureManager mediaCaptureManager = new MediaCaptureManager(SimpleNetworking.getSimpleNetwork(), rpc, portNumber);
        Thread mediaCaptureManagerThread = new Thread(() -> {
            try {
                mediaCaptureManager.startCapture();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        mediaCaptureManagerThread.start();

        addRPCSubscriptions(rpc);

        // We need to get all subscriptions from frontend to also finish before this
        Thread rpcThread = rpc.connect(portNumber);

        rpcThread.join();
        mediaCaptureManagerThread.join();
    }

    private static void addRPCSubscriptions(RPC rpc) {
        ControllerServices controllerServices = ControllerServices.getInstance();

        rpc.subscribe("core/register", (byte[] userData) -> {
            System.out.println("Registering user");
            UserProfile RegisteredUser = null;
            try {
                RegisteredUser = AuthService.register();
                System.out.println("Registered user with emailId: " + RegisteredUser.getEmail());
            } catch (GeneralSecurityException | IOException e) {
                // throw new RuntimeException(e);
                System.out.println("Error registering user: " + e.getMessage());
                return new byte[0];
            }

            controllerServices.self = RegisteredUser;
            
            try {
                return DataSerializer.serialize(RegisteredUser);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        rpc.subscribe("core/createMeeting", (byte[] meetMode) -> {
            MeetingSession meetingSession = null;
            try {
                meetingSession = MeetingServices.createMeeting(controllerServices.self, DataSerializer.deserialize(meetMode, SessionMode.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            try {
                ClientNode localClientNode = Utils.getLocalClientNode();
                ClientNode serverClientNode = Utils.getServerClientNode(meetingSession.getMeetingId());
                controllerServices.networking.addUser(localClientNode, serverClientNode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                return DataSerializer.serialize(meetingSession);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        rpc.subscribe("core/joinMeeting", (byte[] meetId) -> {
            String id;
            try {
                id = DataSerializer.deserialize(meetId, String.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            try {
                ClientNode localClientNode = Utils.getLocalClientNode();
                ClientNode serverClientNode = Utils.getServerClientNode(id);
                controllerServices.networking.addUser(localClientNode, serverClientNode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return meetId;
        });
    }
}

