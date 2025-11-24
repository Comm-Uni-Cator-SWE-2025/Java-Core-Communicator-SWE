package com.swe.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import com.swe.networking.SimpleNetworking.MessageListener;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NetworkingAdapterTest {

    private final Networking networking = mock(Networking.class);
    private final NetworkingAdapter adapter = new NetworkingAdapter(networking);

    @Test
    void sendDataConvertsModuleEnum() {
        final byte[] payload = new byte[] { 4, 5, 6 };
        final ClientNode[] destinations = { new ClientNode("127.0.0.1", 6000) };

        adapter.sendData(payload, destinations, ModuleType.CHAT, 2);

        verify(networking).sendData(payload, destinations, ModuleType.CHAT.ordinal(), 2);
    }

    @Test
    void subscribeBridgesListenerType() {
        final MessageListener listener = mock(MessageListener.class);
        final ArgumentCaptor<com.swe.networking.MessageListener> captor = ArgumentCaptor
                .forClass(com.swe.networking.MessageListener.class);

        adapter.subscribe(ModuleType.CONTROLLER, listener);

        verify(networking).subscribe(eq(ModuleType.CONTROLLER.ordinal()), captor.capture());
        final byte[] data = new byte[] { 1 };
        captor.getValue().receiveData(data);
        verify(listener).receiveData(data);
    }

    @Test
    void removeSubscriptionDelegatesToNetworking() {
        adapter.removeSubscription(ModuleType.SCREENSHARING);

        verify(networking).removeSubscription(ModuleType.SCREENSHARING.ordinal());
    }

    @Test
    void addUserDelegatesToNetworking() {
        final ClientNode device = new ClientNode("192.168.1.10", 7000);
        final ClientNode server = new ClientNode("192.168.1.1", 8000);

        adapter.addUser(device, server);

        verify(networking).addUser(device, server);
    }

    @Test
    void closeNetworkingDelegates() {
        adapter.closeNetworking();

        verify(networking).closeNetworking();
    }

    @Test
    void consumeRPCWrapsCalls() throws Exception {
        final AbstractRPC rpc = mock(AbstractRPC.class);
        final ArgumentCaptor<AbstractRPC> captor = ArgumentCaptor.forClass(AbstractRPC.class);

        adapter.consumeRPC(rpc);

        verify(networking).consumeRPC(captor.capture());
        final AbstractRPC forwarded = captor.getValue();

        forwarded.subscribe("method", bytes -> bytes);
        verify(rpc).subscribe(eq("method"), any());

        final Thread thread = new Thread();
        org.mockito.Mockito.when(rpc.connect(55)).thenReturn(thread);
        assertSame(thread, forwarded.connect(55));

        final CompletableFuture<byte[]> future = CompletableFuture.completedFuture(new byte[] { 7 });
        org.mockito.Mockito.when(rpc.call(eq("rpcMethod"), any())).thenReturn(future);
        assertSame(future, forwarded.call("rpcMethod", new byte[] { 0 }));
        verify(rpc).call(eq("rpcMethod"), any());
    }
}
