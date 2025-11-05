package com.swe.controller.serialize;

import com.swe.controller.Meeting.UserProfile;

public class ProfilePacket {
    String meetingId;
    UserProfile userProfile;

    public ProfilePacket(String meetingId, UserProfile userProfile) {
        this.meetingId = meetingId;
        this.userProfile = userProfile;
    }

    public UserProfile getProfile() {
        return userProfile;
    }

    public String getMeetId() {
        return meetingId;
    }
}
