package com.swe.core;

import com.swe.core.Meeting.UserProfile;
import com.swe.core.Meeting.MeetingSession;

public class ControllerServices {
    private static ControllerServices instance;

    public RPC rpc;
    public NetworkingInterface networking;
    public UserProfile self;
    public MeetingSession meetingSession;

    private ControllerServices() {

    }

    public static ControllerServices getInstance() {
        if (instance == null)
            instance = new ControllerServices();
        return instance;
    }
}
