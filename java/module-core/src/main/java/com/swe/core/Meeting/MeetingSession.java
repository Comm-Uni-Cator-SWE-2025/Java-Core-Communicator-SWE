/**
 *  Contributed by Jyoti.
 */

package com.swe.core.Meeting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.swe.core.ClientNode;

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
    private final Map<ClientNode, UserProfile> participants = new ConcurrentHashMap<>();

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
            @JsonProperty("participants") Map<ClientNode, UserProfile> participants){
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

    public UserProfile getParticipant(String emailId) {
        for (UserProfile profile : this.participants.values()) {
            if (profile.getEmail() != null && profile.getEmail().equals(emailId)) {
                return profile;
            }
        }
        return null;
    }

    public UserProfile getParticipantByNode(ClientNode node) {
        return this.participants.get(node);
    }

    public Map<ClientNode, UserProfile> getParticipants() {
        return this.participants;
    }

    /**
     * Adds a participant to this session's in-memory list.
     * @param p The participant to add.
     * @param node The client node for this participant.
     */
    public void addParticipant(UserProfile p, ClientNode node) {
        if (p != null && node != null) {
            this.participants.put(node, p);
        }
    }

    /**
     * Update or insert the ClientNode to email mapping and email to displayName mapping.
     *
     * @param email participant email
     * @param displayName participant display name
     * @param node client node coordinates
     */
    public void upsertParticipantNode(final String email, final String displayName, final ClientNode node) {
        System.out.println("New ip " + email + " " + node);
        if (email == null || node == null) {
            return;
        }
        
        UserProfile profile = participants.get(node);
        if (profile == null) {
            profile = new UserProfile(email, displayName, ParticipantRole.STUDENT);
            participants.put(node, profile);
        } else {
            profile.setEmail(email);
            if (displayName != null) {
                profile.setDisplayName(displayName);
            }
        }
    }

    /**
     * Update or insert the ClientNode to email mapping (backward compatibility).
     * Note: This method does not update displayName. Use the 3-parameter version instead.
     *
     * @param email participant email
     * @param node client node coordinates
     */
    public void upsertParticipantNode(final String email, final ClientNode node) {
        upsertParticipantNode(email, null, node);
    }

    /**
     * Remove a participant's mapping by ClientNode.
     *
     * @param node client node coordinates
     */
    public void removeParticipantByNode(final ClientNode node) {
        if (node == null) {
            return;
        }
        participants.remove(node);
    }

    /**
     * Remove a participant's mapping by email.
     * Note: This requires iterating through the map since it's keyed by ClientNode.
     *
     * @param email participant email
     */
    public void removeParticipantByEmail(final String email) {
        if (email == null) {
            return;
        }
        participants.entrySet().removeIf(entry -> 
            entry.getValue().getEmail() != null && email.equals(entry.getValue().getEmail()));
    }
}