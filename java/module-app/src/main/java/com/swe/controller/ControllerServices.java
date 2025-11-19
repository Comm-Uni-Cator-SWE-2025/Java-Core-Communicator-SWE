package com.swe.controller;

import functionlibrary.CloudFunctionLibrary;
import com.swe.core.Meeting.UserProfile;
import com.swe.core.Meeting.MeetingSession;
import com.swe.core.RPC;

public class ControllerServices {
    private static ControllerServices instance;

    public RPC rpc;
    public NetworkingInterface networking;
    public UserProfile self;
    public MeetingSession meetingSession;
    public CloudFunctionLibrary cloud;

    private ControllerServices() {

    }

    public static ControllerServices getInstance() {
        if (instance == null)
            instance = new ControllerServices();
        return instance;
    }
}




