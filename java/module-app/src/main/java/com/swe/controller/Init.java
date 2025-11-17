package com.swe.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.fasterxml.jackson.core.JsonProcessingException;
// import com.swe.ScreenNVideo.MediaCaptureManager;
import com.swe.core.RPC;
import com.swe.core.Auth.AuthService;
import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.SessionMode;
import com.swe.core.Meeting.UserProfile;
import com.swe.core.serialize.DataSerializer;
import com.swe.core.ClientNode;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

import functionlibrary.CloudFunctionLibrary;

public class Init {
    public static void main(String[] args) throws Exception {
        int portNumber = 6942;

        if (args.length > 0) { 
            String port = args[0];
            portNumber = Integer.parseInt(port);
        }

        RPC rpc = new RPC();
        CloudFunctionLibrary cloud = new CloudFunctionLibrary();
        
        ControllerServices controllerServices = ControllerServices.getInstance();
        controllerServices.rpc = rpc;
        controllerServices.cloud = cloud;


        // Provide RPC somehow here
        NetworkingInterface networking = new SimpleNetworkingAdapter(SimpleNetworking.getSimpleNetwork());
        
        networking.consumeRPC(rpc);

        controllerServices.networking = networking;
        MeetingNetworkingCoordinator.initialize(networking);

        // MediaCaptureManager mediaCaptureManager = new MediaCaptureManager(SimpleNetworking.getSimpleNetwork(), rpc, portNumber);
        // Thread mediaCaptureManagerThread = new Thread(() -> {
        //     try {
        //         mediaCaptureManager.startCapture();
        //     } catch (ExecutionException | InterruptedException e) {
        //         throw new RuntimeException(e);
        //     }
        // });
        // mediaCaptureManagerThread.start();

        addRPCSubscriptions(rpc);

        // We need to get all subscriptions from frontend to also finish before this
        Thread rpcThread = rpc.connect(portNumber);

        rpcThread.join();
        // mediaCaptureManagerThread.join();
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
            System.out.println("[CONTROLLER] Creating meeting");
            final MeetingSession meetingSession = MeetingServices.createMeeting(controllerServices.self, SessionMode.CLASS);
            controllerServices.meetingSession = meetingSession;

            try {
                final ClientNode localClientNode = Utils.getLocalClientNode();
                Utils.setServerClientNode(meetingSession.getMeetingId(), controllerServices.cloud);
                controllerServices.networking.addUser(localClientNode, localClientNode);

                MeetingNetworkingCoordinator.handleMeetingCreated(meetingSession);
            } catch (Exception e) {
                System.out.println("Error initializing networking for meeting host: " + e.getMessage());
                throw new RuntimeException(e);
            }

            try {
                System.out.println("Returning meeting session");
                return DataSerializer.serialize(meetingSession);
            } catch (Exception e) {
                System.out.println("Error serializing meeting session: " + e.getMessage());
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

            System.out.println("Joining meeting with id: " + id);

            try {
                final ClientNode localClientNode = Utils.getLocalClientNode();
                final ClientNode serverClientNode = Utils.getServerClientNode(id, controllerServices.cloud);
                System.out.println("Server client node: " + serverClientNode.toString());

                controllerServices.networking.addUser(localClientNode, serverClientNode);
                MeetingNetworkingCoordinator.handleMeetingJoin(id, serverClientNode);
            } catch (Exception e) {
                System.out.println("Error getting server client node: " + e.getMessage());
                throw new RuntimeException(e);
            }

            return meetId;
        });
    }
}

