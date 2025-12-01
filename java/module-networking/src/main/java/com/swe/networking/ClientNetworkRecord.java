package com.swe.networking;

import com.swe.core.ClientNode;

/**
 * Record to store a client details in a network.
 * 
 * @param client       IP address of the client
 * @param clusterIndex the index of cluster it belongs to
 */
public record ClientNetworkRecord(ClientNode client, int clusterIndex) {
}
