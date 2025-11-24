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
        assertNull(context.getRpc());
        assertNull(context.getSelf());
        assertNull(context.getSelfIP());
        assertNull(context.getMainServerIP());
        assertNull(context.getMeetingSession());
    }

    @Test
    public void canSetRpcField() {
        final Context context = Context.getInstance();
        final RPC rpc = new RPC();
        context.setRpc(rpc);
        assertSame(rpc, context.getRpc());
    }

    @Test
    public void canSetSelfField() {
        final Context context = Context.getInstance();
        final UserProfile profile = new UserProfile(
            "test@example.com",
            "Test User",
            com.swe.core.Meeting.ParticipantRole.STUDENT
        );
        context.setSelf(profile);
        assertSame(profile, context.getSelf());
    }

    @Test
    public void canSetSelfIPField() {
        final Context context = Context.getInstance();
        final ClientNode node = new ClientNode("127.0.0.1", 8080);
        context.setSelfIP(node);
        assertSame(node, context.getSelfIP());
    }

    @Test
    public void canSetMainServerIPField() {
        final Context context = Context.getInstance();
        final ClientNode node = new ClientNode("192.168.1.1", 9090);
        context.setMainServerIP(node);
        assertSame(node, context.getMainServerIP());
    }

    @Test
    public void canSetMeetingSessionField() {
        final Context context = Context.getInstance();
        final MeetingSession session = new MeetingSession(
            "instructor@example.com",
            com.swe.core.Meeting.SessionMode.CLASS
        );
        context.setMeetingSession(session);
        assertSame(session, context.getMeetingSession());
    }
}

