/*
 * -----------------------------------------------------------------------------
 *  File: AbstractTopology.java
 *  Owner: Vishal
 *  Roll Number : 112201049
 *  Module : Networking
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.networking;

import com.swe.core.ClientNode;

/**
 * Interface used between Cluster and the topology class.
 *
 */
public interface AbstractTopology {
    /**
     * Function to get the cluster server.
     *
     * @param dest the destination to be sent to
     * @return the cluster server of the destination client belongs to
     */
    ClientNode getServer(ClientNode dest);
}
