package com.swe.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test class for the topology class.
 */
class TopologyTest {

    /**
     * Function to test the receive by the main server.
     */
    @org.junit.jupiter.api.Test
    void mainServerReceiveTestHello() {
        try {
            final int sleepTime = 15000;
            final Integer port = 8000;
            final Integer timeout = 5000;
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final Topology topology = Topology.getTopology();
            topology.addUser(server, server);
            final Socket destSocket = new Socket();
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            final Socket destSocket1 = new Socket();
            // destSocket1.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            // sendHello(destSocket);
            // sendHello(destSocket1);
            // sendRemove(destSocket);
            Thread.sleep(sleepTime);
            // topology.closeTopology();
        } catch (InterruptedException ex) {
            System.out.println("Error 1...");
        } catch (IOException e) {
            System.out.println("Error 2...");
        }
    }

    /**
     * Test function to send data.
     */
    public void sendHello(final Socket destSocket) {
        try {

            final int sleepTime = 1000;
            System.out.println("Client Socket " + destSocket.getLocalAddress().toString() + destSocket.getLocalPort());
            final Thread receiveThread = new Thread(() -> receiveHello(destSocket));
            receiveThread.start();
            Thread.sleep(sleepTime);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final ClientNode client = new ClientNode("127.0.0.1", destSocket.getLocalPort());
            final byte[] packet = getHelloPacket(client);
            dataOut.write(packet);
            dataOut.flush();
            // receiveThread.interrupt();
            // destSocket.close();
        } catch (IOException | InterruptedException ex) {
        }
    }

    public void sendRemove(final Socket destSocket) {
        try {

            destSocket.setTcpNoDelay(true);
            final int sleepTime = 1000;
            final Thread receiveThread = new Thread(() -> receiveRemove(destSocket));
            receiveThread.start();
            Thread.sleep(sleepTime);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final ClientNode client = new ClientNode("127.0.0.1", destSocket.getLocalPort());
            final byte[] packet = getRemovePacket(client);
            dataOut.write(packet);
            dataOut.flush();
            // receiveThread.interrupt();
            // destSocket.close();
        } catch (IOException | InterruptedException ex) {
        }
    }

    public void receiveHello(final Socket socket) {
        try {
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final int byteBufferSize = 2048;
            final byte[] buffer = new byte[byteBufferSize];
            while ((dataIn.read(buffer)) != -1) {
                final PacketInfo pkt = PacketParser.getPacketParser().parsePacket(buffer);
                final NetworkStructure network = NetworkSerializer.getNetworkSerializer()
                        .deserializeNetworkStructure(pkt.getPayload());
                System.out.println("Hello Response: " + network);
            }

        } catch (IOException ex) {
        }
    }

