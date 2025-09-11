package com.swe.networking;

/**
 * The addUser function here invokes a function on topology which in turn
 * adds the client to a cluster
 * */

public interface AbstractController {

    void addUser(String ip, Integer port);
}
