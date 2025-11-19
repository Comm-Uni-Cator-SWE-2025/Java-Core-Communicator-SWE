package com.swe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.serializer.AnnouncePacket;
import com.swe.controller.serializer.JoinAckPacket;
import com.swe.controller.serializer.JoinPacket;
import com.swe.controller.serializer.MeetingPacketType;
import com.swe.core.ClientNode;
import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.SessionMode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.core.serialize.DataSerializer;
import com.swe.networking.ModuleType;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Coordinates networking responsibilities for meeting lifecycle messages.
 */
public final class MeetingNetworkingCoordinator {
    private static final String TAG = "[MEETING-NETWORK]";
    private static final MeetingPacketType[] PACKET_TYPES = MeetingPacketType.values();

    private MeetingNetworkingCoordinator() {
    }

    /**
     * Registers listeners for controller networking packets.
     *
     * @param networking networking adapter
     */
    public static void initialize(final NetworkingInterface networking) {
        networking.subscribe(ModuleType.CONTROLLER, MeetingNetworkingCoordinator::handleIncomingPacket);
        System.out.println(TAG + " Registered controller networking handlers");
    }

    /**
     * Handles the networking side-effects once a meeting is created.
     *
     * @param meeting the meeting session that was created
     */
    public static void handleMeetingCreated(final MeetingSession meeting) {
        final ControllerServices services = ControllerServices.getInstance();
        final ClientNode localNode = getLocalClientNode();
        meeting.upsertParticipantNode(services.context.self.getEmail(), localNode);
        System.out.println(TAG + " Server registered local node " + localNode + " for meeting " + meeting.getMeetingId());
    }

    /**
     * Handles networking bookkeeping when a user joins a meeting.
     *
     * @param meetingId target meeting identifier
     * @param serverNode server coordinates
     */
    public static void handleMeetingJoin(final String meetingId, final ClientNode serverNode) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession session = ensureMeetingSession(services, meetingId);
        final ClientNode localNode = getLocalClientNode();

        if (services.context.self != null) {
            session.upsertParticipantNode(services.context.self.getEmail(), localNode);
        }

