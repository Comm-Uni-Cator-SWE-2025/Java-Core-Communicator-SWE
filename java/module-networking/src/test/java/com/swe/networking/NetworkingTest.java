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


import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
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
        System.out.println("Test data: " + new String(receivedDataRef.get()));
        assertNotNull(receivedDataRef.get(), "Listener was not called");
        assertArrayEquals(testData, receivedDataRef.get(), "Listener received incorrect data");
    }

    @Test
    public void testCallSubscriber_NoFunction() {
        assertDoesNotThrow(() -> networking.callSubscriber(ModuleType.CHAT.ordinal() + 99, "dummy".getBytes()));
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
    public void testSendData_NullDestination() {
        byte[] testData = "Should be ignored".getBytes();
        ClientNode[] nullDest = null;

        assertDoesNotThrow(() -> networking.sendData(testData, nullDest, ModuleType.CANVAS.ordinal(), 1));
        assertTrue(priorityQueue.isEmpty(), "Queue must remain empty when destination is null.");
    }

    @Test
    public void testSendData_CoverInternalCatch() throws Exception {

        PacketParser mockParser = mock(PacketParser.class);
        resetStaticSingleton(PacketParser.class, "parser", mockParser); // Reset static singleton

        Field parserField = Networking.class.getDeclaredField("parser");
        parserField.setAccessible(true);
        parserField.set(networking, mockParser);

        topology.addClient(clientNode); // clientNode = 127.0.0.1:8001
        byte[] testData = "test data".getBytes();


        doThrow(new UnknownHostException("Forcing sendData's catch block"))
                .when(mockParser).parsePacket(any(byte[].class));

        assertDoesNotThrow(() -> {
            networking.sendData(testData, new ClientNode[]{clientNode}, ModuleType.CANVAS.ordinal(), 1);
        }, "sendData should catch the UnknownHostException thrown by the parser mock.");

        verify(mockParser, atLeastOnce()).parsePacket(any(byte[].class));
    }

    @Test
    public void testBroadcast_ServerSender() {
        topology.addClient(clientNode);
        byte[] data = "server broadcast".getBytes();
        assertDoesNotThrow(() -> networking.broadcast(data, ModuleType.UIUX.ordinal(), 2));
    }

//    @Test
    public void testBroadcast_RegularClientSender() {
        topology.addClient(clientNode);
        networking.addUser(clientNode, serverNode);
        byte[] data = "client broadcast".getBytes();
        assertDoesNotThrow(() -> networking.broadcast(data, ModuleType.CHAT.ordinal(), 1));
    }

//    @Test
    public void testStart() throws Exception {
        Topology mockTopology = mock(Topology.class);
        PacketParser mockParser = mock(PacketParser.class);
        NewPriorityQueue mockQueue = mock(NewPriorityQueue.class);

        // Inject Topology
        Field topologyField = Networking.class.getDeclaredField("topology");
        topologyField.setAccessible(true);
        topologyField.set(networking, mockTopology);

        // Inject PacketParser
        Field parserField = Networking.class.getDeclaredField("parser");
        parserField.setAccessible(true);
        parserField.set(networking, mockParser);

        // Inject NewPriorityQueue
        Field queueField = Networking.class.getDeclaredField("priorityQueue");
        queueField.setAccessible(true);
        queueField.set(networking, mockQueue);

        final ClientNode destNode = new ClientNode("1.1.1.1", 12345);
        final byte[] testPacket = "test_data".getBytes();
        final PacketInfo mockPacketInfo = new PacketInfo();

        mockPacketInfo.setIpAddress(InetAddress.getByName(destNode.hostName()));
        mockPacketInfo.setPortNum(destNode.port());

        when(mockQueue.isEmpty()).thenReturn(false, true);
        when(mockQueue.getPacket()).thenReturn(testPacket);
        when(mockParser.parsePacket(testPacket)).thenReturn(mockPacketInfo);

        Thread executionThread = new Thread(() -> {
            networking.start();
        });

        executionThread.start();
        Thread.sleep(100);
        executionThread.interrupt();
        executionThread.join(1000);
        verify(mockQueue, times(1)).getPacket();
        verify(mockParser, times(1)).parsePacket(testPacket);
        verify(mockTopology, times(1)).sendPacket(eq(testPacket), eq(destNode));
        assertFalse(executionThread.isAlive(), "The execution thread should have terminated after interruption.");
    }

//    @Test
    public void testStart_UnknownHostException() throws Exception {

        Topology mockTopology = mock(Topology.class);
        PacketParser mockParser = mock(PacketParser.class);
        NewPriorityQueue mockQueue = mock(NewPriorityQueue.class);

        resetStaticSingleton(Topology.class, "topology", mockTopology);
        resetStaticSingleton(PacketParser.class, "parser", mockParser);
        resetStaticSingleton(NewPriorityQueue.class, "priorityQueue", mockQueue);

        Field topologyField = Networking.class.getDeclaredField("topology");
        topologyField.setAccessible(true);
        topologyField.set(networking, mockTopology);

        Field parserField = Networking.class.getDeclaredField("parser");
        parserField.setAccessible(true);
        parserField.set(networking, mockParser);

        Field queueField = Networking.class.getDeclaredField("priorityQueue");
        queueField.setAccessible(true);
        queueField.set(networking, mockQueue);

        when(mockQueue.isEmpty()).thenReturn(false, false, true);
        when(mockQueue.getPacket()).thenReturn("dummy".getBytes());

        doThrow(new UnknownHostException("Simulated host resolution failure"))
                .when(mockParser).parsePacket(any(byte[].class));

        Thread executionThread = new Thread(() -> {
            networking.start();
        });

        executionThread.start();
        Thread.sleep(100);

        executionThread.interrupt();
        executionThread.join(1000);

        verify(mockParser, times(2)).parsePacket(any(byte[].class));
        verify(mockTopology, never()).sendPacket(any(byte[].class), any(ClientNode.class));
        assertFalse(executionThread.isAlive(), "The execution thread should have terminated after interruption.");
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

    @Test
    public void testIsMainServerLive_Success() throws Exception {

        // We will make the first Socket successfully connect.
        try (MockedConstruction<Socket> mockedSocket = mockConstruction(Socket.class)) {
            // simulates a successful connection on the first attempt (8.8.8.8:53).
            boolean isDead = networking.isMainServerLive();
            assertFalse(isDead, "isMainServerLive should return false when connection succeeds.");
            verify(mockedSocket.constructed().get(0), times(1)).connect(any(), eq(2000));
        }
    }

    @Test
    public void testIsMainServerLive_Failure() throws Exception {
        final int totalAttempts = 8; // 4 DNS servers * 2 ports

        try (MockedConstruction<Socket> mockedSocket = mockConstruction(Socket.class, (mock, context) -> {
            doThrow(new IOException("Simulated network failure")).when(mock).connect(any(), eq(2000));
        })) {
            boolean isDead = networking.isMainServerLive();
            assertTrue(isDead, "isMainServerLive should return true when all connection attempts fail.");
            assertEquals(totalAttempts, mockedSocket.constructed().size(),
                    "The method should attempt to connect exactly 8 times (4 servers * 2 ports).");
            for (Socket mock : mockedSocket.constructed()) {
                verify(mock, times(1)).connect(any(), eq(2000));
            }
        }
    }
}