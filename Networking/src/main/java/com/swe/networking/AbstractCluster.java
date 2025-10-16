package com.swe.networking;

/**
 * Interface used between topology and Cluster to add clients.
 *
 */
public interface AbstractCluster {
    void addClient(String ip, Integer port);
}
