package com.swe.networking;

/**
 * The interface between the controller and networking modules.
 * Used to  send the joining clients address to the networking module
 *
 */

public interface AbstractController {
    void addUser(String ip, Integer port);
}
