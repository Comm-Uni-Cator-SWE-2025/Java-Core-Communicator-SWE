package com.swe.networking;

import com.swe.core.ClientNode;

/**
 * Interface used between topology and Cluster to add clients.
 *
 */
public interface AbstractCluster {
    /**
     * Function to add a client to the cluster.
     *
     * @param client the client to be added
     */
    void addClient(ClientNode client);
}
