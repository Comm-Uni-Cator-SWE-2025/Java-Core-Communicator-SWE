package com.swe.networking;

import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import com.swe.networking.MessageListener;
import com.swe.networking.PriorityQueue;
import com.swe.networking.PacketParser;
import com.swe.networking.PacketInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import java.lang.reflect.Field;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkingTest {

    private Networking networking;

    private PriorityQueue priorityQueue;
    private PacketParser packetParser;
    private Topology topology;

    private final ClientNode serverNode = new ClientNode("127.0.0.1", 8000);
    private final ClientNode clientNode = new ClientNode("127.0.0.1", 8001);

    /**
     * This setup runs before each @Test.
     */
    @BeforeEach
    public void setUp() throws Exception {

        networking = Networking.getNetwork();
        priorityQueue = PriorityQueue.getPriorityQueue();
        packetParser = PacketParser.getPacketParser();
        topology = Topology.getTopology(); // Get topology for setup
        priorityQueue.clear();
        networking.addUser(serverNode, serverNode);
    }

    private static void resetStaticSingleton(final Class<?> targetClass, final String fieldName, final Object value) throws Exception {
        final Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (networking != null) {
            networking.closeNetworking();
        }
        resetStaticSingleton(Topology.class, "topology", null);
        resetStaticSingleton(PacketParser.class, "parser", null);
        resetStaticSingleton(Networking.class, "networking", null);
        resetStaticSingleton(PriorityQueue.class, "priorityQueue", null);
    }

    @Test
    public void testSubscribeAndCallSubscriber() {

        AtomicReference<byte[]> receivedDataRef = new AtomicReference<>();
        MessageListener chatListener = (data) -> {
            System.out.println("Received data: " + new String(data));
            receivedDataRef.set(data);
        };

        networking.subscribe(ModuleType.CHAT.ordinal(), chatListener);
        byte[] testData = "hello chat module!".getBytes();

        networking.callSubscriber(ModuleType.CHAT.ordinal(), testData);

        assertNotNull(receivedDataRef.get(), "Listener was not called");
        assertArrayEquals(testData, receivedDataRef.get(), "Listener received incorrect data");
    }

    @Test
    public void testRemoveSubscription() {
        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        MessageListener chatListener = (data) -> {
            System.out.println("Received data: " + new String(data));
            listenerCalled.set(true);
        };

        networking.subscribe(ModuleType.CHAT.ordinal(), chatListener);
        networking.removeSubscription(ModuleType.CHAT.ordinal());

        byte[] testData = "remove this !".getBytes();
        networking.callSubscriber(ModuleType.CHAT.ordinal(), testData);
        networking.removeSubscription(ModuleType.CANVAS.ordinal());

        assertFalse(listenerCalled.get(), "Listener was called after being removed");
    }

    @Test
    public void testSendDataPacketToPriorityQueue() {
        topology.addClient(clientNode);
        byte[] data = "test data".getBytes();
        System.out.println("Sending data to networking : " + new String(data));
        networking.sendData(data, new ClientNode[]{clientNode}, ModuleType.CANVAS.ordinal(), 1);
    }

    @Test
    public void testBroadcast() throws Exception {
        topology.addClient(clientNode);
        Field threadField = Networking.class.getDeclaredField("sendThread");
        threadField.setAccessible(true);

        Thread privateSendThread = (Thread) threadField.get(networking);

        assertNotNull(privateSendThread, "Networking thread was not started");
        privateSendThread.interrupt();
        privateSendThread.join(1000);

        assertTrue(priorityQueue.isEmpty(), "Queue should be empty before test");
        assertFalse(privateSendThread.isAlive(), "Background sendThread must be stopped before testing queue contents.");
        byte[] data = "test broadcast data".getBytes();

        networking.broadcast(data, ModuleType.UIUX.ordinal(), 2);

        System.out.println("size of priority queue: "+priorityQueue.isEmpty());
        assertFalse(priorityQueue.isEmpty(), "Queue should be empty after broadcast");
        byte[] packetBytes = priorityQueue.nextPacket();
        System.out.println("Packet length: "+ Arrays.toString(packetBytes));

        assertNotNull(packetBytes);

        PacketInfo info = packetParser.parsePacket(packetBytes);
        assertEquals(1, info.getBroadcast(), "Broadcast flag should be set to 1");
        assertArrayEquals(data, info.getPayload(), "Payload data does not match");
        privateSendThread = null;
    }

    @Test
    public void testCallSubscriber_NoFunction() {
        assertDoesNotThrow(() -> networking.callSubscriber(ModuleType.CHAT.ordinal() + 99, "dummy".getBytes()));
    }

    @Test
    public void testSendData_NullDestination() {
        byte[] testData = "Should be ignored".getBytes();
        ClientNode[] nullDest = null;

        assertDoesNotThrow(() -> networking.sendData(
                testData,
                nullDest,
                ModuleType.CANVAS.ordinal(),
                1
        ));
        assertTrue(priorityQueue.isEmpty(), "Queue must remain empty when destination is null.");
    }

    @Test
    public void testSubscribe_ExistingModule() {
        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        MessageListener chatListener = (data) -> {
            listenerCalled.set(true);
        };
        networking.subscribe(ModuleType.CHAT.ordinal(), chatListener);
        networking.subscribe(ModuleType.CHAT.ordinal(), chatListener);
        byte[] testData = "test overwrite".getBytes();
        networking.callSubscriber(ModuleType.CHAT.ordinal(), testData);

        assertTrue(listenerCalled.get(), "The module listener should still be active after the duplicate subscription attempt.");
    }

    @Test
    public void testIsClientAlive_Present() {

        topology.addClient(clientNode);
        assertTrue(networking.isClientAlive(clientNode),
                "isClientAlive should return true for a client added to the topology.");

        final ClientNode unknownClient = new ClientNode("10.0.0.1", 9999);
        assertFalse(networking.isClientAlive(unknownClient),
                "isClientAlive should return false for a client not in the topology.");
    }

    @Test
    public void testConsumeAndGetRPC_Mockito() {

        AbstractRPC mockRpc = mock(AbstractRPC.class);
        networking.consumeRPC(mockRpc);
        assertEquals(mockRpc, networking.getRPC(),
                "getRPC should return the AbstractRPC instance.");

        verify(mockRpc, times(1)).subscribe(eq("getNetworkRPCAddUser"), any());
        verify(mockRpc, times(1)).subscribe(eq("networkRPCBroadcast"), any());
        verify(mockRpc, times(1)).subscribe(eq("networkRPCRemoveSubscription"), any());
        verify(mockRpc, times(1)).subscribe(eq("networkRPCSendData"), any());
        verify(mockRpc, times(1)).subscribe(eq("networkRPCSubscribe"), any());
        verify(mockRpc, times(1)).subscribe(eq("networkRPCCloseNetworking"), any());

        verifyNoMoreInteractions(mockRpc);
    }

//    @Test
//    public void testIsMainServerLive_Success() throws Exception {
//
//        // We will make the first Socket successfully connect.
//        try (MockedConstruction<Socket> mockedSocket = mockConstruction(Socket.class)) {
//
//            // simulates a successful connection on the first attempt (8.8.8.8:53).
//            boolean isDead = networking.isMainServerLive();
//            assertFalse(isDead, "isMainServerLive should return false when connection succeeds.");
//            verify(mockedSocket.constructed().get(0), times(1)).connect(any(), eq(2000));
//        }
//    }
}