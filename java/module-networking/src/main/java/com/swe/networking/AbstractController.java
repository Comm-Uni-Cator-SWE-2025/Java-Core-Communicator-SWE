package com.swe.networking;

import com.swe.core.ClientNode;

/**
 * The interface between the controller and networking modules.
 * Used to send the joining clients address to the networking module
 *
 */
public interface AbstractController {
    /**
     * Function to add user to the network.
     *
     * @param deviceAddress     the device IP address details
     * @param mainServerAddress the main server IP address details
     */
    void addUser(ClientNode deviceAddress, ClientNode mainServerAddress);
}
