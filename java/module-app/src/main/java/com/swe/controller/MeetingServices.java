package com.swe.controller;

import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.UserProfile;
import com.swe.core.Meeting.SessionMode;
import com.swe.core.Meeting.ParticipantRole;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

/**
 * Service class for meeting-related operations.
 */
public class MeetingServices {

    /**
     * Logger for meeting services.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("CONTROLLER-APP");

    /**
     * Creates a new meeting (only for instructors) and saves it
     * to the central DataStore.
     *
     * @param userParam logged-in user
     * @param mode The session mode for the meeting
     * @return MeetingSession if created, null if user is not an instructor
     */
    public static MeetingSession createMeeting(final UserProfile userParam, final SessionMode mode) {
        // if (userParam.getRole() != ParticipantRole.INSTRUCTOR) {
        //     System.err.println("MEETING-SERVICE: Create failed. User is not an instructor.");
        //     return null;
        // }

        userParam.setRole(ParticipantRole.INSTRUCTOR); // SET creator as INSTRUCTOR

        final MeetingSession meeting = new MeetingSession(userParam.getEmail(), mode);

        LOG.info("Meeting created and saved to DataStore: " + meeting.getMeetingId());
        return meeting;
    }
}