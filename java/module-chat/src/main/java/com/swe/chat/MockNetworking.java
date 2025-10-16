package com.swe.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * A mock networking implementation for testing without a real server.
 * Loops back messages to subscribed listeners.
 */
public class MockNetworking implements AbstractNetworking {

    /**
     * List of listeners that are subscribed to receive data.
     */
    private final List<MessageListener> listeners = new ArrayList<>();

    /**
     * Sends data to all subscribed listeners (loopback).
     *
     * @param data  the message bytes
     * @param dest  the destination addresses (unused in mock)
     * @param port  the destination ports (unused in mock)
     */
    @Override
    public void sendData(final byte[] data, final String[] dest, final int[] port) {
        // Just loop back the data to the subscribed listener
        for (MessageListener listener : listeners) {
            listener.receiveData(data);
        }
    }

    /**
     * Subscribes a new listener.
     *
     * @param name      the subscriber name
     * @param function  the listener callback
     */
    @Override
    public void subscribe(final String name, final MessageListener function) {

        this.listeners.add(function);
    }

    /**
     * Removes a subscription by name.
     *
     * @param name  the subscriber name
     */
    @Override
    public void removeSubscription(final String name) {
//        this.listener = null;
    }


    /**
     * Simulates a server message and notifies all listeners.
     *
     * @param data  the simulated message
     */
    public void simulateMessageFromServer(final byte[] data) {
        System.out.println("NETWORK_SIM: A message was received from the 'server'.");
        for (MessageListener listener : listeners) {
            listener.receiveData(data);
        }
    }
}
