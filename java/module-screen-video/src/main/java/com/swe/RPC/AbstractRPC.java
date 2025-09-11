package com.swe.RPC;

import java.util.concurrent.CompletableFuture;

public interface AbstractRPC {
    void subscribe(String name, RProcedure func);
    CompletableFuture<byte[]> Call(String name, byte[] args);
}