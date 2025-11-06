package com.swe.controller;

import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.UserProfile;
import com.swe.core.Meeting.SessionMode;
import com.swe.core.Meeting.ParticipantRole;

public class MeetingServices {

    /**
     * Creates a new meeting (only for instructors) and saves it
     * to the central DataStore.
     *
     * @param userParam logged-in user
     * @return MeetingSession if created, null if user is not an instructor
     */
    public static MeetingSession createMeeting(final UserProfile userParam, SessionMode mode) {
        if (userParam.getRole() != ParticipantRole.INSTRUCTOR) {
            System.err.println("MEETING-SERVICE: Create failed. User is not an instructor.");
            return null;
        }

        final MeetingSession meeting = new MeetingSession(userParam.getEmail(), mode);

        System.out.println("MEETING-SERVICE: Meeting created and saved to DataStore: " + meeting.getMeetingId());
        return meeting;
    }
}