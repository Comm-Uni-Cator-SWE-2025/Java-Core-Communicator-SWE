package com.swe.networking;

import static java.lang.Math.floor;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The main architecture of the networking module.
 * Implements the cluster networks
 */
public final class Topology implements AbstractTopology, AbstractController {
    /**
     * The hashmap of all clients present separated per cluster.
     */
    private HashMap<Cluster, ArrayList<ClientNode>> clientDetails;
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

    /**
     * Singleton design pattern to prevent repeating class instantiations.
     *
     */
    private static Topology topology = null;

    private Topology() {
        clusters = new ArrayList<>();
    }

    /**
     * Function to get the statically instantiated class object.
     *
     * @return Topology the statically instantiated class.
     */
    public static Topology getTopology() {
        if (topology == null) {
            System.out.println("Creating new Topology object...");
            topology = new Topology();
        }
        System.out.println("Passing already instantiated Topology object...");
        return topology;
    }

    /**
     * Function returns the cluster server in which the client is present.
     *
     * @param dest The ip address of the destination client
     */
    @Override
    public ClientNode getServer(final String dest) {
        ClientNode node = null;
        int idx = -1;
        for (Cluster cluster : clusters) {
            final ArrayList<ClientNode> clientDetailss =
                clientDetails.get(cluster);
            for (ClientNode client : clientDetailss) {
                if (dest.equals(client.hostName())) {
                    idx = clusters.indexOf(cluster);
                    node = cluster.getServerName();
                    break;
                }
            }
            if (node != null) {
                break;
            }
        }
        if (node == null) {
            System.out.println("The client is not part of the network...");
            return null;
        }
        System.out.println("Adding client to cluster " + idx + " ...");
        return node;
    }

    /**
     * Add a user to the topology.
     * Logic: choose a cluster, add the user, and update bookkeeping.
     *
     * @param deviceAddress     Ip address of the current device
     * @param mainServerAddress Ip address of the server device
     */
    @Override
    public void addUser(final ClientNode deviceAddress,
            final ClientNode mainServerAddress) {
        // update the network and add the client
        if (deviceAddress.equals(mainServerAddress)) {
            System.out.println("This device is considered as the main Server");
        }
    }

    /**
     * Choose a cluster based on √N rule and least loaded cluster.
     *
     * @return best chosen Cluster
     */
    private Cluster chooseCluster() {
        final int totalClients = numClients;
        final int maxClientPerCluster = (int) floor(sqrt(totalClients)) + 1;

        if (clusters.isEmpty()) {
            final Cluster newCluster = new Cluster();
            clusters.add(newCluster);
            clientDetails.put(newCluster, new ArrayList<>());
            numClusters++;
            return newCluster;
        }

        // Find the least loaded cluster
        Cluster minCluster = clusters.get(0);
        int minSize = clientDetails.getOrDefault(minCluster,
                new ArrayList<>()).size();

        for (Cluster candidate : clusters) {
            final int size = clientDetails.getOrDefault(candidate,
                    new ArrayList<>()).size();
            if (size < minSize) {
                minCluster = candidate;
                minSize = size;
            }
        }

        // Create new cluster if allowed and needed
        if (minSize >= maxClientPerCluster) {
            final Cluster newCluster = new Cluster();
            clusters.add(newCluster);
            clientDetails.put(newCluster, new ArrayList<>());
            numClusters++;
            return newCluster;
        }

        return minCluster;
    }

    /**
     * This function returns the current Network details.
     *
     * @return structure - The Devices connected to the current network
     */
    public NetworkStructure getNetwork() {
        final List<List<ClientNode>> clients = new ArrayList<>();
        final List<ClientNode> servers = new ArrayList<>();
        final NetworkStructure structure =
            new NetworkStructure(clients, servers);
        for (Cluster cluster : clusters) {
            structure.clusters().add(cluster.getClients());
            structure.servers().add(cluster.getServerName());
        }
        return structure;
    }

    // Ensure the receive is not working during updating the network or it could
    // cause unpredicted results
    /**
     * Function to update the network.
     */
    public void updateNetwork() {
        // List<List<ClientNode>> receivedClusters = network.clusters();
        // ArrayList<ClientNode> clients = new ArrayList<>();
        // clientDetails = new HashMap<>();
        // clusters = new ArrayList<>();
        // for (List<ClientNode> cluster : receivedClusters) {

        // }

    }
}
