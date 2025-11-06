package com.swe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.Auth.DataStore;
import com.swe.controller.Meeting.MeetingSession;
import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.controller.serialize.ProfilePacket;

public class controllerServicesOld {

    AbstractRPC rpc;

    SimpleNetworking networking;

    UserProfile RegisteredUser;

    // Get the SINGLE instance of the DataStore
    private final DataStore dataStore = DataStore.getInstance();

    controllerServicesOld(AbstractRPC rpc, SimpleNetworking networking) {
        this.rpc = rpc;
        this.networking = networking;
    }

    public void updateFrontendProfile(ProfilePacket userData) throws JsonProcessingException {
        rpc.call("getProfile", DataSerializer.serializeParticipantsList(userData)).thenAccept(response -> {
            // Handle the response if needed
        });
    }

    public void runController() {

        rpc.subscribe("Register", (byte[] userData) -> {
            boolean success = false;
            try {
                RegisteredUser = DataSerializer.deserializeParticipantsList(userData, UserProfile.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            try {
                return DataSerializer.serializeParticipantsList(RegisteredUser);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        rpc.subscribe("JoinSession", (byte[] sessionId) -> {
            try {
                dataStore.addMeeting(DataSerializer.deserializeParticipantsList(sessionId, MeetingSession.class));

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            try {
                return DataSerializer.serializeParticipantsList("Session Registered");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });


        networking.subscribe(ModuleType.CONTROLLER, (byte[] data) -> {
            ProfilePacket profile = DataSerializer.deserializeParticipantsList(data, ProfilePacket.class);
            if(profile != null) {
                dataStore.addUser(profile.getProfile(), profile.getMeetId());
                updateFrontendProfile(profile);
            }
        });

        networking.sendData(new byte[] {1, 2, 3, 4, 5}, null, ModuleType.CONTROLLER, 1);

       while (true) {}
    }
}

