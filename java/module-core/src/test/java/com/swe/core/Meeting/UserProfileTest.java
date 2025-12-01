package com.swe.core.Meeting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserProfileTest {

    @Test
    public void defaultConstructorCreatesEmptyProfile() {
        final UserProfile profile = new UserProfile();
        assertNull(profile.getEmail());
        assertNull(profile.getDisplayName());
        assertNull(profile.getRole());
    }

    @Test
    public void constructorSetsAllFields() {
        final UserProfile profile = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);

        assertEquals("test@example.com", profile.getEmail());
        assertEquals("Test User", profile.getDisplayName());
        assertEquals(ParticipantRole.STUDENT, profile.getRole());
    }

    @Test
    public void settersUpdateFields() {
        final UserProfile profile = new UserProfile();
        profile.setEmail("new@example.com");
        profile.setDisplayName("New Name");
        profile.setRole(ParticipantRole.INSTRUCTOR);

        assertEquals("new@example.com", profile.getEmail());
        assertEquals("New Name", profile.getDisplayName());
        assertEquals(ParticipantRole.INSTRUCTOR, profile.getRole());
    }

    @Test
    public void equalsReturnsTrueForSameFields() {
        final UserProfile profile1 = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);
        final UserProfile profile2 = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);

        assertTrue(profile1.equals(profile2));
        assertEquals(profile1, profile2);
    }

    @Test
    public void equalsReturnsFalseForDifferentFields() {
        final UserProfile profile1 = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);
        final UserProfile profile2 = new UserProfile(
                "other@example.com",
                "Test User",
                ParticipantRole.STUDENT);

        assertFalse(profile1.equals(profile2));
        assertNotEquals(profile1, profile2);
    }

    @Test
    public void equalsReturnsFalseForNull() {
        final UserProfile profile = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);

        assertFalse(profile.equals(null));
    }

    @Test
    public void equalsReturnsTrueForSameInstance() {
        final UserProfile profile = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);

        assertTrue(profile.equals(profile));
    }

    @Test
    public void equalsReturnsFalseForDifferentClass() {
        final UserProfile profile = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);

        assertFalse(profile.equals("not a profile"));
    }

    @Test
    public void hashCodeIsConsistent() {
        final UserProfile profile = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);

        final int hashCode1 = profile.hashCode();
        final int hashCode2 = profile.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void hashCodeIsSameForEqualObjects() {
        final UserProfile profile1 = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);
        final UserProfile profile2 = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);

        assertEquals(profile1.hashCode(), profile2.hashCode());
    }

    @Test
    public void toStringContainsAllFields() {
        final UserProfile profile = new UserProfile(
                "test@example.com",
                "Test User",
                ParticipantRole.STUDENT);

        final String toString = profile.toString();
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("Test User"));
        assertTrue(toString.contains("STUDENT"));
    }

    @Test
    public void equalsHandlesNullFields() {
        final UserProfile profile1 = new UserProfile();
        final UserProfile profile2 = new UserProfile();

        assertTrue(profile1.equals(profile2));
    }

    @Test
    public void equalsHandlesPartialNullFields() {
        final UserProfile profile1 = new UserProfile("test@example.com", null, null);
        final UserProfile profile2 = new UserProfile("test@example.com", null, null);

        assertTrue(profile1.equals(profile2));
    }
}
