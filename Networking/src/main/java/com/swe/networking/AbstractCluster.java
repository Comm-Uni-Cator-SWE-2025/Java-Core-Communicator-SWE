package com.swe.networking;

/**
 * The cluster class implements this interface to handle
 * adding of clients to the cluster and the topology
 * class invokes this function
 *
 */

public interface AbstractCluster {

    void addClient(String ip, Integer port);
}
