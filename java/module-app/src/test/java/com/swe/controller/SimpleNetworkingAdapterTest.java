package com.swe.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.MessageListener;
import com.swe.networking.SimpleNetworking.SimpleNetworking;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SimpleNetworkingAdapterTest {

    private final SimpleNetworking simpleNetworking = mock(SimpleNetworking.class);
    private final SimpleNetworkingAdapter adapter = new SimpleNetworkingAdapter(simpleNetworking);

    @Test
    void sendDataDelegatesToSimpleNetworking() {
        final byte[] payload = new byte[] { 1, 2, 3 };
        final ClientNode[] destinations = new ClientNode[] { new ClientNode("127.0.0.1", 9000) };

        adapter.sendData(payload, destinations, ModuleType.CHAT, 1);

        verify(simpleNetworking).sendData(payload, destinations, ModuleType.CHAT, 1);
    }

    @Test
    void subscribeDelegatesToSimpleNetworking() {
        final MessageListener listener = data -> {
        };

        adapter.subscribe(ModuleType.CONTROLLER, listener);

        verify(simpleNetworking).subscribe(ModuleType.CONTROLLER, listener);
    }

    @Test
    void removeSubscriptionDelegatesToSimpleNetworking() {
        adapter.removeSubscription(ModuleType.CANVAS);

        verify(simpleNetworking).removeSubscription(ModuleType.CANVAS);
    }

    @Test
    void addUserDelegatesToSimpleNetworking() {
        final ClientNode device = new ClientNode("10.0.0.3", 7000);
        final ClientNode server = new ClientNode("10.0.0.1", 6000);

        adapter.addUser(device, server);

        verify(simpleNetworking).addUser(device, server);
    }

    @Test
    void closeNetworkingDelegatesToSimpleNetworking() {
        adapter.closeNetworking();

        verify(simpleNetworking).closeNetworking();
    }

    @Test
    void consumeRPCDelegatesToSimpleNetworking() throws Exception {
        final AbstractRPC rpc = mock(AbstractRPC.class);
        final ArgumentCaptor<AbstractRPC> captor = ArgumentCaptor.forClass(AbstractRPC.class);

        adapter.consumeRPC(rpc);

        verify(simpleNetworking).consumeRPC(captor.capture());
        final AbstractRPC wrapped = captor.getValue();

        final CompletableFuture<byte[]> expectedFuture = CompletableFuture.completedFuture(new byte[] { 9 });
        final Thread thread = new Thread();

        wrapped.subscribe("method", bytes -> bytes);
        verify(rpc).subscribe(eq("method"), any());

        when(rpc.connect(42)).thenReturn(thread);
        assertSame(thread, wrapped.connect(42));

        when(rpc.call(eq("method"), any())).thenReturn(expectedFuture);
        assertSame(expectedFuture, wrapped.call("method", new byte[] { 1 }));
    }
}