        sendJoinPacket(serverNode, localNode);
    }

    private static void handleIncomingPacket(final byte[] data) {
        if (data == null || data.length == 0) {
            System.out.println(TAG + " Received empty packet");
            return;
        }

        final byte ordinal = data[0];
        if (ordinal < 0 || ordinal >= PACKET_TYPES.length) {
            System.out.println(TAG + " Unknown packet ordinal: " + ordinal);
            return;
        }
        final MeetingPacketType type = PACKET_TYPES[ordinal];

        System.out.println(TAG + " Handling packet type: " + type);

        switch (type) {
            case JOIN -> handleJoinPacket(data);
            case JOINACK -> handleJoinAckPacket(data);
            case ANNOUNCE -> handleAnnouncePacket(data);
            default -> System.out.println(TAG + " Unhandled packet type: " + type);
        }

        final ControllerServices services = ControllerServices.getInstance();
        try {
            services.context.rpc.call("core/setIpToMailMap", DataSerializer.serialize(services.context.meetingSession.getNodeToEmailMap())).get();
        } catch (Exception e) {
            System.out.println("Error calling setIpToMailMap: " + e.getMessage());
        }
        System.out.println("Total participants: " + services.context.meetingSession.getNodeToEmailMap());
    }

    private static void handleJoinPacket(final byte[] data) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession meeting = services.context.meetingSession;
        if (meeting == null) {
            System.out.println(TAG + " No active meeting to process JOIN packet");
            return;
        }

        // Only the meeting creator should process JOIN packets
        if (services.context.self == null || !meeting.getCreatedBy().equals(services.context.self.getEmail())) {
            System.out.println(TAG + " Ignoring JOIN packet because this controller is not the server");
            return;
        }

        final JoinPacket packet = JoinPacket.deserialize(data);
        meeting.upsertParticipantNode(packet.getEmail(), packet.getClientNode());
        System.out.println(TAG + " Received JOIN from " + packet.getEmail() + " at " + packet.getClientNode());

        final JoinAckPacket ackPacket = new JoinAckPacket(meeting.getNodeToEmailMap());
        sendBytes(ackPacket.serialize(), new ClientNode[]{packet.getClientNode()});
    }

    private static void handleJoinAckPacket(final byte[] data) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession meeting = ensureMeetingSession(services, null);

        final JoinAckPacket joinAckPacket = JoinAckPacket.deserialize(data);
        final Map<ClientNode, String> nodeToEmailMap = joinAckPacket.getNodeToEmailMap();
        
        for (Map.Entry<ClientNode, String> entry : nodeToEmailMap.entrySet()) {
            meeting.upsertParticipantNode(entry.getValue(), entry.getKey());
        }

        System.out.println(TAG + " Received JOINACK with " + nodeToEmailMap.size() + " mappings");

        final ClientNode localNode = getLocalClientNode();
        final List<ClientNode> recipients = new ArrayList<>();
        for (ClientNode node : nodeToEmailMap.keySet()) {
            if (!node.equals(localNode)) {
                recipients.add(node);
            }
        }

        broadcastAnnounce(recipients);
    }

    private static void handleAnnouncePacket(final byte[] data) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession meeting = ensureMeetingSession(services, null);
        final AnnouncePacket announcePacket = AnnouncePacket.deserialize(data);

        meeting.upsertParticipantNode(announcePacket.getEmail(), announcePacket.getClientNode());
        System.out.println(TAG + " Processed ANNOUNCE from " + announcePacket.getEmail()
                + " at " + announcePacket.getClientNode());
    }

    private static void sendJoinPacket(final ClientNode serverNode, final ClientNode localNode) {
        final ControllerServices services = ControllerServices.getInstance();
        if (services.networking == null || services.context.self == null) {
            System.out.println(TAG + " Cannot send JOIN: networking or self profile not initialized");
            return;
        }

        final JoinPacket joinPacket = new JoinPacket(services.context.self.getEmail(), localNode);
        sendBytes(joinPacket.serialize(), new ClientNode[]{serverNode});
    }

    private static void broadcastAnnounce(final Collection<ClientNode> recipients) {
        final ControllerServices services = ControllerServices.getInstance();
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        if (services.networking == null || services.context.self == null) {
            System.out.println(TAG + " Cannot send ANNOUNCE: networking or self profile not initialized");
            return;
        }

        final ClientNode localNode = getLocalClientNode();
        final AnnouncePacket packet = new AnnouncePacket(services.context.self.getEmail(), localNode);
        sendBytes(packet.serialize(), recipients.toArray(new ClientNode[0]));
    }

    private static void sendBytes(final byte[] payload, final ClientNode[] destination) {
        final ControllerServices services = ControllerServices.getInstance();
        if (destination == null || destination.length == 0 || services.networking == null) {
            System.out.println(TAG + " Cannot send: invalid destination or networking not available");
            return;
        }
        services.networking.sendData(payload, destination, ModuleType.CONTROLLER, 0);
    }

    private static MeetingSession ensureMeetingSession(final ControllerServices services, final String meetingId) {
        if (services.context.meetingSession != null) {
            return services.context.meetingSession;
        }

        final String id = meetingId != null ? meetingId : UUID.randomUUID().toString();
        services.context.meetingSession = new MeetingSession(
                id,
                services.context.self != null ? services.context.self.getEmail() : "",
                System.currentTimeMillis(),
                SessionMode.CLASS,
                null
        );
        System.out.println(TAG + " Created placeholder MeetingSession for networking with ID " + id);
        return services.context.meetingSession;
    }

    private static ClientNode getLocalClientNode() {
        try {
            return Utils.getLocalClientNode();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to determine local client node", e);
        }
    }
}

