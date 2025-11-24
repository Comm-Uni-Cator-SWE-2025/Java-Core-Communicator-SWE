/**
 * Abstract RPC interface.
 * Contributed by Pushti Vasoya.
 */
package com.swe.core.RPCinterface;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Abstract RPC interface for remote procedure calls.
 */
public interface AbstractRPC {
    /**
     * Subscribes a method to the RPC interface.
     *
     * @param methodName The method name
     * @param method The method implementation
     */
    void subscribe(String methodName, Function<byte[], byte[]> method);

    /**
     * Connects to the RPC server.
     *
     * @param portNumber The port number
     * @return The RPC thread
     * @throws IOException If connection fails
     * @throws InterruptedException If interrupted
     * @throws ExecutionException If execution fails
     */
    Thread connect(int portNumber) throws IOException, InterruptedException, ExecutionException;

    /**
     * Calls a remote procedure.
     *
     * @param methodName The method name
     * @param data The data to send
     * @return A future with the result
     */
    CompletableFuture<byte[]> call(String methodName, byte[] data);
}
