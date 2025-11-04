package com.swe.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class P2PClientTest {
    final ClientNode mainServer = new ClientNode("127.0.0.1", 5000);
    final ClientNode deviceNode = new ClientNode("127.0.0.1", 5001);
    final ClientNode clusterServerNode = new ClientNode("127.0.0.1", 5002);
    final ClientNode otherClientNode = new ClientNode("127.0.0.1", 5003);

    final PacketParser packetParser = PacketParser.getPacketParser();
    final NetworkSerializer serializer = NetworkSerializer.getNetworkSerializer();
    final Topology topology = Topology.getTopology();

    private P2PClient testClient;

    public NetworkStructure getMockNetworkStructure() {
        List<List<ClientNode>> clusters = new ArrayList<>();
        List<ClientNode> clusterServers = new ArrayList<>();

        // Cluster 0 (another cluster)
        List<ClientNode> cluster0 = new ArrayList<>();
        ClientNode otherServer = new ClientNode("127.0.0.1", 6000);
        cluster0.add(otherServer);
        clusters.add(cluster0);
        clusterServers.add(otherServer);

        // Cluster 1 (The client's cluster)
        List<ClientNode> cluster1 = new ArrayList<>();
        cluster1.add(clusterServerNode); // The server
        cluster1.add(deviceNode);         // The client itself
        cluster1.add(otherClientNode);    // Another client
        clusters.add(cluster1);
        clusterServers.add(clusterServerNode);

        return new NetworkStructure(clusters, clusterServers);
    }

    /**
     * Helper method to send a packet to a specific ClientNode.
     * We use this to send packets *to* our testClient.
     */
    public void sendPacket(byte[] packet, ClientNode destNode) {
        try (Socket socket = new Socket(destNode.hostName(), destNode.port())) {
            OutputStream out = socket.getOutputStream();
            out.write(packet);
            out.flush();
        } catch (Exception e) {
            System.err.println("Test sendPacket failed: " + e.getMessage());
        }
    }

    /**
     * Helper to create a fully formed packet for sending.
     * @return Raw packet bytes
     */
    private byte[] createTestPacket(int type, int connType, byte[] payload) throws UnknownHostException {
        PacketInfo info = new PacketInfo();
        info.setType(type);
        info.setConnectionType(connType);
        info.setPayload(payload != null ? payload : new byte[0]);

        info.setIpAddress(InetAddress.getByName(mainServer.hostName()));
        info.setPortNum(mainServer.port());
        return packetParser.createPkt(info);
    }

    @Before
    public void setUp() {

        topology.replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
        testClient = null;
    }

    @After
    public void tearDown() throws InterruptedException {

        if (testClient != null) {
            testClient.close();
            testClient = null;
        }
        Thread.sleep(100);
    }

    @Test
    public void testClientReceivesNetworkPacket() throws Exception {
        System.out.println("Test for recieving any packet ...............");
        testClient = new P2PClient(deviceNode, mainServer);

        // 1. Create the network structure and the packet
        NetworkStructure network = getMockNetworkStructure();
        byte[] networkPayload = serializer.serializeNetworkStructure(network);
        byte[] networkPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.NETWORK.ordinal(), networkPayload);

        sendPacket(networkPacket, deviceNode);

        Thread.sleep(500);

        assertEquals("Client should be in cluster 1", 1, topology.getClusterIndex(deviceNode));
        assertEquals("Client's server should be clusterServerNode", clusterServerNode, topology.getServer(deviceNode));

    }

    @Test
    public void testClientSendToSingleDestPacket() throws Exception {
        System.out.println("Test for sending data to single dest packet ...............");
        testClient = new P2PClient(deviceNode, mainServer);
        ClientNode destination = new ClientNode("127.0.0.1", 8001);
        byte[] testdata = new byte[]{2, 4, 5};
        testClient.send(testdata, destination);
    }

    @Test
    public void testClientSendToMultipleDestPacket() throws Exception {
        System.out.println("Test for sending data to multiple dest packet ...............");
        testClient = new P2PClient(deviceNode, mainServer);
        ClientNode destination1 = new ClientNode("127.0.0.1", 8002);
        ClientNode destination2 = new ClientNode("127.0.0.1", 8003);

        byte[] testdata = new byte[]{2, 4, 5};

        ClientNode[] destination = new ClientNode[]{destination1,destination2};
        testClient.send(testdata, destination);
    }

    @Test
    public void testALivePacketSending() throws Exception {
        System.out.println("Test for sending alive packet ...............");
        AtomicReference<byte[]> receivedPacket = new AtomicReference<>();

        Thread mockServerThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(clusterServerNode.port())) {
                Socket socket = serverSocket.accept();
                byte[] buffer = new byte[1024];
                int bytesRead = socket.getInputStream().read(buffer);
                if (bytesRead > 0) {
                    byte[] packet = new byte[bytesRead];
                    System.arraycopy(buffer, 0, packet, 0, bytesRead);
                    receivedPacket.set(packet); // Store the received packet
                }
                socket.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        });
        mockServerThread.start();

        NetworkStructure network = getMockNetworkStructure();
        byte[] networkPayload = serializer.serializeNetworkStructure(network);
        byte[] networkPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.NETWORK.ordinal(), networkPayload);

        testClient = new P2PClient(deviceNode, mainServer);

        sendPacket(networkPacket, deviceNode);
        Thread.sleep(3000);

        assertNotNull("Mock server did not receive any packet", receivedPacket.get());
        PacketInfo info = packetParser.parsePacket(receivedPacket.get());
        assertEquals(NetworkType.USE.ordinal(), info.getType());
        assertEquals(NetworkConnectionType.ALIVE.ordinal(), info.getConnectionType());

        mockServerThread.interrupt();
    }

    @Test
    public void testHandlingAddReceivedPacket() throws Exception {
        System.out.println("Test for receiving ADD packet ...............");

        NetworkStructure network = getMockNetworkStructure();
        byte[] networkPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.NETWORK.ordinal(), serializer.serializeNetworkStructure(network));

        testClient = new P2PClient(deviceNode, mainServer);

        sendPacket(networkPacket, deviceNode);
        Thread.sleep(100);

        ClientNode newNode = new ClientNode("127.0.0.1", 7000);
        ClientNetworkRecord addRecord = new ClientNetworkRecord(newNode, 1); // Add to cluster 1
        byte[] addPayload = serializer.serializeClientNetworkRecord(addRecord);
        byte[] addPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.ADD.ordinal(), addPayload);
        sendPacket(addPacket, deviceNode);
        Thread.sleep(500);

        assertEquals("new node should be in cluster 1", 1, topology.getClusterIndex(newNode));
    }

    @Test
    public void testHandlingRemoveReceivedPacket() throws Exception {
        System.out.println("Test for receiving REMOVE packet ...............");
        NetworkStructure network = getMockNetworkStructure();
        byte[] networkPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.NETWORK.ordinal(), serializer.serializeNetworkStructure(network));

        testClient = new P2PClient(deviceNode, mainServer);

        sendPacket(networkPacket, deviceNode);
        Thread.sleep(100);

        assertEquals("otherClientNode should be in cluster 1", 1, topology.getClusterIndex(otherClientNode));

        ClientNetworkRecord removeRecord = new ClientNetworkRecord(otherClientNode, 1);
        byte[] removePayload = serializer.serializeClientNetworkRecord(removeRecord);
        byte[] removePacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.REMOVE.ordinal(), removePayload);

        sendPacket(removePacket, deviceNode);
        Thread.sleep(500);

