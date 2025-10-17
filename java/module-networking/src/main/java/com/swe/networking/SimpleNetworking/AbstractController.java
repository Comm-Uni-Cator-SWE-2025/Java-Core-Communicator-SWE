package com.swe.networking.SimpleNetworking;

import com.swe.networking.ClientNode;

public interface AbstractController {
    void addUser(ClientNode deviceAddress, ClientNode mainServerAddress);
}
