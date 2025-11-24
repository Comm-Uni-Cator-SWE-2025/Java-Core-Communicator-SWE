package com.swe.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.ParticipantRole;
import com.swe.core.Meeting.SessionMode;
import com.swe.core.Meeting.UserProfile;
import org.junit.jupiter.api.Test;

class MeetingServicesTest {

    @Test
    void createMeetingSetsCreatorAndReturnsSession() {
        final UserProfile user = new UserProfile("instructor@example.com", "Instructor", ParticipantRole.STUDENT);

        final MeetingSession meeting = MeetingServices.createMeeting(user, SessionMode.CLASS);

        assertEquals(ParticipantRole.INSTRUCTOR, user.getRole(), "Creator should be promoted to instructor");
        assertNotNull(meeting, "Meeting session should be created");
        assertEquals("instructor@example.com", meeting.getCreatedBy(), "Meeting creator should match user email");
        assertEquals(SessionMode.CLASS, meeting.getSessionMode(), "Session mode should be preserved");
    }
}
