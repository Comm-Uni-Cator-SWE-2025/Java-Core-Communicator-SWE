package com.swe.controller;

import com.swe.controller.RPCinterface.AbstractRPC;

public class dummyCloud {

    AbstractRPC rpc;

    dummyCloud (AbstractRPC rpc) {
        this.rpc = rpc;
    }
}
