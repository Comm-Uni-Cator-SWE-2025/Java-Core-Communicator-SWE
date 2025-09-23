package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.RPC.AbstractRPC;
import com.swe.RPC.RProcedure;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DummyRPC implements AbstractRPC {
    HashMap<String, RProcedure> procedures;

    public DummyRPC() {
        procedures = new HashMap<>();
    }

    @Override
    public void subscribe(String name, RProcedure func) {
        procedures.put(name, func);
    }

    @Override
    public CompletableFuture<byte[]> call(String name, byte[] args) {
        RProcedure procedure = procedures.get(name);
        if (procedure == null) {
            return null;
        }
        return CompletableFuture.supplyAsync( () -> procedure.call(args));
    }
}
