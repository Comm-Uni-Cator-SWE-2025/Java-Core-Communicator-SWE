package com.swe.controller.Meeting;

import com.swe.controller.AbstractController;
import com.swe.controller.Auth.DataStore;
import com.swe.controller.ClientNode;

public class MeetingServices {

    private final DataStore dataStore;
    private final AbstractController networkController;

    /**
     * Constructor using Dependency Injection.
     * We "inject" the database (DataStore) and the network (AbstractController)
     * so that this class can use them.
     */
    public MeetingServices(DataStore dataStore, AbstractController networkController) {
        this.dataStore = dataStore;
        this.networkController = networkController;
    }

    /**
     * Creates a new meeting (only for instructors) and saves it
     * to the central DataStore.
     *
     * @param userParam logged-in user
     * @return MeetingSession if created, null if user is not an instructor
     */
    public MeetingSession createMeeting(final UserProfile userParam) {
        if (userParam.getRole() != ParticipantRole.INSTRUCTOR) {
            System.err.println("MEETING-SERVICE: Create failed. User is not an instructor.");
            return null;
        }

        final MeetingSession meeting = new MeetingSession(userParam.getEmail(), SessionMode.CLASS);

        if (dataStore.addMeeting(meeting)) {
            System.out.println("MEETING-SERVICE: Meeting created and saved to DataStore: " + meeting.getMeetingId());
            return meeting;
        } else {
            System.err.println("MEETING-SERVICE: Failed to save meeting (duplicate ID?).");
            return null;
        }
    }

    /**
     * Joins a meeting by first checking the DataStore, and then
     * notifying the network controller.
     *
     * @param userParam      logged-in user
     * @param meetingIdParam meeting ID
     * @param hostNode       The network address of the host (primary server)
     * @param deviceNode     The network address of the user who is joining
     * @return true if the meeting was found and the user was added, false otherwise
     */
    public boolean joinMeeting(final UserProfile userParam, final String meetingIdParam,
                               ClientNode hostNode, ClientNode deviceNode) {

        if (dataStore.findMeetingById(meetingIdParam).isPresent()) {
            System.out.println("MEETING-SERVICE: Meeting " + meetingIdParam + " found in DataStore.");

            networkController.addUser(deviceNode, hostNode);

            return true;
        } else {
            System.err.println("MEETING-SERVICE: Join failed. Meeting ID not found: " + meetingIdParam);
            return false;
        }
    }
}