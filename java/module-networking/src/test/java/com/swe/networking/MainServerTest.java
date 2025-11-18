package com.swe.networking;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Test class for MainServer.
 */
public class MainServerTest {

    private <T> T invokePrivateMethod(Object object, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(object, parameters);
    }

    /**
     * Function to test send Function.
     */
    @org.junit.jupiter.api.Test
    public void testSend() {
        try {
            final int serverport1 = 8001;
            final int serverport2 = 8002;
            final int serverport3 = 8003;
            final int serverport4 = 8004;
            final Thread recieveThread1 = new Thread(() -> receive(serverport1));
            final Thread recieveThread2 = new Thread(() -> receive(serverport2));
            final Thread recieveThread3 = new Thread(() -> receive(serverport3));
            final Thread recieveThread4 = new Thread(() -> receive(serverport4));
            recieveThread1.start();
            recieveThread2.start();
            recieveThread3.start();
            recieveThread4.start();
            final Integer sleepTime = 500;
            Thread.sleep(sleepTime);
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final MainServer mainServer = new MainServer(server, server);
            final ClientNode dest1 = new ClientNode("127.0.0.1", 8001);
            final ClientNode dest2 = new ClientNode("127.0.0.1", 8002);
            final String data = "Good morning !!!";
            mainServer.send(data.getBytes(), dest1);
            mainServer.send(data.getBytes(), dest1);
            mainServer.send(data.getBytes(), dest1);
            mainServer.send(data.getBytes(), dest2);
            mainServer.closeClient(dest1);
            mainServer.closeClient(dest2);
            Thread.sleep(sleepTime);
            final ClientNode dest3 = new ClientNode("127.0.0.1", 8003);
            final ClientNode dest4 = new ClientNode("127.0.0.1", 8004);
            final ClientNode[] clients = { dest3, dest4 };
            mainServer.send(data.getBytes(), clients);
            mainServer.closeClient(dest3);
            mainServer.closeClient(dest4);
            recieveThread1.join();
            recieveThread2.join();
            recieveThread3.join();
            recieveThread4.join();
            mainServer.close();
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Test function to check send is working.
     * 
     * @param serverPort to set the port of server
     */
    public void receive(final int serverPort) {
        try {
            final ServerSocket serverSocket = new ServerSocket(serverPort);
            serverSocket.setSoTimeout(0);
            final Socket socket = serverSocket.accept();
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final byte[] packet = dataIn.readAllBytes();
            System.out.println("Client port:" + serverPort + " data : " + new String(packet));
            System.out.println("Data received successfully...");
            serverSocket.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Function to test the receive functionality.
     */
    @org.junit.jupiter.api.Test
    public void testReceive() {
        try {
            final int sleepTime = 1000;
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final MainServer mainServer = new MainServer(server, server);
            send();
            Thread.sleep(sleepTime);
            mainServer.close();
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Test function to send data.
     */
    public void send() {
        try {
            final Socket destSocket = new Socket();
            final Integer port = 8000;
            final Integer timeout = 5000;
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final ClientNode client = new ClientNode("127.0.0.1", destSocket.getLocalPort());
            final byte[] packet = getHelloPacket(client);
            dataOut.write(packet);
            destSocket.close();
        } catch (IOException ex) {
        }
    }

    public byte[] getRemovePacket(final ClientNode client) {
        try {
            final Topology topology = Topology.getTopology();
            final int randomFactor = (int) Math.pow(10, 6);
            final PacketParser parser = PacketParser.getPacketParser();
            final PacketInfo pkt = new PacketInfo();
            final int packetHeaderSize = 22;
            pkt.setLength(packetHeaderSize);
            pkt.setType(NetworkType.USE.ordinal());
            pkt.setPriority(0);
            pkt.setModule(0);
            pkt.setConnectionType(NetworkConnectionType.REMOVE.ordinal());
            pkt.setBroadcast(0);
            pkt.setIpAddress(InetAddress.getByName(client.hostName()));
            pkt.setPortNum(client.port());
            pkt.setMessageId((int) (Math.random() * randomFactor));
            pkt.setChunkNum(0);
            pkt.setChunkLength(1);
            final int idx = topology.getClusterIndex(client);
            final ClientNetworkRecord record = new ClientNetworkRecord(client, idx);
            pkt.setPayload(NetworkSerializer.getNetworkSerializer().serializeClientNetworkRecord(record));
            final byte[] packet = parser.createPkt(pkt);
            return packet;
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    public byte[] getHelloPacket(final ClientNode client) {
        try {
            final int randomFactor = (int) Math.pow(10, 6);
            final PacketParser parser = PacketParser.getPacketParser();
            final PacketInfo pkt = new PacketInfo();
            final int packetHeaderSize = 22;
            final String data = "Hello Sekai !!!";
            pkt.setLength(packetHeaderSize + data.length());
            pkt.setType(NetworkType.USE.ordinal());
            pkt.setPriority(0);
            pkt.setModule(0);
            pkt.setConnectionType(NetworkConnectionType.HELLO.ordinal());
            pkt.setBroadcast(0);
            pkt.setIpAddress(InetAddress.getByName(client.hostName()));
            pkt.setPortNum(client.port());
            pkt.setMessageId((int) (Math.random() * randomFactor));
            pkt.setChunkNum(0);
            pkt.setChunkLength(1);
            pkt.setPayload(data.getBytes());
            final byte[] packet = parser.createPkt(pkt);
            return packet;
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    @org.junit.jupiter.api.Test
    public void testMainServerTimeout() {
        final ClientNode server = new ClientNode("127.0.0.1", 8000);
        final MainServer mainServer = new MainServer(server, server);
        timeoutsend();
    }

    public void timeoutsend() {
        try {
            final Socket destSocket = new Socket();
            final Integer port = 8000;
            final Integer timeout = 5000;
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final ClientNode client = new ClientNode("127.0.0.1", destSocket.getLocalPort());
            final byte[] packet = getHelloPacket(client);
            dataOut.write(packet);
            Thread.sleep(6000);
        } catch (IOException | InterruptedException ex) {
        }
    }

    /**
     * A receiver method that listens on a port, prints the received message,
     * and can be safely interrupted by closing the socket.
     * @param port The port number to listen on.
     * @param serverSocketToClose The ServerSocket to close for shutdown.
     */
    public void stoppableReceive(final int port, ServerSocket[] serverSocketToClose) {
        try {
            final ServerSocket serverSocket = new ServerSocket(port);
            serverSocketToClose[0] = serverSocket;
            serverSocket.setSoTimeout(5000);
            final Socket socket = serverSocket.accept();
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final byte[] packet = dataIn.readAllBytes();
            System.out.println("Client port:" + port + " data : " + new String(packet));
            System.out.println("Data received successfully...");
            socket.close();
            serverSocket.close();
        } catch (IOException ex) {
            System.out.println("Receiver on port " + port + " was interrupted or timed out.");
        }
    }

    private NetworkStructure createTopology(ClientNode mainServer, ClientNode... additionalClients) {
        List<ClientNode> cluster = new ArrayList<>();
        cluster.add(mainServer);
        for (ClientNode c : additionalClients) cluster.add(c);
        List<List<ClientNode>> clusters = new ArrayList<>();
        clusters.add(cluster);
        List<ClientNode> servers = new ArrayList<>();
        servers.add(mainServer);
        return new NetworkStructure(clusters, servers);
    }

    private NetworkStructure createMultiClusterTopology(ClientNode mainServer, List<ClientNode> otherCluster, ClientNode otherServer) {
        List<ClientNode> mainCluster = new ArrayList<>();
        mainCluster.add(mainServer);
        List<List<ClientNode>> clusters = new ArrayList<>();
        clusters.add(mainCluster);
        if (otherCluster != null) clusters.add(otherCluster);
        List<ClientNode> servers = new ArrayList<>();
        servers.add(mainServer);
        if (otherServer != null) servers.add(otherServer);
        return new NetworkStructure(clusters, servers);
    }

    private void cleanup(MainServer mainServer, ServerSocket[]... sockets) {
        if (mainServer != null) mainServer.close();
        for (ServerSocket[] sockArr : sockets) {
            if (sockArr[0] != null && !sockArr[0].isClosed()) {
                try { sockArr[0].close(); } catch (IOException e) {}
            }
        }
    }

    private void cleanupThreads(Thread... threads) {
        for (Thread t : threads) {
            if (t != null && t.isAlive()) t.interrupt();
        }
    }

    private PacketInfo createPacketInfo(int type, int connectionType, ClientNode dest, byte[] payload) throws UnknownHostException {
        PacketInfo pkt = new PacketInfo();
        pkt.setLength(22 + (payload != null ? payload.length : 0));
        pkt.setType(type);
        pkt.setConnectionType(connectionType);
        pkt.setIpAddress(InetAddress.getByName(dest.hostName()));
        pkt.setPortNum(dest.port());
        if (payload != null) pkt.setPayload(payload);
        return pkt;
    }

    /**
     * Tests routing functionality.
     */
    @org.junit.jupiter.api.Test
    public void testRouting() {
        System.out.println("Testing routing...");
        final int mainServerPort = 8020;
        final int otherServerPort = 8021;
        final int clientPort = 9020;
        final ServerSocket[] otherServerSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread otherServerThread = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            final ClientNode otherServerNode = new ClientNode("127.0.0.1", otherServerPort);
            final ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);

            // Reset and manually set up topology
            Topology.getTopology().replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
            Topology.getTopology().addClient(mainServerNode);
            Topology.getTopology().addClient(otherServerNode);
            Topology.getTopology().addClient(clientNode);

            // Start a listener for the "other server"
            otherServerThread = new Thread(() -> stoppableReceive(otherServerPort, otherServerSock));
            otherServerThread.start();

            // Start the main server
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500); // Give server time to start

            // Create and send a packet destined for a client in another cluster
            PacketInfo packetInfo = new PacketInfo();
            packetInfo.setLength(22);
            packetInfo.setType(NetworkType.OTHERCLUSTER.ordinal());
            packetInfo.setConnectionType(NetworkConnectionType.HELLO.ordinal());
            packetInfo.setIpAddress(InetAddress.getByName(clientNode.hostName()));
            packetInfo.setPortNum(clientNode.port());
            packetInfo.setPayload("Routing Test".getBytes());
            byte[] packet = PacketParser.getPacketParser().createPkt(packetInfo);

            // Send packet to main server, which should route it to otherServerNode
            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.write(packet);
                dataOut.flush();
            }

            otherServerThread.join(2000); // Wait for the thread to finish, with a timeout

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Teardown
            if (mainServer != null) {
                mainServer.close();
            }
            if (otherServerSock[0] != null && !otherServerSock[0].isClosed()) {
                try {
                    otherServerSock[0].close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (otherServerThread != null && otherServerThread.isAlive()) {
                otherServerThread.interrupt();
            }
            System.out.println("Finished routing test.");
        }
    }

    /**
     * Tests broadcast functionality.
     */
    @org.junit.jupiter.api.Test
    public void testBroadcast() {
        System.out.println("Testing broadcast...");
        final int mainServerPort = 8030;
        final int serverBPort = 8031;
        final int serverCPort = 8032;
        final ServerSocket[] serverBSock = new ServerSocket[1];
        final ServerSocket[] serverCSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread serverBThread = null;
        Thread serverCThread = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            final ClientNode serverBNode = new ClientNode("127.0.0.1", serverBPort);
            final ClientNode serverCNode = new ClientNode("127.0.0.1", serverCPort);

            // Reset and manually set up topology
            Topology.getTopology().replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
            Topology.getTopology().addClient(mainServerNode);
            Topology.getTopology().addClient(serverBNode);
            Topology.getTopology().addClient(serverCNode);

            // Start listeners for other servers
            serverBThread = new Thread(() -> stoppableReceive(serverBPort, serverBSock));
            serverCThread = new Thread(() -> stoppableReceive(serverCPort, serverCSock));
            serverBThread.start();
            serverCThread.start();

            // Start the main server
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);

            // Create and send a broadcast packet
            PacketInfo packetInfo = new PacketInfo();
            packetInfo.setLength(22);
            packetInfo.setType(NetworkType.USE.ordinal());
            packetInfo.setConnectionType(NetworkConnectionType.HELLO.ordinal());
            packetInfo.setIpAddress(InetAddress.getByName(mainServerNode.hostName()));
            packetInfo.setPortNum(mainServerNode.port());
            packetInfo.setBroadcast(1); // Set broadcast flag
            packetInfo.setPayload("Broadcast Test".getBytes());
            byte[] packet = PacketParser.getPacketParser().createPkt(packetInfo);

            // Send packet to main server
            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.write(packet);
                dataOut.flush();
            }

            serverBThread.join(2000);
            serverCThread.join(2000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Teardown
            if (mainServer != null) {
                mainServer.close();
            }
            // Close server sockets
            for (ServerSocket sock : new ServerSocket[]{serverBSock[0], serverCSock[0]}) {
                if (sock != null && !sock.isClosed()) {
                    try {
                        sock.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // Interrupt threads if still alive
            for (Thread t : new Thread[]{serverBThread, serverCThread}) {
                if (t != null && t.isAlive()) {
                    t.interrupt();
                }
            }
            System.out.println("Finished broadcast test.");
        }
    }

    /**
     * Tests routing to a client within the same cluster.
     */
    @org.junit.jupiter.api.Test
    public void testSameClusterRouting() {
        System.out.println("Testing same-cluster routing...");
        final int mainServerPort = 8040;
        final int clientPort = 9040;
        final ServerSocket[] clientSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread clientThread = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            final ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);

            // Reset and manually set up topology
            Topology.getTopology().replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
            Topology.getTopology().addClient(mainServerNode);
            Topology.getTopology().addClient(clientNode); // Client in the same cluster

            // Start a listener for the client
            clientThread = new Thread(() -> stoppableReceive(clientPort, clientSock));
            clientThread.start();

            // Start the main server
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);

            // Create and send a packet destined for the client
            PacketInfo packetInfo = new PacketInfo();
            packetInfo.setLength(22);
            packetInfo.setType(NetworkType.SAMECLUSTER.ordinal());
            packetInfo.setConnectionType(NetworkConnectionType.HELLO.ordinal());
            packetInfo.setIpAddress(InetAddress.getByName(clientNode.hostName()));
            packetInfo.setPortNum(clientNode.port());
            packetInfo.setPayload("Same Cluster Routing".getBytes());
            byte[] packet = PacketParser.getPacketParser().createPkt(packetInfo);

            // Send packet to main server
            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.write(packet);
                dataOut.flush();
            }

            clientThread.join(2000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Teardown
            if (mainServer != null) {
                mainServer.close();
            }
            if (clientSock[0] != null && !clientSock[0].isClosed()) {
                try {
                    clientSock[0].close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (clientThread != null && clientThread.isAlive()) {
                clientThread.interrupt();
            }
            System.out.println("Finished same-cluster routing test.");
        }
    }

    /**
     * Tests that the server remains stable after receiving a malformed packet.
     */
    @org.junit.jupiter.api.Test
    public void testMalformedPacket() {
        System.out.println("Testing malformed packet...");
        final int mainServerPort = 8050;
        MainServer mainServer = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            Topology.getTopology().replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);

            // Send a malformed packet
            byte[] malformedPacket = new byte[]{1, 2, 3};
            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.write(malformedPacket);
                dataOut.flush();
            }
            System.out.println("Sent a malformed packet.");
            Thread.sleep(500); // Give server time to process and fail

            // Send a valid packet to see if the server is still alive
            System.out.println("Sending a valid HELLO packet to check server status...");
            try (Socket testSocket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream out = new DataOutputStream(testSocket.getOutputStream());
                DataInputStream in = new DataInputStream(testSocket.getInputStream());

                byte[] helloPacket = getHelloPacket(new ClientNode("127.0.0.1", testSocket.getLocalPort()));
                out.write(helloPacket);

                // The server should respond with a NETWORK packet.
                testSocket.setSoTimeout(2000);
                byte[] response = in.readAllBytes();
                if (response.length > 0) {
                    System.out.println("Server responded to valid packet after malformed one. Server is stable.");
                } else {
                    System.out.println("Server did NOT respond after malformed packet.");
                }
            }

        } catch (Exception e) {
            // If this exception happens, it might mean the server died.
            System.out.println("An exception occurred during the test, possibly because the server is down.");
            e.printStackTrace();
        } finally {
            // 4. Teardown
            if (mainServer != null) {
                mainServer.close();
            }
            System.out.println("Finished malformed packet test.");
        }
    }
    /**
     * Test for routing functionality to a client in another cluster.
     */
    @org.junit.jupiter.api.Test
    public void testRoutingToOtherCluster() {
        System.out.println("Testing another routing to other cluster...");
        final int mainServerPort = 8060;
        final int otherServerPort = 8061;
        final int clientPort = 9060;
        final ServerSocket[] otherServerSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread otherServerThread = null;

        try {
            //Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            final ClientNode otherServerNode = new ClientNode("127.0.0.1", otherServerPort);
            final ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);

            // Reset and manually set up topology
            Topology.getTopology().replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
            Topology.getTopology().addClient(mainServerNode);
            Topology.getTopology().addClient(otherServerNode);
            Topology.getTopology().addClient(clientNode);

            // Start a listener for the "other server"
            otherServerThread = new Thread(() -> stoppableReceive(otherServerPort, otherServerSock));
            otherServerThread.start();

            // Start the main server
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500); // Give server time to start

            // Create and send a packet destined for a client in another cluster
            PacketInfo packetInfo = new PacketInfo();
            packetInfo.setLength(22);
            packetInfo.setType(NetworkType.OTHERCLUSTER.ordinal());
            packetInfo.setConnectionType(NetworkConnectionType.HELLO.ordinal());
            packetInfo.setIpAddress(InetAddress.getByName(clientNode.hostName()));
            packetInfo.setPortNum(clientNode.port());
            packetInfo.setPayload("Another Routing Test".getBytes());
            byte[] packet = PacketParser.getPacketParser().createPkt(packetInfo);

            // Send packet to main server, which should route it to otherServerNode
            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.write(packet);
                dataOut.flush();
            }

            otherServerThread.join(2000); // Wait for the thread to finish, with a timeout

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Teardown
            if (mainServer != null) {
                mainServer.close();
            }
            if (otherServerSock[0] != null && !otherServerSock[0].isClosed()) {
                try {
                    otherServerSock[0].close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (otherServerThread != null && otherServerThread.isAlive()) {
                otherServerThread.interrupt();
            }
                        System.out.println("Finished another routing to other cluster test.");
                    }
                }
                /**
                 * Tests the server's ability to handle a client removal notification.
                 */
                @org.junit.jupiter.api.Test
                public void testHandleRemove() {
                    System.out.println("Testing handle remove...");
                    final int mainServerPort = 8070;
                    final int clientAPort = 9070;
                    final int clientBPort = 9071;
                    final ServerSocket[] clientBSock = new ServerSocket[1];
                    MainServer mainServer = null;
                    Thread clientBThread = null;
            
                    try {
                        // Setup
                        final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
                        final ClientNode clientANode = new ClientNode("127.0.0.1", clientAPort);
                        final ClientNode clientBNode = new ClientNode("127.0.0.1", clientBPort);
            
                        // Reset and manually set up topology
                        Topology topology = Topology.getTopology();
                        topology.replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
                        topology.addClient(mainServerNode);
                        topology.addClient(clientANode);
                        topology.addClient(clientBNode);
            
                        // Start a listener for Client B to verify it receives the remove message
                        clientBThread = new Thread(() -> stoppableReceive(clientBPort, clientBSock));
                        clientBThread.start();
            
                        // Start the main server
                        mainServer = new MainServer(mainServerNode, mainServerNode);
                        Thread.sleep(500);
            
                        // Create and send a REMOVE packet for Client A
                        byte[] removePacket = getRemovePacket(clientANode);
            
                        // Send packet to main server
                        try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                            dataOut.write(removePacket);
                            dataOut.flush();
                        }
            
                        clientBThread.join(2000); // Wait for Client B to receive the broadcast
            
                        // Verify Client A was removed from the topology
                        if (topology.getClusterIndex(clientANode) == -1) {
                            System.out.println("Client A successfully removed from topology.");
                        } else {
                            System.out.println("Client A was NOT removed from topology.");
                        }
            
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // Teardown
                        if (mainServer != null) {
                            mainServer.close();
                        }
                        if (clientBSock[0] != null && !clientBSock[0].isClosed()) {
                            try {
                                clientBSock[0].close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (clientBThread != null && clientBThread.isAlive()) {
                            clientBThread.interrupt();
                        }
                                    System.out.println("Finished handle remove test.");
                                }
                            }
                            /**
                             * A simple listener to capture messages for testing.
                             */
                            class TestMessageListener implements MessageListener {
                                private byte[] receivedData = null;
                        
                                @Override
                                public void receiveData(byte[] data) {
                                    this.receivedData = data;
                                    System.out.println("TestMessageListener received data.");
                                }
                        
                                public byte[] getReceivedData() {
                                    return receivedData;
                                }
                            }
                            /**
                             * Tests the server's handling of MODULE type packets via the ChunkManager.
                             */
                            @org.junit.jupiter.api.Test
                            public void testModulePacketHandling() {
                                System.out.println("Testing MODULE packet handling...");
                                final int mainServerPort = 8080;
                                MainServer mainServer = null;
                        
                                try {
                                    // Setup
                                    final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
                                    Topology.getTopology().replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
                                    mainServer = new MainServer(mainServerNode, mainServerNode);
                        
                                    // Register a listener to catch the output
                                    TestMessageListener listener = new TestMessageListener();
                                    Networking.getNetwork().subscribe(ModuleType.NETWORKING.ordinal(), listener);
                        
                                    Thread.sleep(500);
                        
                                    // Create a MODULE packet
                                    String testPayload = "This is the module payload.";
                                    PacketInfo packetInfo = new PacketInfo();
                                    packetInfo.setLength(22 + testPayload.getBytes().length);
                                    packetInfo.setType(NetworkType.USE.ordinal());
                                    packetInfo.setConnectionType(NetworkConnectionType.MODULE.ordinal());
                                    packetInfo.setModule(ModuleType.NETWORKING.ordinal());
                                    packetInfo.setIpAddress(InetAddress.getByName("127.0.0.1"));
                                    packetInfo.setPortNum(9080);
                                    packetInfo.setPayload(testPayload.getBytes());
                                    packetInfo.setChunkNum(0); // First and only chunk
                                    packetInfo.setChunkLength(1);
                                    byte[] modulePacket = PacketParser.getPacketParser().createPkt(packetInfo);
                        
                                    // Send the packet
                                    try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                                        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                                        dataOut.write(modulePacket);
                                        dataOut.flush();
                                    }
                        
                                    Thread.sleep(1000); // Give server time to process
                        
                                    // Verification
                                    if (listener.getReceivedData() != null && new String(listener.getReceivedData()).equals(testPayload)) {
                                        System.out.println("Successfully received and processed MODULE packet.");
                                    } else {
                                        System.out.println("Failed to process MODULE packet correctly.");
                                    }
                        
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    // Teardown
                                    if (mainServer != null) {
                                        mainServer.close();
                                    }
                                                System.out.println("Finished MODULE packet handling test.");
                                            }
                                        }
                                        /**
                                         * Tests the server's handling of an OTHERCLUSTER broadcast.
                                         */
                                        @org.junit.jupiter.api.Test
                                        public void testOtherClusterBroadcast() {
                                            System.out.println("Testing OTHERCLUSTER broadcast...");
                                            final int mainServerPort = 8090;
                                            final int clientPort = 9090;
                                            final ServerSocket[] clientSock = new ServerSocket[1];
                                            MainServer mainServer = null;
                                            Thread clientThread = null;
                                    
                                            try {
                                                // Setup
                                                final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
                                                final ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);
                                                final ClientNode otherServerNode = new ClientNode("127.0.0.1", 8091); // Does not need to be listening
                                    
                                                // Reset and manually set up topology
                                                Topology topology = Topology.getTopology();
                                                topology.replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
                                                topology.addClient(mainServerNode);
                                                topology.addClient(clientNode);
                                                topology.addClient(otherServerNode); // Belongs to a different cluster
                                    
                                                // Start a listener for the client
                                                clientThread = new Thread(() -> stoppableReceive(clientPort, clientSock));
                                                clientThread.start();
                                    
                                                // Start the main server
                                                mainServer = new MainServer(mainServerNode, mainServerNode);
                                                Thread.sleep(500);
                                    
                                                // Create an OTHERCLUSTER broadcast packet
                                                PacketInfo packetInfo = new PacketInfo();
                                                packetInfo.setLength(22);
                                                packetInfo.setType(NetworkType.OTHERCLUSTER.ordinal());
                                                packetInfo.setConnectionType(NetworkConnectionType.HELLO.ordinal());
                                                packetInfo.setIpAddress(InetAddress.getByName(otherServerNode.hostName())); // Pretend it's from the other server
                                                packetInfo.setPortNum(otherServerNode.port());
                                                packetInfo.setBroadcast(1);
                                                packetInfo.setPayload("Cross-cluster broadcast".getBytes());
                                                byte[] packet = PacketParser.getPacketParser().createPkt(packetInfo);
                                    
                                                // Send the packet to the main server
                                                try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                                                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                                                    dataOut.write(packet);
                                                    dataOut.flush();
                                                }
                                    
                                                clientThread.join(2000); // Wait for the client to receive the forwarded packet
                                    
                                                // Verification happens in the stoppableReceive method, which prints to console.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            } finally {
                                                // Teardown
                                                if (mainServer != null) {
                                                    mainServer.close();
                                                }
                                                if (clientSock[0] != null && !clientSock[0].isClosed()) {
                                                    try {
                                                        clientSock[0].close();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                if (clientThread != null && clientThread.isAlive()) {
                                                    clientThread.interrupt();
                                                }
                                                            System.out.println("Finished OTHERCLUSTER broadcast test.");
                                                        }
                                                    }
                                                    /**
                                                     * Tests the server's stability when receiving a packet of an unknown type.
                                                     */
                                                    @org.junit.jupiter.api.Test
                                                    public void testUnknownPacketType() {
                                                        System.out.println("Testing unknown packet type...");
                                                        final int mainServerPort = 8100;
                                                        MainServer mainServer = null;
                                                
                                                        try {
                                                            // Setup
                                                            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
                                                            Topology.getTopology().replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
                                                            mainServer = new MainServer(mainServerNode, mainServerNode);
                                                            Thread.sleep(500);
                                                
                                                            // Create a packet with an invalid type
                                                            PacketInfo packetInfo = new PacketInfo();
                                                            packetInfo.setLength(22);
                                                            packetInfo.setType(99); // Invalid type
                                                            packetInfo.setConnectionType(NetworkConnectionType.HELLO.ordinal());
                                                            packetInfo.setIpAddress(InetAddress.getByName("127.0.0.1"));
                                                            packetInfo.setPortNum(9100);
                                                            packetInfo.setPayload("Unknown type test".getBytes());
                                                            byte[] unknownPacket = PacketParser.getPacketParser().createPkt(packetInfo);
                                                
                                                            // Send the unknown packet
                                                            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                                                                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                                                                dataOut.write(unknownPacket);
                                                                dataOut.flush();
                                                            }
                                                            System.out.println("Sent a packet with an unknown type.");
                                                            Thread.sleep(500); // Give server time to process
                                                
                                                            // Send a valid packet to ensure the server is still responsive
                                                            System.out.println("Sending a valid HELLO packet to check server status...");
                                                            try (Socket testSocket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                                                                DataOutputStream out = new DataOutputStream(testSocket.getOutputStream());
                                                                DataInputStream in = new DataInputStream(testSocket.getInputStream());
                                                
                                                                byte[] helloPacket = getHelloPacket(new ClientNode("127.0.0.1", testSocket.getLocalPort()));
                                                                out.write(helloPacket);
                                                
                                                                // The server should respond with a NETWORK packet.
                                                                testSocket.setSoTimeout(2000);
                                                                byte[] response = in.readAllBytes();
                                                                if (response.length > 0) {
                                                                    System.out.println("Server responded to valid packet after unknown one. Server is stable.");
                                                                } else {
                                                                    System.out.println("Server did NOT respond after unknown packet.");
                                                                }
                                                            }
                                                
                                                        } catch (Exception e) {
                                                            // If this exception happens, it might mean the server died.
                                                            System.out.println("An exception occurred during the test, possibly because the server is down.");
                                                            e.printStackTrace();
                                                        } finally {
                                                            // Teardown
                                                            if (mainServer != null) {
                                                                mainServer.close();
                                                            }
                                                            System.out.println("Finished unknown packet type test.");
                                                        }
                                                    }
                                                
    @org.junit.jupiter.api.Test
    public void testHandleClientTimeout() throws InterruptedException {
        System.out.println("Testing client timeout...");
        final int mainServerPort = 8110;
        final int clientPort = 9110;
        MainServer mainServer = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            final ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);

            // Reset and manually set up topology
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
            topology.addClient(mainServerNode);

            // Start the main server
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);

            // Send a HELLO packet to add the client to the timer
            byte[] helloPacket = getHelloPacket(clientNode);
            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.write(helloPacket);
                dataOut.flush();
            }

            // Wait for the timer to trigger the timeout
            Thread.sleep(6000); // Default timeout is 5000ms

            // Verification
            if (topology.getClusterIndex(clientNode) == -1) {
                System.out.println("Client successfully removed after timeout.");
            } else {
                System.out.println("Client was NOT removed after timeout.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Teardown
            if (mainServer != null) {
                mainServer.close();
            }
            System.out.println("Finished client timeout test.");
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleAlivePacket() throws Exception {
        System.out.println("Testing ALIVE packet handling...");
        final int mainServerPort = 8120;
        MainServer mainServer = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);

            // Create an ALIVE packet
            PacketInfo packetInfo = new PacketInfo();
            packetInfo.setLength(22);
            packetInfo.setType(NetworkType.USE.ordinal());
            packetInfo.setConnectionType(NetworkConnectionType.ALIVE.ordinal());
            packetInfo.setIpAddress(InetAddress.getByName("127.0.0.1"));
            packetInfo.setPortNum(9120);
            byte[] alivePacket = PacketParser.getPacketParser().createPkt(packetInfo);

            // Send the packet
            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.write(alivePacket);
                dataOut.flush();
            }

            System.out.println("Successfully sent ALIVE packet.");

        } finally {
            if (mainServer != null) {
                mainServer.close();
            }
            System.out.println("Finished ALIVE packet handling test.");
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleClosePacket() throws Exception {
        System.out.println("Testing CLOSE packet handling...");
        final int mainServerPort = 8130;
        MainServer mainServer = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);

            // Create a CLOSE packet
            PacketInfo packetInfo = new PacketInfo();
            packetInfo.setLength(22);
            packetInfo.setType(NetworkType.USE.ordinal());
            packetInfo.setConnectionType(NetworkConnectionType.CLOSE.ordinal());
            packetInfo.setIpAddress(InetAddress.getByName("127.0.0.1"));
            packetInfo.setPortNum(9130);
            byte[] closePacket = PacketParser.getPacketParser().createPkt(packetInfo);

            // Send the packet
            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.write(closePacket);
                dataOut.flush();
            }

            // Verification: The server should close, so a new connection should fail.
            Thread.sleep(500);
            try (Socket testSocket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                System.out.println("Server is still alive, CLOSE packet test failed.");
            } catch (IOException e) {
                System.out.println("Server is closed, CLOSE packet test passed.");
            }

        } finally {
            if (mainServer != null) {
                mainServer.close();
            }
            System.out.println("Finished CLOSE packet handling test.");
        }
    }

    @org.junit.jupiter.api.Test
    public void testServerClose() {
        System.out.println("Testing server close...");
        final int mainServerPort = 8140;
        MainServer mainServer = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);

            // Close the server
            mainServer.close();

            // Verification: The server should be closed, so a new connection should fail.
            Thread.sleep(500);
            try (Socket testSocket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                System.out.println("Server is still alive, close() test failed.");
            } catch (IOException e) {
                System.out.println("Server is closed, close() test passed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mainServer != null) {
                mainServer.close();
            }
            System.out.println("Finished server close test.");
        }
    }

    @org.junit.jupiter.api.Test
    public void testOtherClusterBroadcastToClients() {
        System.out.println("Testing OTHERCLUSTER broadcast to clients...");
        final int mainServerPort = 8150;
        final int clientAPort = 9150;
        final int clientBPort = 9151;
        final ServerSocket[] clientASock = new ServerSocket[1];
        final ServerSocket[] clientBSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread clientAThread = null;
        Thread clientBThread = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            final ClientNode clientANode = new ClientNode("127.0.0.1", clientAPort);
            final ClientNode clientBNode = new ClientNode("127.0.0.1", clientBPort);

            // Reset and manually set up topology
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
            topology.addClient(mainServerNode);
            topology.addClient(clientANode);
            topology.addClient(clientBNode);

            // Start listeners for clients
            clientAThread = new Thread(() -> stoppableReceive(clientAPort, clientASock));
            clientBThread = new Thread(() -> stoppableReceive(clientBPort, clientBSock));
            clientAThread.start();
            clientBThread.start();

            // Start the main server
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);

            // Create an OTHERCLUSTER broadcast packet
            PacketInfo packetInfo = new PacketInfo();
            packetInfo.setLength(22);
            packetInfo.setType(NetworkType.OTHERCLUSTER.ordinal());
            packetInfo.setBroadcast(1);
            packetInfo.setIpAddress(InetAddress.getByName("127.0.0.1"));
            packetInfo.setPortNum(9999); // Some other server
            packetInfo.setPayload("Broadcast from another cluster".getBytes());
            byte[] packet = PacketParser.getPacketParser().createPkt(packetInfo);

            // Send the packet to the main server
            try (Socket socket = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.write(packet);
                dataOut.flush();
            }

            clientAThread.join(2000);
            clientBThread.join(2000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Teardown
            if (mainServer != null) {
                mainServer.close();
            }
            for (ServerSocket sock : new ServerSocket[]{clientASock[0], clientBSock[0]}) {
                if (sock != null && !sock.isClosed()) {
                    try {
                        sock.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (Thread t : new Thread[]{clientAThread, clientBThread}) {
                if (t != null && t.isAlive()) {
                    t.interrupt();
                }
            }
            System.out.println("Finished OTHERCLUSTER broadcast to clients test.");
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleClientTimeoutWithReflection() throws Exception {
        System.out.println("Testing client timeout with reflection...");
        final int mainServerPort = 8190;
        MainServer mainServer = null;

        try {
            // Setup
            final ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            final ClientNode clientNode = new ClientNode("127.0.0.1", 9190);

            Topology topology = Topology.getTopology();
            topology.replaceNetwork(new NetworkStructure(new ArrayList<>(), new ArrayList<>()));
            // Create a cluster with mainServerNode as the server at index 0
            List<ClientNode> mainCluster = new ArrayList<>();
            mainCluster.add(mainServerNode);
            List<ClientNode> servers = new ArrayList<>();
            servers.add(mainServerNode);
            List<List<ClientNode>> clusters = new ArrayList<>();
            clusters.add(mainCluster);
            topology.replaceNetwork(new NetworkStructure(clusters, servers));

            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);

            // Call handleHello to add the client to the timer
            invokePrivateMethod(mainServer, "handleHello", new Class<?>[]{ClientNode.class}, new Object[]{clientNode});

            // Verification
            if (topology.getClusterIndex(clientNode) != -1) {
                System.out.println("Client successfully added to topology via handleHello.");
            } else {
                System.out.println("Client was NOT added to topology via handleHello.");
            }

        } finally {
            if (mainServer != null) {
                mainServer.close();
            }
            System.out.println("Finished client timeout test with reflection.");
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleUsePacketAlive() throws Exception {
        final int mainServerPort = 8200;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", 9200);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            byte[] alivePacket = PacketParser.getPacketParser().createPkt(createPacketInfo(NetworkType.USE.ordinal(), NetworkConnectionType.ALIVE.ordinal(), clientNode, null));
            invokePrivateMethod(mainServer, "handleUsePacket", new Class<?>[]{byte[].class, ClientNode.class, int.class}, new Object[]{alivePacket, clientNode, NetworkConnectionType.ALIVE.ordinal()});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleUsePacketClose() throws Exception {
        final int mainServerPort = 8210;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", 9210);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            byte[] closePacket = PacketParser.getPacketParser().createPkt(createPacketInfo(NetworkType.USE.ordinal(), NetworkConnectionType.CLOSE.ordinal(), clientNode, null));
            invokePrivateMethod(mainServer, "handleUsePacket", new Class<?>[]{byte[].class, ClientNode.class, int.class}, new Object[]{closePacket, clientNode, NetworkConnectionType.CLOSE.ordinal()});
            Thread.sleep(500);
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleBroadcastUse() throws Exception {
        final int mainServerPort = 8220, serverBPort = 8221, serverCPort = 8222;
        final ServerSocket[] serverBSock = new ServerSocket[1], serverCSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread serverBThread = null, serverCThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode serverBNode = new ClientNode("127.0.0.1", serverBPort);
            ClientNode serverCNode = new ClientNode("127.0.0.1", serverCPort);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createTopology(mainServerNode));
            topology.addClient(serverBNode);
            topology.addClient(serverCNode);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            serverBThread = new Thread(() -> stoppableReceive(serverBPort, serverBSock));
            serverCThread = new Thread(() -> stoppableReceive(serverCPort, serverCSock));
            serverBThread.start();
            serverCThread.start();
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(NetworkType.USE.ordinal(), 0, new ClientNode("127.0.0.1", 9220), "Broadcast from main server".getBytes());
            pkt.setBroadcast(1);
            invokePrivateMethod(mainServer, "handleBroadcast", new Class<?>[]{PacketInfo.class}, new Object[]{pkt});
            serverBThread.join(2000);
            serverCThread.join(2000);
        } finally {
            cleanup(mainServer, serverBSock, serverCSock);
            cleanupThreads(serverBThread, serverCThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleBroadcastOtherCluster() throws Exception {
        final int mainServerPort = 8230, clientAPort = 9230, clientBPort = 9231;
        final ServerSocket[] clientASock = new ServerSocket[1], clientBSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread clientAThread = null, clientBThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientANode = new ClientNode("127.0.0.1", clientAPort);
            ClientNode clientBNode = new ClientNode("127.0.0.1", clientBPort);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createTopology(mainServerNode));
            topology.addClient(clientANode);
            topology.addClient(clientBNode);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            clientAThread = new Thread(() -> stoppableReceive(clientAPort, clientASock));
            clientBThread = new Thread(() -> stoppableReceive(clientBPort, clientBSock));
            clientAThread.start();
            clientBThread.start();
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(NetworkType.OTHERCLUSTER.ordinal(), 0, mainServerNode, "Broadcast from another cluster".getBytes());
            pkt.setBroadcast(1);
            invokePrivateMethod(mainServer, "handleBroadcast", new Class<?>[]{PacketInfo.class}, new Object[]{pkt});
            clientAThread.join(2000);
            clientBThread.join(2000);
        } finally {
            cleanup(mainServer, clientASock, clientBSock);
            cleanupThreads(clientAThread, clientBThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testAddClientToTimerWithReflection() throws Exception {
        final int mainServerPort = 8240;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", 9240);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "addClientToTimer", new Class<?>[]{ClientNode.class, int.class}, new Object[]{clientNode, 0});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testSendNetworkPktResponseWithReflection() throws Exception {
        final int mainServerPort = 8250, clientPort = 9250;
        final ServerSocket[] clientSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread clientThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            clientThread = new Thread(() -> stoppableReceive(clientPort, clientSock));
            clientThread.start();
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "sendNetworkPktResponse", new Class<?>[]{ClientNode.class}, new Object[]{clientNode});
            clientThread.join(2000);
        } finally {
            cleanup(mainServer, clientSock);
            cleanupThreads(clientThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testSendAddPktResponseWithReflection() throws Exception {
        final int mainServerPort = 8260, serverBPort = 8261;
        final ServerSocket[] serverBSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread serverBThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode newClientNode = new ClientNode("127.0.0.1", 9260);
            ClientNode serverBNode = new ClientNode("127.0.0.1", serverBPort);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            serverBThread = new Thread(() -> stoppableReceive(serverBPort, serverBSock));
            serverBThread.start();
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "sendAddPktResponse", new Class<?>[]{ClientNode.class, ClientNode.class, int.class}, new Object[]{newClientNode, serverBNode, 0});
            serverBThread.join(2000);
        } finally {
            cleanup(mainServer, serverBSock);
            cleanupThreads(serverBThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testParsePacketSameCluster() throws Exception {
        final int mainServerPort = 8270, clientPort = 9270;
        final ServerSocket[] clientSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread clientThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode));
            Topology.getTopology().addClient(clientNode);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            clientThread = new Thread(() -> stoppableReceive(clientPort, clientSock));
            clientThread.start();
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(NetworkType.SAMECLUSTER.ordinal(), NetworkConnectionType.HELLO.ordinal(), clientNode, "Same cluster test".getBytes());
            try (Socket s = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                new DataOutputStream(s.getOutputStream()).write(PacketParser.getPacketParser().createPkt(pkt));
            }
            clientThread.join(2000);
        } finally {
            cleanup(mainServer, clientSock);
            cleanupThreads(clientThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testParsePacketOtherClusterDirect() throws Exception {
        final int mainServerPort = 8280, otherServerPort = 8281;
        final ServerSocket[] otherServerSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread otherServerThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode otherServerNode = new ClientNode("127.0.0.1", otherServerPort);
            List<ClientNode> otherCluster = new ArrayList<>();
            otherCluster.add(otherServerNode);
            Topology.getTopology().replaceNetwork(createMultiClusterTopology(mainServerNode, otherCluster, otherServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            otherServerThread = new Thread(() -> stoppableReceive(otherServerPort, otherServerSock));
            otherServerThread.start();
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(NetworkType.OTHERCLUSTER.ordinal(), NetworkConnectionType.HELLO.ordinal(), otherServerNode, "Other cluster direct test".getBytes());
            try (Socket s = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                new DataOutputStream(s.getOutputStream()).write(PacketParser.getPacketParser().createPkt(pkt));
            }
            otherServerThread.join(2000);
        } finally {
            cleanup(mainServer, otherServerSock);
            cleanupThreads(otherServerThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testParsePacketOtherClusterViaServer() throws Exception {
        final int mainServerPort = 8290, otherServerPort = 8291, clientPort = 9290;
        final ServerSocket[] otherServerSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread otherServerThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode otherServerNode = new ClientNode("127.0.0.1", otherServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);
            List<ClientNode> otherCluster = new ArrayList<>();
            otherCluster.add(otherServerNode);
            otherCluster.add(clientNode);
            Topology.getTopology().replaceNetwork(createMultiClusterTopology(mainServerNode, otherCluster, otherServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            otherServerThread = new Thread(() -> stoppableReceive(otherServerPort, otherServerSock));
            otherServerThread.start();
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(NetworkType.OTHERCLUSTER.ordinal(), NetworkConnectionType.HELLO.ordinal(), clientNode, "Other cluster via server test".getBytes());
            try (Socket s = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                new DataOutputStream(s.getOutputStream()).write(PacketParser.getPacketParser().createPkt(pkt));
            }
            otherServerThread.join(2000);
        } finally {
            cleanup(mainServer, otherServerSock);
            cleanupThreads(otherServerThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleBroadcastUnknownType() throws Exception {
        final int mainServerPort = 8300;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(99, 0, new ClientNode("127.0.0.1", 9300), "Unknown broadcast type".getBytes());
            pkt.setBroadcast(1);
            invokePrivateMethod(mainServer, "handleBroadcast", new Class<?>[]{PacketInfo.class}, new Object[]{pkt});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleRemoveDifferentCluster() throws Exception {
        final int mainServerPort = 8310, clientAPort = 9310, clientBPort = 9311;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientANode = new ClientNode("127.0.0.1", clientAPort);
            ClientNode clientBNode = new ClientNode("127.0.0.1", clientBPort);
            List<ClientNode> otherCluster = new ArrayList<>();
            otherCluster.add(clientANode);
            Topology.getTopology().replaceNetwork(createMultiClusterTopology(mainServerNode, otherCluster, clientANode));
            Topology.getTopology().addClient(clientBNode);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "handleRemove", new Class<?>[]{byte[].class, ClientNode.class}, new Object[]{getRemovePacket(clientANode), clientANode});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testAddClientToTimerSameCluster() throws Exception {
        final int mainServerPort = 8320;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", 9320);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createTopology(mainServerNode));
            topology.addClient(clientNode);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "addClientToTimer", new Class<?>[]{ClientNode.class, int.class}, new Object[]{clientNode, topology.getClusterIndex(clientNode)});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testAddClientToTimerAsServer() throws Exception {
        final int mainServerPort = 8330, otherServerPort = 8331;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode otherServerNode = new ClientNode("127.0.0.1", otherServerPort);
            List<ClientNode> otherCluster = new ArrayList<>();
            otherCluster.add(otherServerNode);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createMultiClusterTopology(mainServerNode, otherCluster, otherServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "addClientToTimer", new Class<?>[]{ClientNode.class, int.class}, new Object[]{otherServerNode, topology.getClusterIndex(otherServerNode)});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleHelloWithMultipleServersAndClients() throws Exception {
        final int mainServerPort = 8340, serverBPort = 8341, clientAPort = 9340, clientBPort = 9341;
        final ServerSocket[] serverBSock = new ServerSocket[1], clientASock = new ServerSocket[1], clientBSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread serverBThread = null, clientAThread = null, clientBThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode serverBNode = new ClientNode("127.0.0.1", serverBPort);
            ClientNode clientANode = new ClientNode("127.0.0.1", clientAPort);
            ClientNode clientBNode = new ClientNode("127.0.0.1", clientBPort);
            ClientNode newClientNode = new ClientNode("127.0.0.1", 9342);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createTopology(mainServerNode, clientANode, clientBNode));
            List<ClientNode> servers = new ArrayList<>();
            servers.add(mainServerNode);
            servers.add(serverBNode);
            List<List<ClientNode>> clusters = new ArrayList<>();
            clusters.add(topology.getNetwork().clusters().get(0));
            topology.replaceNetwork(new NetworkStructure(clusters, servers));
            topology.addClient(serverBNode);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            serverBThread = new Thread(() -> stoppableReceive(serverBPort, serverBSock));
            clientAThread = new Thread(() -> stoppableReceive(clientAPort, clientASock));
            clientBThread = new Thread(() -> stoppableReceive(clientBPort, clientBSock));
            serverBThread.start();
            clientAThread.start();
            clientBThread.start();
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "handleHello", new Class<?>[]{ClientNode.class}, new Object[]{newClientNode});
            serverBThread.join(2000);
            clientAThread.join(2000);
            clientBThread.join(2000);
        } finally {
            cleanup(mainServer, serverBSock, clientASock, clientBSock);
            cleanupThreads(serverBThread, clientAThread, clientBThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleClientTimeoutWithServersAndClients() throws Exception {
        final int mainServerPort = 8350, serverBPort = 8351, clientAPort = 9350;
        final ServerSocket[] serverBSock = new ServerSocket[1], clientASock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread serverBThread = null, clientAThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode serverBNode = new ClientNode("127.0.0.1", serverBPort);
            ClientNode clientANode = new ClientNode("127.0.0.1", clientAPort);
            ClientNode timeoutClient = new ClientNode("127.0.0.1", 9351);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createTopology(mainServerNode, clientANode, timeoutClient));
            List<ClientNode> servers = new ArrayList<>();
            servers.add(mainServerNode);
            servers.add(serverBNode);
            List<List<ClientNode>> clusters = new ArrayList<>();
            clusters.add(topology.getNetwork().clusters().get(0));
            topology.replaceNetwork(new NetworkStructure(clusters, servers));
            topology.addClient(serverBNode);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            serverBThread = new Thread(() -> stoppableReceive(serverBPort, serverBSock));
            clientAThread = new Thread(() -> stoppableReceive(clientAPort, clientASock));
            serverBThread.start();
            clientAThread.start();
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "handleClientTimeout", new Class<?>[]{ClientNode.class}, new Object[]{timeoutClient});
            serverBThread.join(2000);
            clientAThread.join(2000);
        } finally {
            cleanup(mainServer, serverBSock, clientASock);
            cleanupThreads(serverBThread, clientAThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testModulePacketWithNullData() throws Exception {
        final int mainServerPort = 8360;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", 9360);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(NetworkType.USE.ordinal(), NetworkConnectionType.MODULE.ordinal(), clientNode, "Partial chunk".getBytes());
            pkt.setModule(ModuleType.NETWORKING.ordinal());
            pkt.setChunkNum(0);
            pkt.setChunkLength(2);
            invokePrivateMethod(mainServer, "handleUsePacket", new Class<?>[]{byte[].class, ClientNode.class, int.class}, new Object[]{PacketParser.getPacketParser().createPkt(pkt), clientNode, NetworkConnectionType.MODULE.ordinal()});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testCloseClient() throws Exception {
        final int mainServerPort = 8370;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", 9370);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createTopology(mainServerNode));
            topology.addClient(clientNode);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "addClientToTimer", new Class<?>[]{ClientNode.class, int.class}, new Object[]{clientNode, topology.getClusterIndex(clientNode)});
            mainServer.closeClient(clientNode);
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testParsePacketWithBroadcastAndUse() throws Exception {
        final int mainServerPort = 8380, serverBPort = 8381;
        final ServerSocket[] serverBSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread serverBThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode serverBNode = new ClientNode("127.0.0.1", serverBPort);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createTopology(mainServerNode));
            List<ClientNode> servers = new ArrayList<>();
            servers.add(mainServerNode);
            servers.add(serverBNode);
            List<List<ClientNode>> clusters = new ArrayList<>();
            clusters.add(topology.getNetwork().clusters().get(0));
            topology.replaceNetwork(new NetworkStructure(clusters, servers));
            topology.addClient(serverBNode);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            serverBThread = new Thread(() -> stoppableReceive(serverBPort, serverBSock));
            serverBThread.start();
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(NetworkType.USE.ordinal(), NetworkConnectionType.HELLO.ordinal(), new ClientNode("127.0.0.1", 9380), "Broadcast then USE test".getBytes());
            pkt.setBroadcast(1);
            try (Socket s = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                new DataOutputStream(s.getOutputStream()).write(PacketParser.getPacketParser().createPkt(pkt));
            }
            serverBThread.join(2000);
        } finally {
            cleanup(mainServer, serverBSock);
            cleanupThreads(serverBThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testSendWithArray() throws Exception {
        final int mainServerPort = 8390, clientPort1 = 9390, clientPort2 = 9391;
        final ServerSocket[] clientSock1 = new ServerSocket[1], clientSock2 = new ServerSocket[1];
        MainServer mainServer = null;
        Thread clientThread1 = null, clientThread2 = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode1 = new ClientNode("127.0.0.1", clientPort1);
            ClientNode clientNode2 = new ClientNode("127.0.0.1", clientPort2);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createTopology(mainServerNode));
            topology.addClient(clientNode1);
            topology.addClient(clientNode2);
            mainServer = new MainServer(mainServerNode, mainServerNode);
            clientThread1 = new Thread(() -> stoppableReceive(clientPort1, clientSock1));
            clientThread2 = new Thread(() -> stoppableReceive(clientPort2, clientSock2));
            clientThread1.start();
            clientThread2.start();
            Thread.sleep(500);
            mainServer.send("Array send test".getBytes(), new ClientNode[]{clientNode1, clientNode2});
            clientThread1.join(2000);
            clientThread2.join(2000);
        } finally {
            cleanup(mainServer, clientSock1, clientSock2);
            cleanupThreads(clientThread1, clientThread2);
        }
    }

    @org.junit.jupiter.api.Test
    public void testAddClientToTimerNotSameClusterNotServer() throws Exception {
        final int mainServerPort = 8400, clientPort = 9400, otherServerPort = 8401;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);
            ClientNode otherServerNode = new ClientNode("127.0.0.1", otherServerPort);
            List<ClientNode> otherCluster = new ArrayList<>();
            otherCluster.add(otherServerNode);
            otherCluster.add(clientNode);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createMultiClusterTopology(mainServerNode, otherCluster, otherServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "addClientToTimer", new Class<?>[]{ClientNode.class, int.class}, new Object[]{clientNode, topology.getClusterIndex(clientNode)});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleHelloWithNoOtherServersOrClients() throws Exception {
        final int mainServerPort = 8410, clientPort = 9410;
        final ServerSocket[] clientSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread clientThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode newClientNode = new ClientNode("127.0.0.1", clientPort);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            clientThread = new Thread(() -> stoppableReceive(clientPort, clientSock));
            clientThread.start();
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "handleHello", new Class<?>[]{ClientNode.class}, new Object[]{newClientNode});
            clientThread.join(2000);
        } finally {
            cleanup(mainServer, clientSock);
            cleanupThreads(clientThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleBroadcastUseWithNoOtherServers() throws Exception {
        final int mainServerPort = 8420;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(NetworkType.USE.ordinal(), 0, new ClientNode("127.0.0.1", 9420), "Broadcast with no other servers".getBytes());
            pkt.setBroadcast(1);
            invokePrivateMethod(mainServer, "handleBroadcast", new Class<?>[]{PacketInfo.class}, new Object[]{pkt});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleBroadcastOtherClusterWithNoClients() throws Exception {
        final int mainServerPort = 8430;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(NetworkType.OTHERCLUSTER.ordinal(), 0, mainServerNode, "Broadcast OTHERCLUSTER with no clients".getBytes());
            pkt.setBroadcast(1);
            invokePrivateMethod(mainServer, "handleBroadcast", new Class<?>[]{PacketInfo.class}, new Object[]{pkt});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleRemoveSameCluster() throws Exception {
        final int mainServerPort = 8440, clientAPort = 9440, clientBPort = 9441;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientANode = new ClientNode("127.0.0.1", clientAPort);
            ClientNode clientBNode = new ClientNode("127.0.0.1", clientBPort);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode, clientANode, clientBNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "handleRemove", new Class<?>[]{byte[].class, ClientNode.class}, new Object[]{getRemovePacket(clientANode), clientANode});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleClientTimeoutWithNoOtherServersOrClients() throws Exception {
        final int mainServerPort = 8450;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode timeoutClient = new ClientNode("127.0.0.1", 9450);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode, timeoutClient));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "handleClientTimeout", new Class<?>[]{ClientNode.class}, new Object[]{timeoutClient});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleClientTimeoutWhenClientIsServer() throws Exception {
        final int mainServerPort = 8460, otherServerPort = 8461;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode otherServerNode = new ClientNode("127.0.0.1", otherServerPort);
            List<ClientNode> otherCluster = new ArrayList<>();
            otherCluster.add(otherServerNode);
            Topology.getTopology().replaceNetwork(createMultiClusterTopology(mainServerNode, otherCluster, otherServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "handleClientTimeout", new Class<?>[]{ClientNode.class}, new Object[]{otherServerNode});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testSendNetworkPktResponse() throws Exception {
        final int mainServerPort = 8470, clientPort = 9470;
        final ServerSocket[] clientSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread clientThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", clientPort);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode, clientNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            clientThread = new Thread(() -> stoppableReceive(clientPort, clientSock));
            clientThread.start();
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "sendNetworkPktResponse", new Class<?>[]{ClientNode.class}, new Object[]{clientNode});
            clientThread.join(2000);
        } finally {
            cleanup(mainServer, clientSock);
            cleanupThreads(clientThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testSendAddPktResponse() throws Exception {
        final int mainServerPort = 8480, serverBPort = 8481;
        final ServerSocket[] serverBSock = new ServerSocket[1];
        MainServer mainServer = null;
        Thread serverBThread = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode serverBNode = new ClientNode("127.0.0.1", serverBPort);
            ClientNode newClientNode = new ClientNode("127.0.0.1", 9480);
            List<ClientNode> otherCluster = new ArrayList<>();
            otherCluster.add(serverBNode);
            Topology topology = Topology.getTopology();
            topology.replaceNetwork(createMultiClusterTopology(mainServerNode, otherCluster, serverBNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            serverBThread = new Thread(() -> stoppableReceive(serverBPort, serverBSock));
            serverBThread.start();
            Thread.sleep(500);
            invokePrivateMethod(mainServer, "sendAddPktResponse", new Class<?>[]{ClientNode.class, ClientNode.class, int.class}, new Object[]{newClientNode, serverBNode, topology.addClient(newClientNode)});
            serverBThread.join(2000);
        } finally {
            cleanup(mainServer, serverBSock);
            cleanupThreads(serverBThread);
        }
    }

    @org.junit.jupiter.api.Test
    public void testParsePacketUnknownType() throws Exception {
        final int mainServerPort = 8490;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            PacketInfo pkt = createPacketInfo(99, NetworkConnectionType.HELLO.ordinal(), new ClientNode("127.0.0.1", 9490), "Unknown type test".getBytes());
            try (Socket s = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                new DataOutputStream(s.getOutputStream()).write(PacketParser.getPacketParser().createPkt(pkt));
            }
            Thread.sleep(500);
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testHandleUsePacketUnknownConnectionType() throws Exception {
        final int mainServerPort = 8500;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            ClientNode clientNode = new ClientNode("127.0.0.1", 9500);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            byte[] packet = PacketParser.getPacketParser().createPkt(createPacketInfo(NetworkType.USE.ordinal(), 99, clientNode, null));
            invokePrivateMethod(mainServer, "handleUsePacket", new Class<?>[]{byte[].class, ClientNode.class, int.class}, new Object[]{packet, clientNode, 99});
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }

    @org.junit.jupiter.api.Test
    public void testReceiveWithSplitPackets() throws Exception {
        final int mainServerPort = 8510;
        MainServer mainServer = null;
        try {
            ClientNode mainServerNode = new ClientNode("127.0.0.1", mainServerPort);
            Topology.getTopology().replaceNetwork(createTopology(mainServerNode));
            mainServer = new MainServer(mainServerNode, mainServerNode);
            Thread.sleep(500);
            try (Socket s = new Socket(mainServerNode.hostName(), mainServerNode.port())) {
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.write(getHelloPacket(new ClientNode("127.0.0.1", 9510)));
                out.write(getHelloPacket(new ClientNode("127.0.0.1", 9511)));
            }
            Thread.sleep(1000);
        } finally {
            if (mainServer != null) mainServer.close();
        }
    }
}
                                                