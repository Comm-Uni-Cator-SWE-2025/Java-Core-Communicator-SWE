package com.swe.core.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.swe.core.Meeting.ParticipantRole;
import com.swe.core.Meeting.UserProfile;
import org.junit.Test;

public class ProfilePacketTest {

    @Test
    public void constructorSetsMeetingIdAndProfile() {
        final UserProfile profile = new UserProfile(
            "test@example.com",
            "Test User",
            ParticipantRole.STUDENT
        );
        final ProfilePacket packet = new ProfilePacket("meeting-123", profile);

        assertEquals("meeting-123", packet.getMeetId());
        assertEquals(profile, packet.getProfile());
    }

    @Test
    public void getProfileReturnsCorrectProfile() {
        final UserProfile profile = new UserProfile(
            "user@example.com",
            "User Name",
            ParticipantRole.INSTRUCTOR
        );
        final ProfilePacket packet = new ProfilePacket("meeting-456", profile);

        final UserProfile retrieved = packet.getProfile();
        assertNotNull(retrieved);
        assertEquals("user@example.com", retrieved.getEmail());
        assertEquals("User Name", retrieved.getDisplayName());
        assertEquals(ParticipantRole.INSTRUCTOR, retrieved.getRole());
    }

    @Test
    public void getMeetIdReturnsCorrectId() {
        final ProfilePacket packet = new ProfilePacket("test-meeting-id", null);
        assertEquals("test-meeting-id", packet.getMeetId());
    }

    @Test
    public void constructorWithNullProfile() {
        final ProfilePacket packet = new ProfilePacket("meeting-123", null);
        assertNotNull(packet);
        assertEquals("meeting-123", packet.getMeetId());
        assertNull(packet.getProfile());
    }
}

