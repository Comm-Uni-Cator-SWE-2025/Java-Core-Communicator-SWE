package com.swe.core.Meeting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.swe.core.ClientNode;

import java.util.Collections;
import java.util.HashMap;
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
    @JsonProperty("sessionMode")
    private final SessionMode sessionMode;

    @JsonProperty("participants")
    private final Map<String, UserProfile> participants = new ConcurrentHashMap<>();

    /**
     * In-memory mapping of participant email to their network coordinates.
     */
    @JsonIgnore
    private final Map<String, ClientNode> participantNodes = new ConcurrentHashMap<>();

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
        if (participants != null) {
            this.participants.putAll(participants);
        }
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

    /**
     * Update or insert the {@link ClientNode} associated with the participant email.
     *
     * @param email participant email
     * @param node client node coordinates
     */
    public void upsertParticipantNode(final String email, final ClientNode node) {
        if (email == null || node == null) {
            return;
        }
        participantNodes.put(email, node);
    }

    /**
     * Retrieve an immutable copy of participant → {@link ClientNode} mappings.
     *
     * @return copy of the mapping
     */
    public Map<String, ClientNode> getParticipantNodes() {
        return Collections.unmodifiableMap(new HashMap<>(participantNodes));
    }

    /**
     * Retrieve a new map keyed by {@link ClientNode} for serialization.
     *
     * @return node → email mapping
     */
    public Map<ClientNode, String> getNodeToEmailMap() {
        final Map<ClientNode, String> mapping = new HashMap<>();
        participantNodes.forEach((email, node) -> {
            if (node != null) {
                mapping.put(node, email);
            }
        });
        return mapping;
    }

    /**
     * Remove a participant's {@link ClientNode} mapping.
     *
     * @param email participant email
     */
    public void removeParticipantNode(final String email) {
        if (email == null) {
            return;
        }
        participantNodes.remove(email);
    }
}