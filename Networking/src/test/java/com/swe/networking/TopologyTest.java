package com.swe.networking;

import static org.junit.jupiter.api.Assertions.*;

class TopologyTest {

    @org.junit.jupiter.api.Test
    void getServer() {
        Topology topology = Topology.getTopology();
        String destination = "localhost";
        ClientNode client = topology.getServer(destination);
        assertNull(client, "The output is not what is expected");
    }
}