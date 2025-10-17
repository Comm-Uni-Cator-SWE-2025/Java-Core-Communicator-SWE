package com.swe.networking;

/**
 * Interface used between Cluster and the topology class.
 *
 */
public interface AbstractTopology {
    ClientNode getServer(String dest);
}
