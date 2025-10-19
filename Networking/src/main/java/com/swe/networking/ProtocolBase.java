package com.swe.networking;

import java.net.Socket;

/**
 * Common interface to send data using various protocols.
 */
public interface ProtocolBase {
    /**
     * Opens a socket in the current device.
     *
     * @return the created socket
     */
    Socket openSocket();

    /**
     * To close an opened socket.
     *
     * @param client the client socket to close.
     */
    void closeSocket(String client);

    /**
     * To send data to given destination.
     *
     * @param data the data to be sent
     * @param dest the dest to send the data
     */
    void sendData(byte[] data, ClientNode dest);

    /**
     * To receive data/socket form clients.
     *
     */
    void receiveData();
}