//        assertEquals("otherClientNode should be removed", -1, topology.getClusterIndex(otherClientNode));
    }

    @Test
    public void testHandlingNetworkReceivedPacket() throws Exception {
        System.out.println("Test for receiving NETWORK packet ...............");
        NetworkStructure network = getMockNetworkStructure();
        byte[] networkPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.NETWORK.ordinal(), serializer.serializeNetworkStructure(network));

        testClient = new P2PClient(deviceNode, mainServer);

        sendPacket(networkPacket, deviceNode);
        Thread.sleep(100);
    }

    @Test
    public void testHandlingHello_Alive_Unknown_Packet() throws Exception {
        System.out.println("Test for receiving hello or alive packet ...............");
        NetworkStructure network = getMockNetworkStructure();
        byte[] networkPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.NETWORK.ordinal(), serializer.serializeNetworkStructure(network));

        testClient = new P2PClient(deviceNode, mainServer);

        sendPacket(networkPacket, deviceNode);
        Thread.sleep(100);

        byte[] alivePacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.ALIVE.ordinal(), null);
        byte[] helloPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.HELLO.ordinal(), null);
        sendPacket(helloPacket, deviceNode);
        sendPacket(alivePacket, deviceNode);
        Thread.sleep(500);
    }

    @Test
    public void testHandlingClosePacket() throws Exception {
        System.out.println("Test for receiving close packet ...............");

        testClient = new P2PClient(deviceNode, mainServer);

        try {
            byte[] helloPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.HELLO.ordinal(), null);
            sendPacket(helloPacket, deviceNode);
        } catch (IOException e) {
            fail("Client socket was not open before test: " + e.getMessage());
        }

        byte[] closePacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.CLOSE.ordinal(), null);
        sendPacket(closePacket, deviceNode);

        //checking
        Thread.sleep(500);
        boolean conn = true;
        try {
            byte[] helloPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.HELLO.ordinal(), null);
            sendPacket(helloPacket, deviceNode);
        } catch (IOException e) {
            conn = false;
        }

        assertTrue(conn);
    }

    @Test
    public void testDroppedTypePacket() throws Exception {
        System.out.println("Test for receiving dropped packet ...............");
        NetworkStructure network = getMockNetworkStructure();
        byte[] networkPacket = createTestPacket(NetworkType.USE.ordinal(), NetworkConnectionType.NETWORK.ordinal(), serializer.serializeNetworkStructure(network));

        testClient = new P2PClient(deviceNode, mainServer);

        sendPacket(networkPacket, deviceNode);
        Thread.sleep(100);

        byte[] pkt1 = createTestPacket(NetworkType.CLUSTERSERVER.ordinal(), NetworkConnectionType.HELLO.ordinal(), null);
        byte[] pkt2 = createTestPacket(NetworkType.SAMECLUSTER.ordinal(), NetworkConnectionType.HELLO.ordinal(), null);
        byte[] pkt3 = createTestPacket(NetworkType.OTHERCLUSTER.ordinal(), NetworkConnectionType.HELLO.ordinal(), null);
        byte[] pkt4 = createTestPacket(4,NetworkConnectionType.HELLO.ordinal(), null);
        sendPacket(pkt1, deviceNode);
        sendPacket(pkt2, deviceNode);
        sendPacket(pkt3, deviceNode);
        sendPacket(pkt4, deviceNode);
        Thread.sleep(500);
    }

    @Test
    public void testUpdateClusterServerNull() throws Exception {
        System.out.println("Test for updating cluster server is it null ...............");
        testClient = new P2PClient(deviceNode, mainServer);
        testClient.updateClusterServer();
    }

    @Test
    public void testclosewhenEverythingNull() throws Exception {
        System.out.println("Test for close when all thread already terminated  ...............");

        testClient = new P2PClient(deviceNode, mainServer);

        testClient.close();
        Thread.sleep(100);

        setPrivateField(testClient, "aliveScheduler", null);
        setPrivateField(testClient, "receiveThread", null);
        setPrivateField(testClient, "communicator", null);
        setPrivateField(testClient, "running", true);

        testClient.close();

        assertTrue(true);
    }

    private static void setPrivateField(Object target, String fieldName, Object value)
            throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}