package com.swe.core.Meeting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SessionModeTest {

    @Test
    public void enumValuesExist() {
        assertNotNull(SessionMode.TEST);
        assertNotNull(SessionMode.CLASS);
    }

    @Test
    public void valueOfWorks() {
        assertEquals(SessionMode.TEST, SessionMode.valueOf("TEST"));
        assertEquals(SessionMode.CLASS, SessionMode.valueOf("CLASS"));
    }

    @Test
    public void valuesReturnsAllModes() {
        final SessionMode[] modes = SessionMode.values();
        assertEquals(2, modes.length);
    }
}
