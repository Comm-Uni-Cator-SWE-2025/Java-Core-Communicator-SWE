package com.swe.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.UserProfile;
import org.junit.Test;

public class ContextTest {

    @Test
    public void getInstanceReturnsSameInstance() {
        final Context instance1 = Context.getInstance();
        final Context instance2 = Context.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void getInstanceReturnsNotNull() {
        final Context instance = Context.getInstance();
        assertNotNull(instance);
    }

    @Test
    public void instanceHasNullFieldsInitially() {
        final Context context = Context.getInstance();
        assertNull(context.rpc);
        assertNull(context.self);
        assertNull(context.selfIP);
        assertNull(context.mainServerIP);
        assertNull(context.meetingSession);
    }

    @Test
    public void canSetRpcField() {
        final Context context = Context.getInstance();
        final RPC rpc = new RPC();
        context.rpc = rpc;
        assertSame(rpc, context.rpc);
    }

    @Test
    public void canSetSelfField() {
        final Context context = Context.getInstance();
        final UserProfile profile = new UserProfile(
            "test@example.com",
            "Test User",
            com.swe.core.Meeting.ParticipantRole.STUDENT
        );
        context.self = profile;
        assertSame(profile, context.self);
    }

    @Test
    public void canSetSelfIPField() {
        final Context context = Context.getInstance();
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        context.selfIP = node;
        assertSame(node, context.selfIP);
    }

    @Test
    public void canSetMainServerIPField() {
        final Context context = Context.getInstance();
        final ClientNode node = new ClientNode("192.168.1.1", 9090);
        context.mainServerIP = node;
        assertSame(node, context.mainServerIP);
    }

    @Test
    public void canSetMeetingSessionField() {
        final Context context = Context.getInstance();
        final MeetingSession session = new MeetingSession(
            "instructor@example.com",
            com.swe.core.Meeting.SessionMode.CLASS
        );
        context.meetingSession = session;
        assertSame(session, context.meetingSession);
    }
}

