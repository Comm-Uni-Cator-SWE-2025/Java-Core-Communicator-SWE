package com.swe.controller.Auth;

import com.swe.controller.Meeting.MeetingSession;
import com.swe.controller.Meeting.UserProfile;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe, Singleton DataStore to act as the single source of truth
 * for the application's in-memory database.
 *
 * This class prevents duplicate users (by email) and meetings (by ID).
 */
public class DataStore {

    // 1. The single, private, static instance of the DataStore
    private static final DataStore instance = new DataStore();

    // 2. The data maps are now non-static and private.
    // We use ConcurrentHashMap to make them thread-safe.
    private final Map<String, MeetingSession> meetings;

    /**
     * 3. The constructor is private, so no one else can create a new instance.
     */
    private DataStore() {
        this.meetings = new ConcurrentHashMap<>();
    }

    /**
     * 4. The public, static method to get the one and only instance.
     * All other modules will call DataStore.getInstance() to get this.
     */
    public static DataStore getInstance() {
        return instance;
    }

    // --- User Methods ---

    /**
     * Adds a new user to the database, checking for duplicate emails.
     *
     * @param user The user to add.
     * @return true if the user was added, false if a user with that email already exists.
     */
    public boolean addUser(final UserProfile user, final String meetId) {
        if (user == null || user.getEmail() == null || meetId == null) {
            return false;
        }

        Optional<MeetingSession> meetingOpt = findMeetingById(meetId);

        if (meetingOpt.isEmpty()) {
            return false;
        }
        MeetingSession meeting = meetingOpt.get();

        if (meeting.getParticipants().containsKey(user.getUserId())) {
            return false;
        }

        meeting.addParticipant(user);
        return true;
    }

    /**
     * Finds a user by their email.
     * @param email The email to search for.
     * @return An Optional containing the UserProfile if found.
     */
    public Optional<UserProfile> findUserByEmail(final String email, final String meetId) {
        Optional<MeetingSession> meetingOpt = findMeetingById(meetId);

        if (meetingOpt.isEmpty()) {
            return Optional.empty();
        }
        MeetingSession meeting = meetingOpt.get();

        for (UserProfile user : meeting.getParticipants().values()) {
            if (email.equals(user.getEmail())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // --- Meeting Methods ---

    /**
     * Adds a new meeting to the database, checking for duplicate IDs.
     *
     * @param meeting The meeting to add.
     * @return true if the meeting was added, false if a meeting with that ID already exists.
     */
    public boolean addMeeting(final MeetingSession meeting) {
        if (meeting == null || meeting.getMeetingId() == null) {
            return false;
        }
        // Duplicate check for meetings
        MeetingSession existingMeeting = meetings.putIfAbsent(meeting.getMeetingId(), meeting);
        return (existingMeeting == null);
    }

    /**
     * Finds a meeting by its ID.
     * @param meetingId The ID to search for.
     * @return An Optional containing the MeetingSession if found.
     */
    public Optional<MeetingSession> findMeetingById(final String meetingId) {
        return Optional.ofNullable(meetings.get(meetingId));
    }
}