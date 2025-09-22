package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.Networking.AbstractNetworking;
import com.swe.Networking.MessageListener;

public class DummyNetworking implements AbstractNetworking {

    @Override
    public void SendData(byte[] data, String[] dest, int[] port) {

    }

    @Override
    public String getSelfIP() {
        return "";
    }

    @Override
    public void Subscribe(String name, MessageListener function) {

    }

    @Override
    public void RemoveSubscription(String name) {

    }
}
