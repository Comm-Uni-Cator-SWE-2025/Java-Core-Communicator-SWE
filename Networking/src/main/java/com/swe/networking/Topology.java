package com.swe.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * The main architecture of the networking module.
 * Implements the cluster networks
 */
public class Topology implements AbstractTopology, AbstractController {
    /**
     * The hashmap of all clients present separated per cluster.
     */
    private HashMap<Cluster, ArrayList<ClientNode>> clientIP;
    /**
     * The collection of all cluster objects.
     *
     */
    private ArrayList<Cluster> clusters;
    /**
     * The total number of clusters.
     *
     */
    private int numClusters = 0;
    /**
     * The total number of clients.
     *
     */
    private int numClients = 0;


    public Topology() {
        clusters = new ArrayList<Cluster>();
    }

    /**
     * Function returns the cluster server in which the client is present.
     *
     * @param dest The ip address of the destination client
     */
    @Override
    public ClientNode GetServer(final String dest) {
        ClientNode node = null;
        for (Cluster cluster : clusters) {
            final ArrayList<ClientNode> clientIPs = clientIP.get(cluster);
            int idx = -1;
            for (int i = 0; i < clientIPs.size(); i++) {
                if (Objects.equals(clientIPs.get(i).hostName(), dest)) {
                    idx = i;
                    node = clientIPs.get(i);
                }
            }
            if (idx == -1) {
                System.out.println("The client is not part of the network...");
                return null;
            }
            return node;
        }
        return null;
    }

    @Override
    public void addUser(final String ip, final Integer port) {
        numClients += 1;
    }
}
