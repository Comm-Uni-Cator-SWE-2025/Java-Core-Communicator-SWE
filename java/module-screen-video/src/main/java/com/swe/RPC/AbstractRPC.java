package com.swe.RPC;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Interface for Remote Procedure Call (RPC) mechanism.
 */
public interface AbstractRPC {

    void subscribe(String name, Function<byte[], byte[]> func);

    /**
     * Connects the other side.
     */
    Thread connect() throws IOException, ExecutionException, InterruptedException;

    CompletableFuture<byte[]> call(String name, byte[] args);
}
