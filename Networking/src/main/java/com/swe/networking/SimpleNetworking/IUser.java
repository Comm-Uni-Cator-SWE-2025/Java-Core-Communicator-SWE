package com.swe.networking.SimpleNetworking;

import java.io.IOException;

import com.swe.networking.ClientNode;

public interface IUser {
    void send(byte[] data, ClientNode[] destIp, ClientNode serverIp);

    void receive() throws IOException;
}
