package com.swe.controller.RPCinterface;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public interface AbstractRPC {
    public void subscribe(String methodName, Function<byte[], byte[]> method);

    public Thread connect(int portNumber) throws IOException, InterruptedException, ExecutionException;

    public CompletableFuture<byte[]> call(String methodName, byte[] data);
}
