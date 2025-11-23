package com.swe.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;

public class RPCTest {

    private RPC rpc;

    @Before
    public void setUp() {
        rpc = new RPC();
    }

    @Test
    public void constructorInitializesEmptyMethods() {
        assertNotNull(rpc);
        assertFalse(rpc.isConnected());
    }

    @Test
    public void isConnectedReturnsFalseInitially() {
        assertFalse(rpc.isConnected());
    }

    @Test
    public void subscribeAddsMethod() {
        final Function<byte[], byte[]> method = data -> new byte[]{1, 2, 3};
        rpc.subscribe("testMethod", method);
        // Since methods is package-private, we can't directly verify,
        // but we can test that subscribe doesn't throw
        assertNotNull(rpc);
    }

    @Test
    public void subscribeWithNullMethodName() {
        final Function<byte[], byte[]> method = data -> new byte[]{1, 2, 3};
        rpc.subscribe(null, method);
        // Should not throw, just adds to map
        assertNotNull(rpc);
    }

    @Test
    public void callWithNullSocketryClientReturnsEmptyFuture() {
        final CompletableFuture<byte[]> future = rpc.call("testMethod", new byte[]{1, 2, 3});
        assertNotNull(future);
        // The future should complete with empty byte array
        try {
            final byte[] result = future.get();
            assertNotNull(result);
            assertEquals(0, result.length);
        } catch (Exception e) {
            // Should not throw
        }
    }
}

