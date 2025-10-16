package com.swe.networking.SimpleNetworking;

import java.io.IOException;

import com.swe.networking.ClientNode;

public interface IUser {
    public void send(byte[] data, ClientNode[] destIp, ClientNode serverIp);

    public void receive() throws IOException;
}
