package com.swe.core;

import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.UserProfile;

public class Context {
    private static Context instance;

    public RPC rpc;
    public UserProfile self;
    public ClientNode selfIP;
    public ClientNode mainServerIP;

    public MeetingSession meetingSession;

    private Context() {
    }

    public static Context getInstance() {
        if (instance == null) {
            instance = new Context();
        }
        return instance;
    }
}
