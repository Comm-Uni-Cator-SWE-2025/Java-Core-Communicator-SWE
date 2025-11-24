package com.swe.controller;

import datastructures.Entity;
import datastructures.Response;
import functionlibrary.CloudFunctionLibrary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.core.ClientNode;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Utility class for controller operations.
 */
public class Utils {

    /**
     * Logger for utility operations.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("CONTROLLER-APP");

    /**
     * Default port for client node.
     */
    private static final int DEFAULT_CLIENT_PORT = 6943;

    /**
     * Gets the local client node.
     *
     * @return The local client node
     * @throws UnknownHostException If the host cannot be determined
     */
    public static ClientNode getLocalClientNode() throws UnknownHostException {
        try (DatagramSocket socket = new DatagramSocket()) {
            final int pingPort = 10002;
            socket.connect(InetAddress.getByName("8.8.8.8"), pingPort);
            return new ClientNode(socket.getLocalAddress().getHostAddress(), DEFAULT_CLIENT_PORT);
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the server client node for a meeting.
     *
     * @param meetingId The meeting ID
     * @param cloud The cloud function library
     * @return The server client node
     * @throws UnknownHostException If the host cannot be determined
     */
    public static ClientNode getServerClientNode(final String meetingId, final CloudFunctionLibrary cloud)
        throws UnknownHostException {
        try {
            // Build the request entity
            final Entity request = new Entity(
                    "",
                    "MeetIdTable",
                    meetingId,
                    "",
                    0,
                    null,
                    null);

            LOG.debug("Request to cloud: " + request);

            // Post to cloud
            final Response response = cloud.cloudGet(request);
            final JsonNode node = response.data();

            LOG.debug("Data from cloud: " + node);
            final JsonNode dataNode = node.get("data");

            final String ipAddress = dataNode.get("ipAddress").asText();
            final int port = dataNode.get("port").asInt();

            final ClientNode serverNode = new ClientNode(ipAddress, port);
            LOG.info("Retrieved server node from cloud: " + serverNode);
            return serverNode;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the server client node for a meeting in the cloud.
     *
     * @param meetingId The meeting ID
     * @param cloud The cloud function library
     * @throws UnknownHostException If the host cannot be determined
     */
    public static void setServerClientNode(final String meetingId, final CloudFunctionLibrary cloud)
        throws UnknownHostException {
        try {
            // Create JSON manually
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = mapper.createObjectNode();
            root.put("ipAddress", getLocalClientNode().hostName());
            root.put("port", getLocalClientNode().port());

            // Build the request entity
            final Entity request = new Entity(
                    "",
                    "MeetIdTable",
                    meetingId,
                    "",
                    0,
                    null,
                    root);

            LOG.debug("Request to cloud: " + request);

            final Response responseCreate = cloud.cloudCreate(request);
            LOG.debug("Response from cloud create: " + responseCreate.message());

            // Post to cloud
            final Response response = cloud.cloudPost(request);
            LOG.debug("Response from cloud: " + response.message());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
