package com.swe.chat;

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
     * @return the {@link Thread} instance handling the connection loop
     * @throws IOException if an I/O error occurs during connection
     * @throws ExecutionException if the connection task encounters an exception
     * @throws InterruptedException if the connection process is interrupted
     */
    Thread connect() throws IOException, ExecutionException, InterruptedException;

    CompletableFuture<byte[]> call(String name, byte[] args);
}