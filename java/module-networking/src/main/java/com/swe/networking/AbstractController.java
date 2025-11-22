package com.swe.networking;

import com.swe.core.RPCinterface.AbstractRPC;

/**
 * The interface between the controller and networking modules. Used to send the
 * joining clients address to the networking module
 *
 */
public interface AbstractController {

    /**
     * Function to add user to the network.
     *
     * @param deviceAddress the device IP address details
     * @param mainServerAddress the main server IP address details
     */
    void addUser(ClientNode deviceAddress, ClientNode mainServerAddress);

    /**
     * Method to close the networking module.
     */
    void closeNetworking();

    /**
     * Method to consume the RPC. This function attach all the handlers for the
     * RPC methods to the networking module. NO METHOD MAY BE ATTACHED TO THE
     * RPC AFTER THIS FUNCTION IS CALLED.
     *
     * @param rpc the RPC to consume.
     */
    void consumeRPC(AbstractRPC rpc);

    /**
     * Function to see if the client is part of the network.
     *
     * @param client the client to check
     * @return if the client is present or not
     */
    boolean isClientAlive(ClientNode client);

    /**
     * Function to check if the main server is live by attempting to connect
     * to a high availability public DNS server (Google or Cloudflare).
     * This serves as a network connectivity check to determine if the
     * main server could potentially be reachable.
     *
     * @return true if connection fails (network appears down), false if connection succeeds
     */
    boolean isMainServerLive();
}
