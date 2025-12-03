/**
 *  Contributed by Shreya.
 */

package com.swe.core.serialize;

import com.swe.core.Meeting.UserProfile;

/**
 * Packet that carries the meeting information for a profile.
 */
public class ProfilePacket {
    /**
     * Meeting identifier associated with the profile.
     */
    private final String meetingId;
    /**
     * The serialized user profile.
     */
    private final UserProfile userProfile;

    /**
     * Creates a profile packet for a meeting.
     *
     * @param meetingIdParam the meeting identifier
     * @param userProfileParam the user profile data
     */
    public ProfilePacket(final String meetingIdParam, final UserProfile userProfileParam) {
        this.meetingId = meetingIdParam;
        this.userProfile = userProfileParam;
    }

    /**
     * Gets the profile contained in the packet.
     *
     * @return the user profile
     */
    public UserProfile getProfile() {
        return userProfile;
    }

    /**
     * Gets the meeting identifier associated with the packet.
     *
     * @return the meeting identifier
     */
    public String getMeetId() {
        return meetingId;
    }
}
