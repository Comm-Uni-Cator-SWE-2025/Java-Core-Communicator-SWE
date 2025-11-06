package com.swe.controller.Meeting;

import com.swe.controller.AbstractController;
import com.swe.controller.ClientNode;

public class MeetingServices {

    private final AbstractController networkController;

    /**
     * Constructor using Dependency Injection.q
     * We "inject" the database (DataStore) and the network (AbstractController)
     * so that this class can use them.
     */
    public MeetingServices( AbstractController networkController) {
        this.networkController = networkController;
    }

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

    /**
     * Joins a meeting by first checking the DataStore, and then
     * notifying the network controller.
     *
     * @param hostNode       The network address of the host (primary server)
     * @param deviceNode     The network address of the user who is joining
     * @return true if the meeting was found and the user was added, false otherwise
     */
    public boolean joinMeeting(ClientNode hostNode, ClientNode deviceNode) {

        networkController.addUser(deviceNode, hostNode);
        return true;
    }
}