package com.swe.controller;

import datastructures.Entity;
import datastructures.Response;
import datastructures.TimeRange;
import functionlibrary.CloudFunctionLibrary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.networking.ClientNode;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Utils {
    public static ClientNode getLocalClientNode() throws UnknownHostException {
        return new ClientNode(InetAddress.getLocalHost().getHostAddress(), 6942);
    }

    public static ClientNode getServerClientNode(String meetingId, CloudFunctionLibrary cloud) throws UnknownHostException {
        // TODO: Get the server IP address from the cloud
        try {
            // Build the request entity
            Entity request = new Entity(
                    "",
                    "MeetIdTable",
                    meetingId,
                    "",
                    0,
                    null,
                    null);

            System.out.println("Request to cloud: " + request.toString());
            
            // Response responseCreate = cloud.cloudCreate(request);
            // System.out.println("Response from cloud create: " + responseCreate.message());

            // Post to cloud
            Response response = cloud.cloudGet(request);
            JsonNode node = request.data();

            String ipAddress = node.get("ipAddress").asText();
            int port = node.get("port").asInt();

            ClientNode serverNode = new ClientNode(ipAddress, port);
            System.out.println("Retrieved server node from cloud: " + serverNode.toString());
            return serverNode;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setServerClientNode(String meetingId, CloudFunctionLibrary cloud) throws UnknownHostException {
        // TODO: Set the server IP address to the cloud
        try {
            // Create JSON manually
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            root.put("ipAddress", getLocalClientNode().hostName());
            root.put("port", getLocalClientNode().port());

            // Build the request entity
            Entity request = new Entity(
                    "",
                    "MeetIdTable",
                    meetingId,
                    "",
                    0,
                    null,
                    root);

            System.out.println("Request to cloud: " + request.toString());
            
            Response responseCreate = cloud.cloudCreate(request);
            System.out.println("Response from cloud create: " + responseCreate.message());

            // Post to cloud
            Response response = cloud.cloudPost(request);
            System.out.println("Response from cloud: " + response.message());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
