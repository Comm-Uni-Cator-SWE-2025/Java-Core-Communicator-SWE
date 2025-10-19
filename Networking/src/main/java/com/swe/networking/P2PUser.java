package com.swe.networking;

/**
 * Common interface to be used by client and server.
 */
public interface P2PUser {
    /**
     * Function to send data by the user.
     * @param data the data to be sent
     * @param destIp the detination to send the data
     * @param serverIp the main server of the Cluster
     */
    void send(byte[] data, ClientNode[] destIp, ClientNode serverIp);

    /**
     * Function to receive data from other users.
     */
    void receive();
}
