package com.swe.chat; // Assuming it's in the same package

import com.swe.chat.AbstractRPC;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * A dummy, in-memory implementation of the AbstractRPC interface for testing.
 * This class simulates the RPC mechanism by connecting subscribers and callers
 * directly using a HashMap. It does not perform any real network operations.
 */
public class DummyRPC implements AbstractRPC {

    /**
     * Stores all the "subscribed" functions.
     * Key: The topic name (e.g., "chat:send-message")
     * Value: The function to execute (e.g., ChatManager's handler)
     */
    private final Map<String, Function<byte[], byte[]>> subscribers = new ConcurrentHashMap<>();

    /**
     * Subscribes a handler function to a specific topic name.
     *
     * @param name The topic name to listen for.
     * @param func The function that will handle the call.
     */
    @Override
    public void subscribe(String name, Function<byte[], byte[]> func) {
        System.out.println("[DummyRPC] SUBSCRIBED: '" + name + "'");
        subscribers.put(name, func);
    }

    /**
     * This is a dummy method. It doesn't actually connect to anything,
     * but it fulfills the interface.
     */
    @Override
    public Thread connect() throws IOException, ExecutionException, InterruptedException {
        System.out.println("[DummyRPC] 'connect()' called. Dummy connection established.");
        // We can just return a simple thread that does nothing.
        Thread dummyThread = new Thread(() -> {
            // This is just a placeholder.
        });
        dummyThread.start();
        return dummyThread;
    }

    /**
     * "Calls" a remote procedure by looking it up in the local subscriber map
     * and executing it immediately.
     *
     * @param name The topic name to call (e.g., "chat:send-message").
     * @param args The byte[] payload to send.
     * @return A CompletableFuture that will hold the response.
     */
    @Override
    public CompletableFuture<byte[]> call(String name, byte[] args) {
        System.out.println("[DummyRPC] CALL: '" + name + "' with " + args.length + " bytes.");

        // Find the subscribed function
        Function<byte[], byte[]> handler = subscribers.get(name);

        if (handler != null) {
            try {
                // Run the function and get the result
                byte[] result = handler.apply(args);
                // Return the result in a completed CompletableFuture
                return CompletableFuture.completedFuture(result);
            } catch (Exception e) {
                System.err.println("[DummyRPC] ERROR handling call to '" + name + "': " + e.getMessage());
                return CompletableFuture.failedFuture(e);
            }
        } else {
            // No one was subscribed to this topic
            System.err.println("[DummyRPC] WARNING: No subscriber found for topic '" + name + "'. Call was ignored.");
            return CompletableFuture.completedFuture(null); // No handler, return null
        }
    }
}