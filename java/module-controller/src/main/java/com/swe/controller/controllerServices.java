package com.swe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.Auth.AuthService;
import com.swe.controller.Meeting.*;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.controller.serialize.ProfilePacket;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class controllerServices {

    AbstractRPC rpc;

    SimpleNetworking networking;

    dummyCloud cloud;

    UserProfile RegisteredUser;

    MeetingSession meetingSession;

    controllerServices(AbstractRPC rpc, SimpleNetworking networking, dummyCloud cloud) {
        this.rpc = rpc;
        this.networking = networking;
        this.cloud = cloud;
        InitializeSubscriber();
    }

    private void joinMeetThroughNetworking(String meetId){
        ClientNode host = new ClientNode(cloud.getIpAddr(meetId), cloud.getPort(meetId));
        ClientNode client = new ClientNode(cloud.getIpAddr(meetId), cloud.getPort(meetId));

        networking.addUser(client, host);

//        networking.sendData();
    }

    private void InitializeSubscriber() {
        System.out.println("InitializeSubscriber");

        rpc.subscribe(rpcUtils.REGISTER, (byte[] userData) -> {
            try {
                ParticipantRole role = DataSerializer.deserialize(userData, ParticipantRole.class);
                RegisteredUser = AuthService.register();
                System.out.println("Registered user with emailId: " + RegisteredUser.getEmail());
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }

            try {
                return DataSerializer.serialize(RegisteredUser);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        rpc.subscribe(rpcUtils.CREATE_MEETING, (byte[] meetMode) -> {
            try {
                meetingSession = MeetingServices.createMeeting(RegisteredUser, DataSerializer.deserialize(meetMode, SessionMode.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            try {
                return DataSerializer.serialize(meetingSession);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        rpc.subscribe(rpcUtils.JOIN_MEETING, (byte[] meetId) -> {
            String id;
            try {
                id = DataSerializer.deserialize(meetId, String.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            joinMeetThroughNetworking(id);
            return meetId;
        });

        networking.subscribe(ModuleType.CONTROLLER, (byte[] data) -> {
            ProfilePacket profile = DataSerializer.deserialize(data, ProfilePacket.class);
            if (profile != null) {
                meetingSession.addParticipant(profile.getProfile());
            }
        });
    }

    public void runController() {
        System.out.println("runController");

        while (true) {}
    }
}

