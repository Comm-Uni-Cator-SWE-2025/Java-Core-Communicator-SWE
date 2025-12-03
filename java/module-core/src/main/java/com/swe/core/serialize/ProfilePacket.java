/**
 *  Contributed by Shreya.
 */

package com.swe.core.serialize;

import com.swe.core.Meeting.UserProfile;

public class ProfilePacket {
    String meetingId;
    UserProfile userProfile;

    public ProfilePacket(final String meetingId, final UserProfile userProfile) {
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