    public void receiveRemove(final Socket socket) {
        try {
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final int byteBufferSize = 2048;
            final byte[] buffer = new byte[byteBufferSize];
            while ((dataIn.read(buffer)) != -1) {
                final PacketInfo pkt = PacketParser.getPacketParser().parsePacket(buffer);
                System.out.println("Remove Response: " + new String(pkt.getPayload()));
            }

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
            final int idx = topology.getClusterIndex(client);
            final ClientNetworkRecord record = new ClientNetworkRecord(client, idx);
            final byte[] clientBytes = NetworkSerializer.getNetworkSerializer().serializeClientNetworkRecord(record);
            pkt.setLength(packetHeaderSize + clientBytes.length);
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
            pkt.setPayload(clientBytes);
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

    public byte[] getAlivePacket(final ClientNode client) {
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
            pkt.setConnectionType(NetworkConnectionType.ALIVE.ordinal());
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

    public byte[] getClosePacket(final ClientNode client) {
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
            pkt.setConnectionType(NetworkConnectionType.CLOSE.ordinal());
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

    /**
     * Function to test alive the receive by the main server.
     */
    @org.junit.jupiter.api.Test
    void mainServerReceiveTestAlive() {
        try {
            final int sleepTime = 1000;
            final Integer port = 8000;
            final Integer timeout = 5000;
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final Topology topology = Topology.getTopology();
            topology.addUser(server, server);
            final Socket destSocket = new Socket();
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            sendHello(destSocket);
            sendAlive(destSocket);
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("Error 1...");
        } catch (IOException e) {
            System.out.println("Error 2...");
        }
    }

    /**
     * Test function to send data.
     */
    public void sendAlive(final Socket destSocket) {
        try {

            destSocket.setTcpNoDelay(true);
            final int sleepTime = 1000;
            final Thread receiveThread = new Thread(() -> receiveAlive(destSocket));
            receiveThread.start();
            Thread.sleep(sleepTime);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final ClientNode client = new ClientNode("127.0.0.1", destSocket.getLocalPort());
            final byte[] packet = getAlivePacket(client);
            dataOut.write(packet);
            dataOut.flush();
            // receiveThread.interrupt();
            // destSocket.close();
        } catch (IOException | InterruptedException ex) {
        }
    }

    public void receiveAlive(final Socket socket) {
        try {
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final int byteBufferSize = 2048;
            final byte[] buffer = new byte[byteBufferSize];
            while ((dataIn.read(buffer)) != -1) {
                final PacketInfo pkt = PacketParser.getPacketParser().parsePacket(buffer);
                System.out.println("Alive Response: " + new String(pkt.getPayload()));
            }

        } catch (IOException ex) {
        }
    }

    /**
     * Function to test alive the receive by the main server.
     */
    @org.junit.jupiter.api.Test
    void mainServerReceiveTestOtherCluster() {
        try {
            final int sleepTime = 1000;
            final Integer port = 8000;
            final Integer timeout = 5000;
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final Topology topology = Topology.getTopology();
            topology.addUser(server, server);
            final Socket destSocket = new Socket();
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            sendHello(destSocket);
            sendOtherCluster(destSocket);
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("Error 1...");
        } catch (IOException e) {
            System.out.println("Error 2...");
        }
    }

    /**
     * Test function to send data.
     */
    public void sendOtherCluster(final Socket destSocket) {
        try {

            destSocket.setTcpNoDelay(true);
            final int sleepTime = 1000;
            final Thread receiveThread = new Thread(() -> receiveOtherCluster(destSocket));
            receiveThread.start();
            Thread.sleep(sleepTime);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final ClientNode client = new ClientNode("127.0.0.1", destSocket.getLocalPort());
            final byte[] packet = getOtherClusterPacket(client);
            dataOut.write(packet);
            dataOut.flush();
            // receiveThread.interrupt();
            // destSocket.close();
        } catch (IOException | InterruptedException ex) {
        }
    }

    public void receiveOtherCluster(final Socket socket) {
        try {
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final int byteBufferSize = 2048;
            final byte[] buffer = new byte[byteBufferSize];
            while ((dataIn.read(buffer)) != -1) {
                final PacketInfo pkt = PacketParser.getPacketParser().parsePacket(buffer);
                System.out.println("OtherCluster Response: " + new String(pkt.getPayload()));
            }

        } catch (IOException ex) {
        }
    }

    public byte[] getOtherClusterPacket(final ClientNode client) {
        try {
            final int randomFactor = (int) Math.pow(10, 6);
            final PacketParser parser = PacketParser.getPacketParser();
            final PacketInfo pkt = new PacketInfo();
            final int packetHeaderSize = 22;
            pkt.setLength(packetHeaderSize);
            final String data = "Hello Sekai !!!";
            pkt.setType(NetworkType.OTHERCLUSTER.ordinal() + data.length());
            pkt.setPriority(0);
            pkt.setModule(0);
            pkt.setConnectionType(NetworkConnectionType.CLOSE.ordinal());
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

    /**
     * Function to test alive the receive by the main server.
     */
    @org.junit.jupiter.api.Test
    void mainServerReceiveTestSameCluster() {
        try {
            final int sleepTime = 1000;
            final Integer port = 8000;
            final Integer timeout = 5000;
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final Topology topology = Topology.getTopology();
            topology.addUser(server, server);
            final Socket destSocket = new Socket();
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            sendHello(destSocket);
            sendSameCluster(destSocket);
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("Error 1...");
        } catch (IOException e) {
            System.out.println("Error 2...");
        }
    }

    /**
     * Test function to send data.
     */
    public void sendSameCluster(final Socket destSocket) {
        try {

            destSocket.setTcpNoDelay(true);
            final int sleepTime = 1000;
            final Thread receiveThread = new Thread(() -> receiveSameCluster(destSocket));
            receiveThread.start();
            Thread.sleep(sleepTime);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final ClientNode client = new ClientNode("127.0.0.1", destSocket.getLocalPort());
            final byte[] packet = getSameClusterPacket(client);
            dataOut.write(packet);
            dataOut.flush();
            // receiveThread.interrupt();
            // destSocket.close();
        } catch (IOException | InterruptedException ex) {
        }
    }

    public void receiveSameCluster(final Socket socket) {
        try {
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final int byteBufferSize = 2048;
            final byte[] buffer = new byte[byteBufferSize];
            while ((dataIn.read(buffer)) != -1) {
                final PacketInfo pkt = PacketParser.getPacketParser().parsePacket(buffer);
                System.out.println("SameCluster Response: " + new String(pkt.getPayload()));
            }

        } catch (IOException ex) {
        }
    }

    public byte[] getSameClusterPacket(final ClientNode client) {
        try {
            final int randomFactor = (int) Math.pow(10, 6);
            final PacketParser parser = PacketParser.getPacketParser();
            final PacketInfo pkt = new PacketInfo();
            final int packetHeaderSize = 22;
            final String data = "Hello Sekai !!!";
            pkt.setLength(packetHeaderSize + data.length());
            pkt.setType(NetworkType.SAMECLUSTER.ordinal());
            pkt.setPriority(0);
            pkt.setModule(0);
            pkt.setConnectionType(NetworkConnectionType.CLOSE.ordinal());
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
    void mainServerReceiveTestClose() {
        try {
            final int sleepTime = 1000;
            final Integer port = 8000;
            final Integer timeout = 5000;
            final ClientNode server = new ClientNode("127.0.0.1", 8000);
            final Topology topology = Topology.getTopology();
            topology.addUser(server, server);
            final Socket destSocket = new Socket();
            destSocket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            sendHello(destSocket);
            sendClose(destSocket);
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("Error 1...");
        } catch (IOException e) {
            System.out.println("Error 2...");
        }
    }

    /**
     * Test function to send data.
     */
    public void sendClose(final Socket destSocket) {
        try {

            destSocket.setTcpNoDelay(true);
            final int sleepTime = 1000;
            final Thread receiveThread = new Thread(() -> receiveClose(destSocket));
            receiveThread.start();
            Thread.sleep(sleepTime);
            final DataOutputStream dataOut = new DataOutputStream(destSocket.getOutputStream());
            final ClientNode client = new ClientNode("127.0.0.1", destSocket.getLocalPort());
            final byte[] packet = getClosePacket(client);
            dataOut.write(packet);
            dataOut.flush();
            // receiveThread.interrupt();
            // destSocket.close();
        } catch (IOException | InterruptedException ex) {
        }
    }

    public void receiveClose(final Socket socket) {
        try {
            final DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            final int byteBufferSize = 2048;
            final byte[] buffer = new byte[byteBufferSize];
            while ((dataIn.read(buffer)) != -1) {
                final PacketInfo pkt = PacketParser.getPacketParser().parsePacket(buffer);
                System.out.println("Close Response: " + new String(pkt.getPayload()));
            }

        } catch (IOException ex) {
        }
    }

    @org.junit.jupiter.api.Test
    public void testGetServer() {
        final Topology topology = Topology.getTopology();
        final ClientNode server = new ClientNode("127.0.0.1", 8000);
        final ClientNode dest1 = new ClientNode("127.0.0.1", 8001);
        topology.addUser(server, server);
        topology.addClient(dest1);
        final List<ClientNode> cluster1 = new ArrayList<>();
        final List<ClientNode> cluster2 = new ArrayList<>();
        cluster1.add(server);
        cluster2.add(dest1);
        final List<ClientNode> servers = new ArrayList<>();
        servers.add(server);
        servers.add(dest1);
        final List<List<ClientNode>> clusters = new ArrayList<>();
        clusters.add(cluster1);
        clusters.add(cluster2);
        final NetworkStructure structure = new NetworkStructure(clusters, servers);
        topology.replaceNetwork(structure);
        final List<ClientNode> clients = topology.getAllClients();
        final ClientNode getserver = topology.getServer(server);
        assertEquals(server, getserver);
        final List<ClientNode> expectedclients = new ArrayList<>();
        expectedclients.add(server);
        expectedclients.add(dest1);
        assertEquals(clients, expectedclients);
    }

    @org.junit.jupiter.api.Test
    void testAddClient() {
        final Topology topology = Topology.getTopology();
        
        // Reset the topology
        final java.util.List<java.util.List<ClientNode>> clusters = new java.util.ArrayList<>();
        final java.util.List<ClientNode> servers = new java.util.ArrayList<>();
        final NetworkStructure emptyNetwork = new NetworkStructure(clusters, servers);
        topology.replaceNetwork(emptyNetwork);

        final ClientNode server = new ClientNode("127.0.0.1", 9000); // Using a different port to avoid conflict
        topology.addUser(server, server);

        // Add rest 5 clients, they should all be in the same cluster
        for (int i = 0; i < 5; i++) {
            topology.addClient(new ClientNode("127.0.0.1", 9001 + i));
        }

        NetworkStructure network = topology.getNetwork();
        assertEquals(1, network.clusters().size());
        assertEquals(6, network.clusters().get(0).size()); // 1 server + 5 clients

        // Add one more client, a new cluster should be created
        topology.addClient(new ClientNode("127.0.0.1", 9006));

        network = topology.getNetwork();
        assertEquals(2, network.clusters().size());
        assertEquals(6, network.clusters().get(0).size());
        assertEquals(1, network.clusters().get(1).size());
    }

    @org.junit.jupiter.api.Test
    void testCloseTopology() {
        final Topology topology = Topology.getTopology();

        // Reset the topology
        final java.util.List<java.util.List<ClientNode>> clusters = new java.util.ArrayList<>();
        final java.util.List<ClientNode> servers = new java.util.ArrayList<>();
        final NetworkStructure emptyNetwork = new NetworkStructure(clusters, servers);
        topology.replaceNetwork(emptyNetwork);

        final Networking networking = Networking.getNetwork();

        final ClientNode server = new ClientNode("127.0.0.1", 9000); // Using a different port to avoid conflict
        networking.addUser(server, server);

        final ClientNode client = new ClientNode("127.0.0.1", 9001);
        networking.addUser(client, server);

        topology.closeTopology();
    }



    @org.junit.jupiter.api.Test

    void testUpdateNetworkRemoveClient() {

        final Topology topology = Topology.getTopology();

        // 1. Reset the topology to a clean state
        final java.util.List<java.util.List<ClientNode>> clusters = new java.util.ArrayList<>();
        final java.util.List<ClientNode> servers = new java.util.ArrayList<>();
        final NetworkStructure emptyNetwork = new NetworkStructure(clusters, servers);
        topology.replaceNetwork(emptyNetwork);

        // 2. Setup initial network with one cluster
        final ClientNode server = new ClientNode("127.0.0.1", 10000);
        topology.addUser(server, server); // This creates cluster 0 with the server
        final ClientNode client1 = new ClientNode("127.0.0.1", 10001);
        final ClientNode client2 = new ClientNode("127.0.0.1", 10002);

        topology.addClient(client1);
        topology.addClient(client2);

        NetworkStructure network = topology.getNetwork();
        assertEquals(1, network.clusters().size());
        assertEquals(3, network.clusters().get(0).size()); // server + client1 + client2
        assertEquals(server, network.servers().get(0));

        // 3. Test updateNetwork (add a new client)
        final ClientNode client3 = new ClientNode("127.0.0.1", 10003);
        final ClientNetworkRecord newClientRecord = new ClientNetworkRecord(client3, 0);
        topology.updateNetwork(newClientRecord);

        network = topology.getNetwork();
        assertEquals(1, network.clusters().size());
        assertEquals(4, network.clusters().get(0).size());
        assertEquals(true, topology.checkClientPresent(client3));

        // 4. Test removeClient (remove a non-server client)
        final ClientNetworkRecord removeClient1Record = new ClientNetworkRecord(client1, 0);
        topology.removeClient(removeClient1Record);

        network = topology.getNetwork();
        assertEquals(1, network.clusters().size());
        assertEquals(3, network.clusters().get(0).size());
        assertEquals(false, topology.checkClientPresent(client1));

        // 5. Test removeClient (remove a server client and see promotion)
        // First, add more clients to create a second cluster
        for (int i = 0; i < 3; i++) { // Cluster 0 will be full (was 3, now 3+3=6)
            topology.addClient(new ClientNode("127.0.0.1", 10004 + i));
        }

        final ClientNode newClusterServer = new ClientNode("127.0.0.1", 10007);
        topology.addClient(newClusterServer); // This will be the server of cluster 1
        final ClientNode newClusterClient = new ClientNode("127.0.0.1", 10008);
        topology.addClient(newClusterClient); // Add another client to the new cluster

        network = topology.getNetwork();
        assertEquals(2, network.clusters().size());
        assertEquals(6, network.clusters().get(0).size());
        assertEquals(2, network.clusters().get(1).size());
        assertEquals(newClusterServer, network.servers().get(1)); // Check server of new cluster

        // Now, remove the server of the second cluster
        int clusterIndexOfServer = topology.getClusterIndex(newClusterServer);
        final ClientNetworkRecord removeServerRecord = new ClientNetworkRecord(newClusterServer, clusterIndexOfServer);
        topology.removeClient(removeServerRecord);

        network = topology.getNetwork();
        assertEquals(2, network.clusters().size()); // Cluster should still exist
        assertEquals(1, network.clusters().get(1).size()); // Should have one client left
        assertEquals(false, topology.checkClientPresent(newClusterServer));

        // The other client should have been promoted to server
        assertEquals(newClusterClient, network.servers().get(1));

        // 6. Test removing the last client from a cluster (which is also the server)
        int clusterIndexOfLastClient = topology.getClusterIndex(newClusterClient);
        final ClientNetworkRecord removeLastClientRecord = new ClientNetworkRecord(newClusterClient, clusterIndexOfLastClient);
        topology.removeClient(removeLastClientRecord);

        network = topology.getNetwork();
        assertEquals(1, network.clusters().size()); // The second cluster should now be removed
        assertEquals(1, network.servers().size());

    }



    @org.junit.jupiter.api.Test
    void testMiscellaneous() {

        final Topology topology = Topology.getTopology();

        // 1. Reset the topology to a clean state
        final java.util.List<java.util.List<ClientNode>> clusters = new java.util.ArrayList<>();
        final java.util.List<ClientNode> servers = new java.util.ArrayList<>();
        final NetworkStructure emptyNetwork = new NetworkStructure(clusters, servers);
        topology.replaceNetwork(emptyNetwork);

        // 2. Test getClusterIndex returns -1 for a client not in the topology
        final ClientNode nonExistentClient = new ClientNode("127.0.0.1", 11000);
        int clusterIndex = topology.getClusterIndex(nonExistentClient);
        assertEquals(-1, clusterIndex);


        // 3. Test getClients returns null for invalid indices
        List<ClientNode> clients = topology.getClients(-1);
        assertNull(clients);


        clients = topology.getClients(0); // Topology is empty
        assertNull(clients);

        clients = topology.getClients(10); // Index out of bounds
        assertNull(clients);
    }


    @org.junit.jupiter.api.Test
    void testGetNetworkType() {

        final Topology topology = Topology.getTopology();

        // 1. Reset the topology and set up a multi-cluster network
        final java.util.List<java.util.List<ClientNode>> clusters = new java.util.ArrayList<>();
        final java.util.List<ClientNode> servers = new java.util.ArrayList<>();
        final NetworkStructure emptyNetwork = new NetworkStructure(clusters, servers);
        topology.replaceNetwork(emptyNetwork);

        final ClientNode serverC0 = new ClientNode("127.0.0.1", 12000);
        topology.addUser(serverC0, serverC0);
        final ClientNode clientC0 = new ClientNode("127.0.0.1", 12001);
        topology.addClient(clientC0);



        // Fill up cluster 0 to force creation of a new one
        for (int i = 0; i < 4; i++) {
            topology.addClient(new ClientNode("127.0.0.1", 12002 + i));
        }

        final ClientNode serverC1 = new ClientNode("127.0.0.1", 12006);
        topology.addClient(serverC1); // This client starts cluster 1

        // 2. Test SAME CLUSTER case (USE)
        int networkType = topology.getNetworkType(serverC0, clientC0);
        assertEquals(NetworkType.USE.ordinal(), networkType);

        // 3. Test OTHER CLUSTER case
        networkType = topology.getNetworkType(serverC0, serverC1);
        assertEquals(NetworkType.OTHERCLUSTER.ordinal(), networkType);
    }
}