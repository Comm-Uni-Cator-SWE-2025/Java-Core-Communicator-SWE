package com.swe.controller;

import com.swe.controller.RPCinterface.AbstractRPC;

public class dummyCloud {

    AbstractRPC rpc;

    dummyCloud (AbstractRPC rpc) {
        this.rpc = rpc;
    }

    public String getIpAddr(String meetId) {
        return "127.0.0.1";
    }

    public int getPort(String meetId) {
        return 8000;
    }
}
