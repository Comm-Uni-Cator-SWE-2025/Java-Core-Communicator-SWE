package com.swe.core.Meeting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ParticipantRoleTest {

    @Test
    public void enumValuesExist() {
        assertNotNull(ParticipantRole.INSTRUCTOR);
        assertNotNull(ParticipantRole.STUDENT);
        assertNotNull(ParticipantRole.GUEST);
    }

    @Test
    public void valueOfWorks() {
        assertEquals(ParticipantRole.INSTRUCTOR, ParticipantRole.valueOf("INSTRUCTOR"));
        assertEquals(ParticipantRole.STUDENT, ParticipantRole.valueOf("STUDENT"));
        assertEquals(ParticipantRole.GUEST, ParticipantRole.valueOf("GUEST"));
    }

    @Test
    public void valuesReturnsAllRoles() {
        final ParticipantRole[] roles = ParticipantRole.values();
        assertEquals(3, roles.length);
    }
}
