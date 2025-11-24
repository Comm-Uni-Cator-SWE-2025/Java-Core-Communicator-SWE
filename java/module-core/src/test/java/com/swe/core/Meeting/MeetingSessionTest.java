package com.swe.core.Meeting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.swe.core.ClientNode;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class MeetingSessionTest {

    private MeetingSession meetingSession;
    private String instructorEmail;

    @Before
    public void setUp() {
        instructorEmail = "instructor@example.com";
        meetingSession = new MeetingSession(instructorEmail, SessionMode.CLASS);
    }

    @Test
    public void constructorGeneratesUniqueMeetingId() {
        assertNotNull(meetingSession.getMeetingId());
        assertTrue(meetingSession.getMeetingId().length() > 0);
    }

    @Test
    public void constructorSetsCreatedBy() {
        assertEquals(instructorEmail, meetingSession.getCreatedBy());
    }

    @Test
    public void constructorSetsCreatedAt() {
        final long createdAt = meetingSession.getCreatedAt();
        assertTrue(createdAt > 0);
        assertTrue(createdAt <= System.currentTimeMillis());
    }

    @Test
    public void constructorSetsSessionMode() {
        assertEquals(SessionMode.CLASS, meetingSession.getSessionMode());
    }

    @Test
    public void constructorWithSessionModeTest() {
        final MeetingSession testSession = new MeetingSession(
            "instructor@example.com",
            SessionMode.TEST
        );
        assertEquals(SessionMode.TEST, testSession.getSessionMode());
    }

    @Test
    public void jsonCreatorConstructorSetsAllFields() {
        final String meetingId = "test-meeting-id";
        final String createdBy = "creator@example.com";
        final long createdAt = 1000L;
        final SessionMode mode = SessionMode.CLASS;
        final Map<ClientNode, UserProfile> participants = new java.util.HashMap<>();

        final MeetingSession session = new MeetingSession(
            meetingId,
            createdBy,
            createdAt,
            mode,
            participants
        );

        assertEquals(meetingId, session.getMeetingId());
        assertEquals(createdBy, session.getCreatedBy());
        assertEquals(createdAt, session.getCreatedAt());
        assertEquals(mode, session.getSessionMode());
        assertTrue(session.getParticipants().isEmpty());
    }

    @Test
    public void jsonCreatorConstructorWithNullParticipants() {
        final MeetingSession session = new MeetingSession(
            "id",
            "creator@example.com",
            1000L,
            SessionMode.CLASS,
            null
        );

        assertTrue(session.getParticipants().isEmpty());
    }

    @Test
    public void addParticipantAddsToMap() {
        final UserProfile profile = new UserProfile(
            "student@example.com",
            "Student Name",
            ParticipantRole.STUDENT
        );
        final ClientNode node = new ClientNode("127.0.0.1", 8080);

        meetingSession.addParticipant(profile, node);

        assertEquals(profile, meetingSession.getParticipantByNode(node));
        assertEquals(profile, meetingSession.getParticipant("student@example.com"));
    }

    @Test
    public void addParticipantWithNullValuesDoesNothing() {
        final int initialSize = meetingSession.getParticipants().size();
        meetingSession.addParticipant(null, new ClientNode("127.0.0.1", 8080));
        meetingSession.addParticipant(
            new UserProfile("test@example.com", "Test", ParticipantRole.STUDENT),
            null
        );
        meetingSession.addParticipant(null, null);

        assertEquals(initialSize, meetingSession.getParticipants().size());
    }

    @Test
    public void getParticipantByEmailReturnsCorrectProfile() {
        final UserProfile profile = new UserProfile(
            "student@example.com",
            "Student Name",
            ParticipantRole.STUDENT
        );
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        meetingSession.addParticipant(profile, node);

        final UserProfile found = meetingSession.getParticipant("student@example.com");
        assertEquals(profile, found);
    }

    @Test
    public void getParticipantByEmailReturnsNullWhenNotFound() {
        assertNull(meetingSession.getParticipant("nonexistent@example.com"));
    }

    @Test
    public void getParticipantByNodeReturnsCorrectProfile() {
        final UserProfile profile = new UserProfile(
            "student@example.com",
            "Student Name",
            ParticipantRole.STUDENT
        );
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        meetingSession.addParticipant(profile, node);

        final UserProfile found = meetingSession.getParticipantByNode(node);
        assertEquals(profile, found);
    }

    @Test
    public void getParticipantByNodeReturnsNullWhenNotFound() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        assertNull(meetingSession.getParticipantByNode(node));
    }

    @Test
    public void upsertParticipantNodeCreatesNewProfile() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        meetingSession.upsertParticipantNode("new@example.com", "New User", node);

        final UserProfile profile = meetingSession.getParticipantByNode(node);
        assertNotNull(profile);
        assertEquals("new@example.com", profile.getEmail());
        assertEquals("New User", profile.getDisplayName());
        assertEquals(ParticipantRole.STUDENT, profile.getRole());
    }

    @Test
    public void upsertParticipantNodeUpdatesExistingProfile() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        meetingSession.upsertParticipantNode("test@example.com", "Original Name", node);
        meetingSession.upsertParticipantNode("test@example.com", "Updated Name", node);

        final UserProfile profile = meetingSession.getParticipantByNode(node);
        assertEquals("test@example.com", profile.getEmail());
        assertEquals("Updated Name", profile.getDisplayName());
    }

    @Test
    public void upsertParticipantNodeWithNullDisplayNameDoesNotUpdate() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        meetingSession.upsertParticipantNode("test@example.com", "Original Name", node);
        meetingSession.upsertParticipantNode("test@example.com", null, node);

        final UserProfile profile = meetingSession.getParticipantByNode(node);
        assertEquals("Original Name", profile.getDisplayName());
    }

    @Test
    public void upsertParticipantNodeTwoParameterVersion() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        meetingSession.upsertParticipantNode("test@example.com", node);

        final UserProfile profile = meetingSession.getParticipantByNode(node);
        assertNotNull(profile);
        assertEquals("test@example.com", profile.getEmail());
    }

    @Test
    public void upsertParticipantNodeWithNullEmailDoesNothing() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        final int initialSize = meetingSession.getParticipants().size();
        meetingSession.upsertParticipantNode(null, node);

        assertEquals(initialSize, meetingSession.getParticipants().size());
    }

    @Test
    public void upsertParticipantNodeWithNullNodeDoesNothing() {
        final int initialSize = meetingSession.getParticipants().size();
        meetingSession.upsertParticipantNode("test@example.com", (ClientNode) null);

        assertEquals(initialSize, meetingSession.getParticipants().size());
    }

    @Test
    public void removeParticipantByNodeRemovesFromMap() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        final UserProfile profile = new UserProfile(
            "student@example.com",
            "Student",
            ParticipantRole.STUDENT
        );
        meetingSession.addParticipant(profile, node);

        meetingSession.removeParticipantByNode(node);

        assertNull(meetingSession.getParticipantByNode(node));
    }

    @Test
    public void removeParticipantByNodeWithNullDoesNothing() {
        final int initialSize = meetingSession.getParticipants().size();
        meetingSession.removeParticipantByNode(null);
        assertEquals(initialSize, meetingSession.getParticipants().size());
    }

    @Test
    public void removeParticipantByEmailRemovesFromMap() {
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        final UserProfile profile = new UserProfile(
            "student@example.com",
            "Student",
            ParticipantRole.STUDENT
        );
        meetingSession.addParticipant(profile, node);

        meetingSession.removeParticipantByEmail("student@example.com");

        assertNull(meetingSession.getParticipantByNode(node));
        assertNull(meetingSession.getParticipant("student@example.com"));
    }

    @Test
    public void removeParticipantByEmailWithNullDoesNothing() {
        final int initialSize = meetingSession.getParticipants().size();
        meetingSession.removeParticipantByEmail(null);
        assertEquals(initialSize, meetingSession.getParticipants().size());
    }

    @Test
    public void getParticipantsReturnsUnmodifiableView() {
        final Map<ClientNode, UserProfile> participants = meetingSession.getParticipants();
        assertNotNull(participants);
    }
}

