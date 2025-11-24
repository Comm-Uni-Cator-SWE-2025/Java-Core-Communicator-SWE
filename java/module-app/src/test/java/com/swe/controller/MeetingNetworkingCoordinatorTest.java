package com.swe.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swe.controller.serializer.IamPacket;
import com.swe.controller.serializer.MeetingPacketType;
import com.swe.core.ClientNode;
import com.swe.core.Context;
import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.ParticipantRole;
import com.swe.core.Meeting.SessionMode;
import com.swe.core.Meeting.UserProfile;
import com.swe.core.RPC;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.MessageListener;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class MeetingNetworkingCoordinatorTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        final Field instanceField = ControllerServices.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        final Context context = Context.getInstance();
        context.rpc = null;
        context.self = null;
        context.selfIP = null;
        context.mainServerIP = null;
        context.meetingSession = null;
    }

    @AfterEach
    void cleanupContext() {
        final Context context = Context.getInstance();
        context.rpc = null;
        context.self = null;
        context.meetingSession = null;
    }new MeetingSession(
                id,
                creatorEmail,
                System.currentTimeMillis(),
                SessionMode.CLASS,
                null
        )

    @Test
    void initializeRegistersControllerListener() {
        final RecordingNetworking networking = new RecordingNetworking();

        MeetingNetworkingCoordinator.initialize(networking);

        assertEquals(ModuleType.CONTROLLER, networking.lastSubscriptionModule);
        assertNotNull(networking.lastListener, "Listener should be registered");
    }

    @Test
    void handleMeetingCreatedRegistersLocalParticipant() {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession meeting = new MeetingSession("owner@example.com", SessionMode.CLASS);
        services.context.self = new UserProfile("self@example.com", "Self", ParticipantRole.STUDENT);

        try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
            utilities.when(Utils::getLocalClientNode).thenReturn(new ClientNode("127.0.0.1", 5000));

            MeetingNetworkingCoordinator.handleMeetingCreated(meeting);
        }

        assertNotNull(meeting.getParticipant("self@example.com"), "Self participant should be registered");
    }

    @Test
    void handleMeetingJoinSendsIamPacket() {
        final ControllerServices services = ControllerServices.getInstance();
        final RecordingNetworking networking = new RecordingNetworking();
        services.networking = networking;
        services.context.self = new UserProfile("self@example.com", "Self", ParticipantRole.STUDENT);
        services.context.rpc = mock(RPC.class);

        try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
            utilities.when(Utils::getLocalClientNode).thenReturn(new ClientNode("127.0.0.2", 6001));

            MeetingNetworkingCoordinator.handleMeetingJoin("meeting-123", new ClientNode("127.0.0.3", 7000));
        }

        assertNotNull(services.context.meetingSession, "Meeting session should be initialized");
        assertEquals(ModuleType.CONTROLLER, networking.lastSendModule);
        assertNotNull(networking.lastPayload, "IAM payload should be sent");
        assertEquals(MeetingPacketType.IAM.ordinal(), networking.lastPayload[0]);
        assertEquals("self@example.com",
                services.context.meetingSession.getParticipant("self@example.com").getEmail());
    }

    @Test
    void incomingIamPacketAddsParticipantAndUpdatesCore() throws Exception {
        final ControllerServices services = ControllerServices.getInstance();
        final RecordingNetworking networking = new RecordingNetworking();
        services.networking = networking;
        final RPC rpc = mock(RPC.class);
        when(rpc.call(eq("core/updateParticipants"), any())).thenReturn(CompletableFuture.completedFuture(new byte[0]));
        services.context.rpc = rpc;
        services.context.meetingSession = new MeetingSession("owner@example.com", SessionMode.CLASS);
        services.context.self = new UserProfile("another@example.com", "Another", ParticipantRole.STUDENT);

        MeetingNetworkingCoordinator.initialize(networking);
        final MessageListener listener = networking.lastListener;
        final byte[] packetData = new IamPacket(
                "newuser@example.com",
                "New User",
                new ClientNode("10.0.0.5", 8123)).serialize();

        listener.receiveData(packetData);

        assertNotNull(
                services.context.meetingSession.getParticipant("newuser@example.com"),
                "New participant should be added");
        verify(rpc).call(eq("core/updateParticipants"), any());
    }

    private static final class RecordingNetworking implements NetworkingInterface {
        ModuleType lastSubscriptionModule;
        MessageListener lastListener;
        ModuleType lastSendModule;
        byte[] lastPayload;

        @Override
        public void sendData(byte[] data, ClientNode[] destIp, ModuleType module, int priority) {
            lastPayload = data;
            lastSendModule = module;
        }

        @Override
        public void subscribe(ModuleType name, MessageListener function) {
            lastSubscriptionModule = name;
            lastListener = function;
        }

        @Override
        public void removeSubscription(ModuleType name) {
            // not needed
        }

        @Override
        public void addUser(ClientNode deviceAddress, ClientNode mainServerAddress) {
            // not needed
        }

        @Override
        public void closeNetworking() {
            // not needed
        }

        @Override
        public void consumeRPC(AbstractRPC rpc) {
            // not needed
        }
    }

}
