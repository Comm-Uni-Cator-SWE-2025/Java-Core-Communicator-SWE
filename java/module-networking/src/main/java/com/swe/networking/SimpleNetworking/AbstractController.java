package com.swe.networking.SimpleNetworking;

import com.swe.networking.ClientNode;

/**
 * Interface to interact between controller and networking.
 */
public interface AbstractController {
    /**
     * Method to add user details to the network.
     *
     * @param deviceAddress     the device IP address details.
     * @param mainServerAddress the main server IP address details.
     */
    void addUser(ClientNode deviceAddress, ClientNode mainServerAddress);

    /**
     * Method to close the networking module.
     */
    void closeNetworking();
}
