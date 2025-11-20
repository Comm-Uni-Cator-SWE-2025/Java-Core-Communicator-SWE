package com.swe.networking;

import com.swe.core.ClientNode;
import org.junit.jupiter.api.Test;

public class Networkingtest {

    @Test
    public void testCloseNetworking() {
        try {
            final Networking network = Networking.getNetwork();
            final ClientNode device = new ClientNode("127.0.0.1", 8000);
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            network.addUser(device, server);
            final int sleepTime = 2000;
            Thread.sleep(sleepTime);
            network.closeNetworking();
            network.addUser(device, server);
            Thread.sleep(sleepTime);
            network.closeNetworking();
        } catch (InterruptedException ex) {
        }

    }
}
