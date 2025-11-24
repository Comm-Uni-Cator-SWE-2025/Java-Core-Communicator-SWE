package com.swe.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.core.ClientNode;
import datastructures.Entity;
import datastructures.Response;
import functionlibrary.CloudFunctionLibrary;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class UtilsTest {

    @Test
    void getServerClientNodeParsesCloudResponse() throws Exception {
        final CloudFunctionLibrary cloud = mock(CloudFunctionLibrary.class);
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode responseBody = mapper.createObjectNode();
        final ObjectNode dataNode = mapper.createObjectNode();
        dataNode.put("ipAddress", "10.0.0.5");
        dataNode.put("port", 8123);
        responseBody.set("data", dataNode);
        when(cloud.cloudGet(any(Entity.class))).thenReturn(new Response(200, "ok", responseBody));

        final ClientNode node = Utils.getServerClientNode("meeting-42", cloud);

        assertEquals("10.0.0.5", node.hostName());
        assertEquals(8123, node.port());
        final ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
        verify(cloud).cloudGet(captor.capture());
        assertEquals("MeetIdTable", captor.getValue().table(), "Request should target meeting table");
    }

    @Test
    void setServerClientNodePostsLocalAddress() throws Exception {
        final CloudFunctionLibrary cloud = mock(CloudFunctionLibrary.class);
        when(cloud.cloudCreate(any(Entity.class))).thenReturn(new Response(200, "created", null));
        when(cloud.cloudPost(any(Entity.class))).thenReturn(new Response(200, "posted", null));
        final ClientNode localNode = new ClientNode("192.168.0.10", 6500);
        final AtomicReference<ClientNode> captured = new AtomicReference<>();

        try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class, Mockito.CALLS_REAL_METHODS)) {
            utilities.when(Utils::getLocalClientNode).thenReturn(localNode);
            Utils.setServerClientNode("meeting-100", cloud);
        }

        final ArgumentCaptor<Entity> createCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(cloud).cloudCreate(createCaptor.capture());
        verify(cloud).cloudPost(createCaptor.capture());

        createCaptor.getAllValues().forEach(entity -> {
            final String ip = entity.data().get("ipAddress").asText();
            final int port = entity.data().get("port").asInt();
            captured.set(new ClientNode(ip, port));
        });

        final ClientNode reported = captured.get();
        assertNotNull(reported, "Cloud payload should include node data");
        assertEquals(localNode.hostName(), reported.hostName());
        assertEquals(localNode.port(), reported.port());
    }
}
