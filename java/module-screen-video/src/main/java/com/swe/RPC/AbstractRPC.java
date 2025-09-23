package com.swe.RPC;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for Remote Procedure Call (RPC) mechanism. 
 */
public interface AbstractRPC {
    
    void subscribe(String name, RProcedure func);
    
    CompletableFuture<byte[]> call(String name, byte[] args);
}
