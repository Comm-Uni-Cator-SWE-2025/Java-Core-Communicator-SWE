package com.swe.networking;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class P2PServerTest {

    final ClientNode server = new ClientNode("127.0.0.1", 5000);
    final ClientNode deviceNode = new ClientNode("127.0.0.1", 5001);
    final ClientNode othClientNode = new ClientNode("127.0.0.1", 5002);

    final PacketParser packetParser = PacketParser.getPacketParser();
    final NetworkSerializer serializer = NetworkSerializer.getNetworkSerializer();
    final Topology topology = Topology.getTopology();

    public NetworkStructure getNetworkStructure(ClientNode p2pServer) throws UnknownHostException {
        List<List<ClientNode>> clusters = new ArrayList<>();
        List<ClientNode> clusterServers = new ArrayList<>();
        final NetworkStructure networkStructure = new NetworkStructure(clusters, clusterServers);

        for (int i = 0; i < 3; i++) {
            List<ClientNode> clusterClients = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                final ClientNode client = new ClientNode(
                    "127.0.0.1", 6000 + i * 10 + j);
                clusterClients.add(client);
                if( j == 0) {
                    networkStructure.servers().add(client);
                }
            }
            networkStructure.clusters().add(clusterClients);
        }
        // add one with device node
        List<ClientNode> deviceCluster = new ArrayList<>();
        for(int j = 0; j < 2; j++) {
            final ClientNode client = new ClientNode(
                "127.0.0.1", 7000 + j);
            deviceCluster.add(client);
        }
        deviceCluster.add(p2pServer);
        networkStructure.clusters().add(deviceCluster);
        networkStructure.servers().add(p2pServer);
        return networkStructure;
    }

    public void sendPacket(byte[] packet) {
        try {
            final Socket socket = new Socket(deviceNode.hostName(), deviceNode.port());
            final OutputStream out = socket.getOutputStream();
            out.write(packet);
            out.flush();
            socket.close();
        } catch (Exception e) {
            System.out.println("Exception in sending packet to " + deviceNode.hostName()
                               + ":" + deviceNode.port());
            e.printStackTrace();
        }
    }

    @Test
    public void testInitServer() throws UnknownHostException {
        P2PServer server = new P2PServer(this.deviceNode, this.server);
        final NetworkStructure networkStructure = getNetworkStructure(this.deviceNode);

        for (int i = 0; i < networkStructure.servers().size(); i++) {
            for (ClientNode c : networkStructure.clusters().get(i)) {
                server.monitor(c);
            }
        }
        // send network structure packet
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.USE.ordinal());
        packetInfo.setConnectionType(NetworkConnectionType.NETWORK.ordinal());
        packetInfo.setIpAddress(InetAddress.getByName(deviceNode.hostName()));
        packetInfo.setPortNum(deviceNode.port());
        packetInfo.setPayload(
            serializer.serializeNetworkStructure(networkStructure));
        final byte[] networkPacket = packetParser.createPkt(packetInfo);
        sendPacket(networkPacket);
        topology.replaceNetwork(networkStructure);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public P2PServer getServer() throws UnknownHostException {
        P2PServer server = new P2PServer(this.deviceNode, this.server);
        final NetworkStructure networkStructure = getNetworkStructure(this.deviceNode);

        for (int i = 0; i < networkStructure.servers().size(); i++) {
            for (ClientNode c : networkStructure.clusters().get(i)) {
                server.monitor(c);
            }
        }
        // send network structure packet
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.USE.ordinal());
        packetInfo.setConnectionType(NetworkConnectionType.NETWORK.ordinal());
        packetInfo.setIpAddress(InetAddress.getByName(deviceNode.hostName()));
        packetInfo.setPortNum(deviceNode.port());
        packetInfo.setPayload(
            serializer.serializeNetworkStructure(networkStructure));
        final byte[] networkPacket = packetParser.createPkt(packetInfo);
        sendPacket(networkPacket);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return server;
    }

    @Test
    public void handleSAMECLUSTER() throws UnknownHostException {
        P2PServer server = getServer();
        // send data packet
        final ClientNode destNode = new ClientNode(
            "127.0.0.1", 7000);
        byte[] testData = "This is a test data packet.".getBytes();
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.SAMECLUSTER.ordinal());
        packetInfo.setConnectionType(0);
        packetInfo.setPayload(testData);
        packetInfo.setIpAddress(InetAddress.getByName(destNode.hostName()));
        packetInfo.setPortNum(destNode.port());
        final byte[] dataPacket = packetParser.createPkt(packetInfo);
        // listen for incoming packet at destNode in a separate thread
        new Thread(() -> {
            byte[] receivedData = new byte[4096];
            try {
                final ServerSocket serverSocket = new ServerSocket(destNode.port());
                final Socket socket = serverSocket.accept();
                final OutputStream out = socket.getOutputStream();
                out.flush();
                int bytesRead = socket.getInputStream().read(receivedData);
                socket.close();
                serverSocket.close();
                System.out.println("data is : " + new String(receivedData, 0, bytesRead));
            } catch (Exception e) {
                System.out.println("Exception in receiving packet at " + destNode.hostName()
                                   + ":" + destNode.port());
                e.printStackTrace();
            }
        }).start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendPacket(dataPacket);
    }

    @Test
    public void handleOTHERCLUSTER() throws UnknownHostException {
        P2PServer server = getServer();
        // send data packet
        final ClientNode destNode = new ClientNode("127.0.0.1", 6000);
        byte[] testData = "This is a test data packet.".getBytes();
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.OTHERCLUSTER.ordinal());
        packetInfo.setConnectionType(0);
        packetInfo.setPayload(testData);
        packetInfo.setIpAddress(InetAddress.getByName(destNode.hostName()));
        packetInfo.setPortNum(destNode.port());
        final byte[] dataPacket = packetParser.createPkt(packetInfo);
        // listen for incoming packet at destNode in a separate thread
        new Thread(() -> {
            byte[] receivedData = new byte[4096];
            try {
                final ServerSocket serverSocket = new ServerSocket(destNode.port());
                final Socket socket = serverSocket.accept();
                final OutputStream out = socket.getOutputStream();
                out.flush();
                int bytesRead = socket.getInputStream().read(receivedData);
                socket.close();
                serverSocket.close();
                System.out.println("data is : " + new String(receivedData, 0, bytesRead));
            } catch (Exception e) {
                System.out.println("Exception in receiving packet at " + destNode.hostName()
                                   + ":" + destNode.port());
                e.printStackTrace();
            }
        }).start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendPacket(dataPacket);
    }

    @Test
    public void handleUSEHello() throws UnknownHostException {
        P2PServer server = getServer();
        // send data packet
        byte[] testData = "Hello guyssss.".getBytes();
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.USE.ordinal());
        packetInfo.setConnectionType(NetworkConnectionType.HELLO.ordinal());
        packetInfo.setIpAddress(InetAddress.getByName(deviceNode.hostName()));
        packetInfo.setPortNum(deviceNode.port());
        packetInfo.setPayload(testData);
        final byte[] dataPacket = packetParser.createPkt(packetInfo);
        sendPacket(dataPacket);
    }

    @Test
    public void handleUSEALIVE() throws UnknownHostException {
        P2PServer server = getServer();
        // send data packet
        byte[] testData = "As you can see, I'm not dead.".getBytes();
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.USE.ordinal());
        packetInfo.setConnectionType(NetworkConnectionType.ALIVE.ordinal());
        packetInfo.setIpAddress(InetAddress.getByName(deviceNode.hostName()));
        packetInfo.setPortNum(deviceNode.port());
        packetInfo.setPayload(testData);
        final byte[] dataPacket = packetParser.createPkt(packetInfo);
        sendPacket(dataPacket);
    }

    @Test
    public void handleUSEADD() throws UnknownHostException {
        P2PServer server = getServer();
        // same cluster
        ClientNode clientNode = new ClientNode("127.0.0.1", 1234);
        ClientNetworkRecord clientNetworkRecord = new ClientNetworkRecord(clientNode, 3);
        byte[] testData = "Welcome guys!".getBytes();
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.USE.ordinal());
        packetInfo.setConnectionType(NetworkConnectionType.ADD.ordinal());
        packetInfo.setIpAddress(InetAddress.getByName(deviceNode.hostName()));
        packetInfo.setPortNum(deviceNode.port());
        packetInfo.setPayload(serializer.serializeClientNetworkRecord(clientNetworkRecord));
        final byte[] dataPacket = packetParser.createPkt(packetInfo);
        sendPacket(dataPacket);

        // different cluster
        ClientNode newClientNode = new ClientNode("127.0.0.1", 1234);
        ClientNetworkRecord newClientNetworkRecord = new ClientNetworkRecord(newClientNode, 0);
        byte[] newTestData = "Welcome guys!".getBytes();
        PacketInfo newPacketInfo = new PacketInfo();
        newPacketInfo.setType(NetworkType.USE.ordinal());
        newPacketInfo.setConnectionType(NetworkConnectionType.ADD.ordinal());
        newPacketInfo.setIpAddress(InetAddress.getByName(deviceNode.hostName()));
        newPacketInfo.setPortNum(deviceNode.port());
        newPacketInfo.setPayload(serializer.serializeClientNetworkRecord(newClientNetworkRecord));
        final byte[] newDataPacket = packetParser.createPkt(newPacketInfo);
        sendPacket(newDataPacket);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void handleUSEREMOVE() throws UnknownHostException {
        P2PServer server = getServer();
        // same cluster
        ClientNode clientNode = new ClientNode("127.0.0.1", 7001);
        ClientNetworkRecord clientNetworkRecord = new ClientNetworkRecord(clientNode, 3);
        byte[] testData = "Goodbye guys!".getBytes();
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.USE.ordinal());
        packetInfo.setConnectionType(NetworkConnectionType.REMOVE.ordinal());
        packetInfo.setIpAddress(InetAddress.getByName(deviceNode.hostName()));
        packetInfo.setPortNum(deviceNode.port());
        packetInfo.setPayload(serializer.serializeClientNetworkRecord(clientNetworkRecord));
        final byte[] dataPacket = packetParser.createPkt(packetInfo);
        sendPacket(dataPacket);
        
        // different cluster
        ClientNode newClientNode = new ClientNode("127.0.0.1", 6011);
        ClientNetworkRecord newClientNetworkRecord = new ClientNetworkRecord(newClientNode, 1);
        byte[] newTestData = "Goodbye guys!".getBytes();
        PacketInfo newPacketInfo = new PacketInfo();
        newPacketInfo.setType(NetworkType.USE.ordinal());
        newPacketInfo.setConnectionType(NetworkConnectionType.REMOVE.ordinal());
        newPacketInfo.setIpAddress(InetAddress.getByName(deviceNode.hostName()));
        newPacketInfo.setPortNum(deviceNode.port());
        newPacketInfo.setPayload(serializer.serializeClientNetworkRecord(newClientNetworkRecord));
        final byte[] newDataPacket = packetParser.createPkt(newPacketInfo);
        sendPacket(newDataPacket);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void handleUSECLOSE() throws UnknownHostException {
        P2PServer server = getServer();
        // send data packet
        byte[] testData = "Adios".getBytes();
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setType(NetworkType.USE.ordinal());
        packetInfo.setConnectionType(NetworkConnectionType.CLOSE.ordinal());
        packetInfo.setIpAddress(InetAddress.getByName(deviceNode.hostName()));
        packetInfo.setPortNum(deviceNode.port());
        packetInfo.setPayload(testData);
        final byte[] dataPacket = packetParser.createPkt(packetInfo);
        sendPacket(dataPacket);
    }

}