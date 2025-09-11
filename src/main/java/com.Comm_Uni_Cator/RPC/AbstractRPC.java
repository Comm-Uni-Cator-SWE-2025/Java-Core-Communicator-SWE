package com.Comm_Uni_Cator.RPC;

import java.util.concurrent.CompletableFuture;

public interface AbstractRPC {
    void subscribe(String name, RProcedure func);
    CompletableFuture<byte[]> Call(String name, byte[] args);
}