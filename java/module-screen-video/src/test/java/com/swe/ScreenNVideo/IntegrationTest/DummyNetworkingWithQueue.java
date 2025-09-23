package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.Networking.AbstractNetworking;
import com.swe.Networking.MessageListener;
import com.swe.ScreenNVideo.Utils;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Dummy networking for integration tests.
 * Simulates sending and receiving packets with headers.
 */
public class DummyNetworkingWithQueue implements AbstractNetworking {

    // Simulated subscriptions
    private final Map<String, MessageListener> subscriptions = new ConcurrentHashMap<>();

    // Queue for "network" packets
    private final BlockingQueue<byte[]> packetQueue = new LinkedBlockingQueue<>();

    // Simulated self IP
    private final String selfIP = "127.0.0.1";

    private volatile boolean running = true;

    public DummyNetworkingWithQueue() {
        // Start a background thread to simulate receiving packets
        Thread receiverThread = new Thread(this::receiveLoop, "DummyReceiverThread");
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    @Override
    public void sendData(byte[] data, String[] dest, int[] port) {
        if (data == null) return;

        // Build header: 4 bytes for length
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length);
        buffer.put(data);

        // Instead of sending over network, enqueue locally
        packetQueue.offer(buffer.array());
    }

    @Override
    public String getSelfIP() {
        return selfIP;
    }

    @Override
    public void subscribe(String name, MessageListener function) {
        subscriptions.put(name, function);
    }

    @Override
    public void removeSubscription(String name) {
        subscriptions.remove(name);
    }

    /**
     * Receiver loop: consumes queued packets, reconstructs payload,
     * and forwards to subscribers.
     */
    private void receiveLoop() {
        try {
            while (running) {
                byte[] packet = packetQueue.take(); // blocks until available
                if (packet.length < 4) continue;

                ByteBuffer buffer = ByteBuffer.wrap(packet);
                int length = buffer.getInt();
                byte[] payload = new byte[length];
                buffer.get(payload);

                // Send to "screen_share" subscription
                MessageListener listener = subscriptions.get(Utils.MODULE_REMOTE_KEY);
                if (listener != null) {
                    listener.receiveData(payload);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        running = false;
    }
}
