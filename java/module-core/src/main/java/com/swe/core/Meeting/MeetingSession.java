package com.swe.core.Meeting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a meeting created by an instructor.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingSession {
    /** Unique meeting ID. */
    @JsonProperty("meetingId")
    private final String meetingId;

    /** Email of the instructor who created the meeting. */
    @JsonProperty("createdBy")
    private final String createdBy;

    /** Time the meeting was created. */
    @JsonProperty("createdAt")
    private final long createdAt;
    
    /** Session mode: TEST or CLASS. */
    @JsonProperty("SessionMode")
    private final SessionMode sessionMode;

    @JsonProperty("participants")
    private final Map<String, UserProfile> participants = new ConcurrentHashMap<>();

    /**
     * Creates a new meeting with a unique ID.
     *
     * @param createdByParam email of the instructor who created the meeting
     */
    public MeetingSession(final String createdByParam, SessionMode sessionMode) {
        this.sessionMode = sessionMode;
        this.meetingId = UUID.randomUUID().toString(); // generate unique ID
        this.createdBy = createdByParam;
        this.createdAt = System.currentTimeMillis();
    }

    @JsonCreator
    public MeetingSession(
            @JsonProperty("meetingId") String meetingId,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("createdAt") long createdAt,
            @JsonProperty("sessionMode") SessionMode sessionMode,
            @JsonProperty("participants") Map<String, UserProfile> participants){
        this.meetingId = meetingId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.sessionMode = sessionMode;
    }

    public String getMeetingId() {
        return this.meetingId;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public SessionMode getSessionMode() {
        return this.sessionMode;
    }

    public UserProfile getParticipant(String emailId) { return this.participants.get(emailId); }

    public Map<String, UserProfile> getParticipants() {
        return this.participants;
    }

    /**
     * Adds a participant to this session's in-memory list.
     * @param p The participant to add.
     */
    public void addParticipant(UserProfile p) {
        if (p != null && p.getEmail() != null) {
            this.participants.put(p.getEmail(), p);
        }
    }
}