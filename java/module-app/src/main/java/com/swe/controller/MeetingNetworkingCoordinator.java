/*
 * -----------------------------------------------------------------------------
 *  File: MeetingNetworkCoordinator.java
 *  Owner: Kaushik Rawat
 *  Roll Number : 112201015
 *  Module : Controller/App
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.controller;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.serializer.ILeavePacket;
import com.swe.controller.serializer.IamPacket;
import com.swe.controller.serializer.JoinAckPacket;
import com.swe.controller.serializer.MeetingPacketType;
import com.swe.core.ClientNode;
import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.SessionMode;
import com.swe.core.Meeting.UserProfile;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;
import com.swe.core.serialize.DataSerializer;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;

/**
 * Coordinates networking responsibilities for meeting lifecycle messages.
 */
public final class MeetingNetworkingCoordinator {
    /**
     * Logger for meeting networking coordinator.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("CONTROLLER-APP");

    /**
     * Array of packet types for quick lookup.
     */
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
        LOG.info("Registered controller networking handlers");
    }

    /**
     * Handles the networking side-effects once a meeting is created.
     *
     * @param meeting the meeting session that was created
     */
    public static void handleMeetingCreated(final MeetingSession meeting) {
        final ControllerServices services = ControllerServices.getInstance();
        final ClientNode localNode = getLocalClientNode();
        if (services.getContext().getSelf() != null) {
            meeting.upsertParticipantNode(services.getContext().getSelf().getEmail(),
                    services.getContext().getSelf().getDisplayName(),
                    localNode);
        }
        LOG.info("Server registered local node " + localNode + " for meeting " + meeting.getMeetingId());
    }

    /**
     * Handles networking bookkeeping when a user joins a meeting.
     *
     * @param meetingId  target meeting identifier
     * @param serverNode server coordinates
     */
    public static void handleMeetingJoin(final String meetingId, final ClientNode serverNode) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession session = ensureMeetingSession(services, meetingId);
        final ClientNode localNode = getLocalClientNode();

        if (services.getContext().getSelf() != null) {
            session.upsertParticipantNode(services.getContext().getSelf().getEmail(),
                    services.getContext().getSelf().getDisplayName(),
                    localNode);
        }

        System.out.println("handeling meeting join and leave");
        sendIamPacket(serverNode, localNode);
    }

    private static void handleIncomingPacket(final byte[] data) {
        if (data == null || data.length == 0) {
            LOG.warn("Received empty packet");
            return;
        }

        // final byte ordinal = data[0];

        LOG.debug("Raw packet bytes: " + java.util.Arrays.toString(data));
        final int ordinal = Byte.toUnsignedInt(data[0]);
        LOG.debug("First byte (unsigned ordinal): " + ordinal + " (as signed: " + data[0] + ")");
        if (ordinal < 0 || ordinal >= PACKET_TYPES.length) {
            LOG.warn("Unknown packet ordinal: " + ordinal);
            return;
        }
        final MeetingPacketType type = PACKET_TYPES[ordinal];

        LOG.debug("Handling packet type: " + type);

        switch (type) {
            case LEAVE -> handleLeavePacket(data);
            case IAM -> handleIamPacket(data);
            case JOINACK -> handleJoinAckPacket(data);
            default -> LOG.warn("Unhandled packet type: " + type);
        }

        final ControllerServices services = ControllerServices.getInstance();
        try {
            services.getContext().getRpc().call("core/updateParticipants",
                    DataSerializer.serialize(services.getContext().getMeetingSession().getParticipants())).get();
        } catch (Exception e) {
            LOG.error("Error calling core/updateParticipants", e);
        }
        LOG.debug("Total participants: " + services.getContext().getMeetingSession().getParticipants());
    }

    private static void handleIamPacket(final byte[] data) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession meeting = services.getContext().getMeetingSession();
        if (meeting == null) {
            LOG.warn("No active meeting to process IAM packet");
            return;
        }

        final IamPacket packet = IamPacket.deserialize(data);
        final boolean isServer = services.getContext().getSelf() != null
                && meeting.getCreatedBy().equals(services.getContext().getSelf().getEmail());

        if (isServer) {
            // Server receives IAM as a join request
            System.out.println("handleIncoming Iam server");
            if (meeting.getParticipants().containsKey(packet.getClientNode())) {
                LOG.info("User wants to leave meeting having mail: " + packet.getEmail());
                return;
            }
            meeting.upsertParticipantNode(packet.getEmail(), packet.getDisplayName(), packet.getClientNode());
            LOG.info("Received IAM (join request) from " + packet.getEmail()
                    + " (" + packet.getDisplayName() + ") at " + packet.getClientNode());

            // Convert participants map to the two separate maps for JoinAckPacket
            final Map<ClientNode, String> nodeToEmailMap = new HashMap<>();
            final Map<String, String> emailToDisplayNameMap = new HashMap<>();
            for (Map.Entry<ClientNode, UserProfile> entry : meeting.getParticipants().entrySet()) {
                final UserProfile profile = entry.getValue();
                if (profile.getEmail() != null) {
                    nodeToEmailMap.put(entry.getKey(), profile.getEmail());
                    if (profile.getDisplayName() != null) {
                        emailToDisplayNameMap.put(profile.getEmail(), profile.getDisplayName());
                    }
                }
            }

            final JoinAckPacket ackPacket = new JoinAckPacket(nodeToEmailMap, emailToDisplayNameMap);
            sendBytes(ackPacket.serialize(), new ClientNode[] { packet.getClientNode() });
        } else {
            System.out.println("handleIncoming Iam client");
            if (meeting.getParticipants().containsKey(packet.getClientNode())) {
                return;
            }
            // Peer receives IAM as an announcement
            meeting.upsertParticipantNode(packet.getEmail(), packet.getDisplayName(), packet.getClientNode());
            LOG.info("Received IAM (announcement) from " + packet.getEmail()
                    + " (" + packet.getDisplayName() + ") at " + packet.getClientNode());
        }
    }

    private static void handleJoinAckPacket(final byte[] data) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession meeting = ensureMeetingSession(services, null);

        final JoinAckPacket joinAckPacket = JoinAckPacket.deserialize(data);
        final Map<ClientNode, String> nodeToEmailMap = joinAckPacket.getNodeToEmailMap();
        final Map<String, String> emailToDisplayNameMap = joinAckPacket.getEmailToDisplayNameMap();

        // Convert the two maps to participants map
        for (Map.Entry<ClientNode, String> entry : nodeToEmailMap.entrySet()) {
            final String email = entry.getValue();
            final String displayName = emailToDisplayNameMap.getOrDefault(email, "");
            meeting.upsertParticipantNode(email, displayName, entry.getKey());
        }

        LOG.info("Received JOINACK with " + nodeToEmailMap.size() + " participant mappings");

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
        if (services.getNetworking() == null || services.getContext().getSelf() == null) {
            LOG.warn("Cannot send IAM: networking or self profile not initialized");
            return;
        }

        System.out.println("sending IAM packet");
        final IamPacket iamPacket = new IamPacket(services.getContext().getSelf().getEmail(),
                services.getContext().getSelf().getDisplayName(),
                localNode);
        sendBytes(iamPacket.serialize(), new ClientNode[] { serverNode });
    }

    private static void broadcastIam(final Collection<ClientNode> recipients) {
        final ControllerServices services = ControllerServices.getInstance();
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        if (services.getNetworking() == null || services.getContext().getSelf() == null) {
            LOG.warn("Cannot broadcast IAM: networking or self profile not initialized");
            return;
        }

        final ClientNode localNode = getLocalClientNode();
        final IamPacket packet = new IamPacket(services.getContext().getSelf().getEmail(),
                services.getContext().getSelf().getDisplayName(),
                localNode);
        sendBytes(packet.serialize(), recipients.toArray(new ClientNode[0]));
    }

    private static void sendBytes(final byte[] payload, final ClientNode[] destination) {
        final ControllerServices services = ControllerServices.getInstance();
        if (destination == null || destination.length == 0 || services.getNetworking() == null) {
            LOG.warn("Cannot send packet: invalid destination or networking not available");
            return;
        }
        services.getNetworking().sendData(payload, destination, ModuleType.CONTROLLER, 0);
    }

    private static MeetingSession ensureMeetingSession(final ControllerServices services, final String meetingId) {
        if (services.getContext().getMeetingSession() != null) {
            return services.getContext().getMeetingSession();
        }

        final String id;
        if (meetingId != null) {
            id = meetingId;
        } else {
            id = UUID.randomUUID().toString();
        }
        final String creatorEmail;
        if (services.getContext().getSelf() != null) {
            creatorEmail = services.getContext().getSelf().getEmail();
        } else {
            creatorEmail = "";
        }
        final MeetingSession newSession = new MeetingSession(
                id,
                creatorEmail,
                System.currentTimeMillis(),
                SessionMode.CLASS,
                null);
        services.getContext().setMeetingSession(newSession);
        LOG.info("Created placeholder MeetingSession for networking with ID " + id);
        return services.getContext().getMeetingSession();
    }

    private static ClientNode getLocalClientNode() {
        try {
            return Utils.getLocalClientNode();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to determine local client node", e);
        }
    }

    // Leave meet functions

    public static void handleMeetingLeave(final String meetingId, final ClientNode serverNode) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession session = ensureMeetingSession(services, meetingId);
        final ClientNode localNode = getLocalClientNode();

        if (services.getContext().getSelf() != null) {
            session.removeParticipantByEmail(services.getContext().getSelf().getEmail());
        }

        System.out.println("handeling meeting leave");
        sendILeavePacket(serverNode, localNode);
    }

    private static void sendILeavePacket(final ClientNode serverNode, final ClientNode localNode) {
        final ControllerServices services = ControllerServices.getInstance();
        if (services.getNetworking() == null || services.getContext().getSelf() == null) {
            LOG.warn("Cannot send IAM: networking or self profile not initialized");
            return;
        }

        System.out.println("sending ILeave packet");
        final ILeavePacket iLeavePacket = new ILeavePacket(services.getContext().getSelf().getEmail(),
                services.getContext().getSelf().getDisplayName(),
                localNode);
        byte[] data = iLeavePacket.serialize();
        final int ordinal = Byte.toUnsignedInt(data[0]);
        LOG.info("LEAVE Ordinal is " + ordinal);

        sendBytes(data, new ClientNode[] { serverNode });
    }

    private static void handleLeavePacket(byte[] data) {
        final ControllerServices services = ControllerServices.getInstance();
        final MeetingSession meeting = services.getContext().getMeetingSession();
        if (meeting == null) {
            LOG.warn("No active meeting to process IAM packet");
            return;
        }

        final ILeavePacket packet = ILeavePacket.deserialize(data);
        final boolean isServer = services.getContext().getSelf() != null
                && meeting.getCreatedBy().equals(services.getContext().getSelf().getEmail());

        if (isServer) {
            // Server receives IAM as a join request
            if (meeting.getParticipants().containsKey(packet.getClientNode())) {
                // LOG.info("User wants to leave meeting having mail: " + packet.getEmail());
                // Convert participants map to the two separate maps for leaveBrodcast
                meeting.removeParticipantByNode(packet.getClientNode());
                final Map<ClientNode, String> nodeToEmailMap = new HashMap<>();
                for (Map.Entry<ClientNode, UserProfile> entry : meeting.getParticipants().entrySet()) {
                    final UserProfile profile = entry.getValue();
                    // if (profile.getEmail() != null) {
                        nodeToEmailMap.put(entry.getKey(), profile.getEmail());
                    // }
                }
                final ClientNode localNode = getLocalClientNode();
                final List<ClientNode> recipients = new ArrayList<>();
                for (ClientNode node : nodeToEmailMap.keySet()) {
                    if (!node.equals(localNode)) {
                        recipients.add(node);
                    }
                }

                System.out.println("broadcasting leave packet :" + recipients.size());

                broadcastILeave(recipients);
            }
        } else {
            System.out.println("handleIncoming ILeave client");
            if (meeting.getParticipants().containsKey(packet.getClientNode())) {
                if (packet.getClientNode().hostName() == ControllerServices.getInstance().getContext().getMainServerIP()
                        .hostName()) {
                    try {
                        services.getContext().getRpc().call("core/LeaveMeeting",
                                new byte[0])
                                .get();
                        Networking.getNetwork().closeNetworking();
                    } catch (InterruptedException | ExecutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                System.out.println("handleIncoming ILeave client leave");
                meeting.removeParticipantByNode(packet.getClientNode());

                LOG.info("Received delete (announcement) from " + packet.getEmail()
                        + " (" + packet.getDisplayName() + ") at " + packet.getClientNode());
            }
        }
        try {
            services.getContext().getRpc().call("core/updateParticipants",
                    DataSerializer.serialize(services.getContext().getMeetingSession().getParticipants()))
                    .get();
        } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void broadcastILeave(final Collection<ClientNode> recipients) {
        final ControllerServices services = ControllerServices.getInstance();
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        if (services.getNetworking() == null || services.getContext().getSelf() == null) {
            LOG.warn("Cannot broadcast IAM: networking or self profile not initialized");
            return;
        }

        final ClientNode localNode = getLocalClientNode();
        final ILeavePacket packet = new ILeavePacket(services.getContext().getSelf().getEmail(),
                services.getContext().getSelf().getDisplayName(),
                localNode);
        sendBytes(packet.serialize(), recipients.toArray(new ClientNode[0]));
    }
}
