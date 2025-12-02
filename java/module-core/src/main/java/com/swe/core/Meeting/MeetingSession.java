/**
 *  Contributed by Jyoti.
 */

package com.swe.core.Meeting;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.swe.core.ClientNode;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

/**
 * Represents a meeting created by an instructor.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingSession {

    private static final SweLogger LOG = SweLoggerFactory.getLogger("CORE");
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
     * @param sessionModeParam The session mode
     */
    public MeetingSession(final String createdByParam, final SessionMode sessionModeParam) {
        this.sessionMode = sessionModeParam;
        this.meetingId = UUID.randomUUID().toString(); // generate unique ID
        this.createdBy = createdByParam;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Creates a meeting session from JSON.
     *
     * @param meetingIdParam The meeting ID
     * @param createdByParam The creator email
     * @param createdAtParam The creation timestamp
     * @param sessionModeParam The session mode
     * @param participantsParam The participants map
     */
    @JsonCreator
    public MeetingSession(
            @JsonProperty("meetingId") final String meetingIdParam,
            @JsonProperty("createdBy") final String createdByParam,
            @JsonProperty("createdAt") final long createdAtParam,
            @JsonProperty("sessionMode") final SessionMode sessionModeParam,
            @JsonProperty("participants") final Map<ClientNode, UserProfile> participantsParam) {
        this.meetingId = meetingIdParam;
        this.createdBy = createdByParam;
        this.createdAt = createdAtParam;
        this.sessionMode = sessionModeParam;
        if (participantsParam != null) {
            this.participants.putAll(participantsParam);
        }
    }

    /**
     * Gets the meeting ID.
     *
     * @return The meeting ID
     */
    public String getMeetingId() {
        return this.meetingId;
    }

    /**
     * Gets the creator email.
     *
     * @return The creator email
     */
    public String getCreatedBy() {
        return this.createdBy;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp
     */
    public long getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Gets the session mode.
     *
     * @return The session mode
     */
    public SessionMode getSessionMode() {
        return this.sessionMode;
    }

    /**
     * Gets a participant by email.
     *
     * @param emailId The email ID
     * @return The participant profile, or null if not found
     */
    public UserProfile getParticipant(final String emailId) {
        for (UserProfile profile : this.participants.values()) {
            if (profile.getEmail() != null && profile.getEmail().equals(emailId)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Gets a participant by client node.
     *
     * @param node The client node
     * @return The participant profile, or null if not found
     */
    public UserProfile getParticipantByNode(final ClientNode node) {
        return this.participants.get(node);
    }

    /**
     * Gets all participants.
     *
     * @return The participants map
     */
    public Map<ClientNode, UserProfile> getParticipants() {
        return this.participants;
    }

    /**
     * Adds a participant to this session's in-memory list.
     *
     * @param p The participant to add
     * @param node The client node for this participant
     */
    public void addParticipant(final UserProfile p, final ClientNode node) {
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
        LOG.debug("Updated participant node mapping for " + email + " at " + node);
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
            System.out.println("Node is null, cannot remove participant.");
            return;
        }
        System.out.println("Removing participant at node: " + node);
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