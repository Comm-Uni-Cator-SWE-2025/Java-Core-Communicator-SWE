/**
 *  Contributed by Pushti Vasoya.
 */

package com.swe.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class rpcUtilsTest {

    @Test
    public void registerConstantExists() {
        assertNotNull(rpcUtils.REGISTER);
        assertEquals("Controller-Register", rpcUtils.REGISTER);
    }

    @Test
    public void createMeetingConstantExists() {
        assertNotNull(rpcUtils.CREATE_MEETING);
        assertEquals("Controller-CreateMeet", rpcUtils.CREATE_MEETING);
    }

    @Test
    public void joinMeetingConstantExists() {
        assertNotNull(rpcUtils.JOIN_MEETING);
        assertEquals("Controller-JoinMeet", rpcUtils.JOIN_MEETING);
    }
}
