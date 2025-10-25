package com.swe.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Cluster class to store details of a given cluster.
 *
 */
public class P2PCluster {
    /**
     * List of all clients belonging to the same cluster.
     */
    private List<ClientNode> clients;

    /**
     * Timeout to keep track of all clients.
     */
    private HashMap<String, Long> clientTimeouts;

    /**
     * The status of current device as client / server.
     */
    private P2PUser user;

    /**
     * The server details of the current cluster.
     */
    private ClientNode clusterServer;

    /**
     * To check if the current device is a server or not.
     */
    private boolean isServer = false;

    public P2PCluster() {
        System.out.println("Creating a new P2P Cluster...");
        clients = new ArrayList<>();
        clientTimeouts = new HashMap<>();
    }

    /**
     * Function to add current user to the network.
     * 
     * @param client details of the current client
     * @param server details of the mainserver
     */
    public void addUser(final ClientNode client, final ClientNode server) {
        System.out.println("Adding new user to the network...");

    }

    /**
     * Function to resize the cluster dynamically.
     * 
     * @param size The required size of cluster
     */
    public void resizeCluster(final Integer size) {
        if (clients.size() < size - 1) {
            System.out.println("Cannot resize cluster...");
        }
    }

    public void updateClientTimeout(final String client) {

    }

}
