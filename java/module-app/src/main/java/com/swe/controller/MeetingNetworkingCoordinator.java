package com.swe.controller;

import com.swe.controller.serializer.IamPacket;
import com.swe.controller.serializer.JoinAckPacket;
import com.swe.controller.serializer.MeetingPacketType;
import com.swe.core.ClientNode;
import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.SessionMode;
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
        if (services.context.self != null) {
            meeting.upsertParticipantNode(services.context.self.getEmail(), 
                                         services.context.self.getDisplayName(), 
                                         localNode);
        }
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
            session.upsertParticipantNode(services.context.self.getEmail(), 
                                         services.context.self.getDisplayName(), 
                                         localNode);
        }

        sendIamPacket(serverNode, localNode);
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
            case IAM -> handleIamPacket(data);
            case JOINACK -> handleJoinAckPacket(data);
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

    private static void handleIamPacket(final byte[] data) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession meeting = services.context.meetingSession;
        if (meeting == null) {
            System.out.println(TAG + " No active meeting to process IAM packet");
            return;
        }

        final IamPacket packet = IamPacket.deserialize(data);
        final boolean isServer = services.context.self != null && 
                                  meeting.getCreatedBy().equals(services.context.self.getEmail());

        if (isServer) {
            // Server receives IAM as a join request
            if (meeting.getNodeToEmailMap().containsKey(packet.getClientNode())) {
                System.out.println(TAG + " User already in meeting");
                return;
            }
            meeting.upsertParticipantNode(packet.getEmail(), packet.getDisplayName(), packet.getClientNode());
            System.out.println(TAG + " Received IAM (join request) from " + packet.getEmail() + " (" + packet.getDisplayName() + ") at " + packet.getClientNode());

            final JoinAckPacket ackPacket = new JoinAckPacket(meeting.getNodeToEmailMap(), meeting.getEmailToDisplayNameMap());
            sendBytes(ackPacket.serialize(), new ClientNode[]{packet.getClientNode()});
        } else {
            // Peer receives IAM as an announcement
            meeting.upsertParticipantNode(packet.getEmail(), packet.getDisplayName(), packet.getClientNode());
            System.out.println(TAG + " Received IAM (announcement) from " + packet.getEmail() + " (" + packet.getDisplayName() + ") at " + packet.getClientNode());
        }
    }

    private static void handleJoinAckPacket(final byte[] data) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession meeting = ensureMeetingSession(services, null);

        final JoinAckPacket joinAckPacket = JoinAckPacket.deserialize(data);
        final Map<ClientNode, String> nodeToEmailMap = joinAckPacket.getNodeToEmailMap();
        final Map<String, String> emailToDisplayNameMap = joinAckPacket.getEmailToDisplayNameMap();
        
        for (Map.Entry<ClientNode, String> entry : nodeToEmailMap.entrySet()) {
            final String email = entry.getValue();
            final String displayName = emailToDisplayNameMap.getOrDefault(email, "");
            meeting.upsertParticipantNode(email, displayName, entry.getKey());
        }

        System.out.println(TAG + " Received JOINACK with " + nodeToEmailMap.size() + " mappings");

        final ClientNode localNode = getLocalClientNode();
        final List<ClientNode> recipients = new ArrayList<>();
        for (ClientNode node : nodeToEmailMap.keySet()) {
            if (!node.equals(localNode)) {
                recipients.add(node);
            }
        }

        broadcastIam(recipients);
    }

    private static void sendIamPacket(final ClientNode serverNode, final ClientNode localNode) {
        final ControllerServices services = ControllerServices.getInstance();
        if (services.networking == null || services.context.self == null) {
            System.out.println(TAG + " Cannot send IAM: networking or self profile not initialized");
            return;
        }

        final IamPacket iamPacket = new IamPacket(services.context.self.getEmail(), 
                                                  services.context.self.getDisplayName(), 
                                                  localNode);
        sendBytes(iamPacket.serialize(), new ClientNode[]{serverNode});
    }

    private static void broadcastIam(final Collection<ClientNode> recipients) {
        final ControllerServices services = ControllerServices.getInstance();
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        if (services.networking == null || services.context.self == null) {
            System.out.println(TAG + " Cannot send IAM: networking or self profile not initialized");
            return;
        }

        final ClientNode localNode = getLocalClientNode();
        final IamPacket packet = new IamPacket(services.context.self.getEmail(), 
                                              services.context.self.getDisplayName(), 
                                              localNode);
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

