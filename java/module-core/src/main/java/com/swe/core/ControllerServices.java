package com.swe.core;

import com.swe.core.Meeting.UserProfile;
import com.swe.networking.SimpleNetworking.AbstractNetworking;

public class ControllerServices {
    private static ControllerServices instance;

    public RPC rpc;
    public AbstractNetworking networking;
    public UserProfile self;

    private ControllerServices() {

    }

    public static ControllerServices getInstance() {
        if (instance == null)
            instance = new ControllerServices();
        return instance;
    }
}
