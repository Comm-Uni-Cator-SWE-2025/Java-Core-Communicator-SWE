package com.swe.controller;

import com.swe.networking.ClientNode;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Utils {
    public static ClientNode getLocalClientNode() throws UnknownHostException {
        return new ClientNode(InetAddress.getLocalHost().getHostAddress(), 6942);
    }

    public static ClientNode getServerClientNode() throws UnknownHostException {
        // TODO: Get the server IP address from the cloud
        return new ClientNode("10.32.11.242", 6942);
    }
}
