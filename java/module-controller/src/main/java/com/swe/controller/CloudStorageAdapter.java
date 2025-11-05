package com.swe.controller;

import com.swe.controller.Meeting.MeetingSession;

import java.util.Optional;

/**
 * Implements communication with the remote Cloud Storage module's API.
 */
public class CloudStorageAdapter {

    public Optional<MeetingSession> getMeetingDetailsById(String meetingId) {
        return Optional.empty();
    }

    public Optional<MeetingSession> getOngoingMeeting() {
        return Optional.empty();
    }
}

