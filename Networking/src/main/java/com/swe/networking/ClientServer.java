package com.swe.networking;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Implement the following.
 * > a send function from client and server perspective
 * > a receive function from the server
 * > The server send function can use the interface abstractTopology
 * to send it to the topology for sending to another cluster
 */
public class ClientServer {

    private final InetAddress hostName;
    private final int port;

    private final InetAddress serverHostname;
    private final int serverPort;

    record ClientNode(InetAddress hostName, int port) {}

    private final List<ClientNode> clients;
    private final ServerSocket receiveSocket;

    public ClientServer(InetAddress _serverHostname, int _serverPort) throws IOException {
        try {
            this.hostName = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("Could not get IP address: " + e.getMessage());
            throw new RuntimeException("Failed to get local host IP", e);
        }
        this.port = 12000;

        this.serverHostname = _serverHostname;
        this.serverPort = _serverPort;

        this.clients = new ArrayList<>();

        try {
            this.receiveSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("TCP server error: " + e.getMessage());
            throw e;
        }

        if (serverHostname.equals(hostName)) {
            clients.add(new ClientNode(hostName, port));
        } else {
            sendHello(hostName, port);
        }
    }

    private boolean destInCluster(InetAddress ip, int port) {
        return clients.contains(new ClientNode(ip, port));
    }

    public void receiveFrom() {
        try (Socket commSocket = this.receiveSocket.accept()) {
            DataInputStream dataIn = new DataInputStream(commSocket.getInputStream());
            byte[] packet = dataIn.readAllBytes();

            InetAddress dest = commSocket.getInetAddress();
            int port = commSocket.getPort();

            // Assuming PacketParser is a custom class you have defined elsewhere
            PacketParser parser = PacketParser.getPacketParser();
            System.out.println("Received packet from: " + dest + ":" + port);

            if (serverHostname.equals(hostName)) {
                int type = parser.getType(packet);
                int connectionType = parser.getConnectionType(packet);

                if (type == 3 && connectionType == 0) {
                    receiveHello(parser.getPayload(packet));
                }
            } else {
                int type = parser.getType(packet);
                int connectionType = parser.getConnectionType(packet);

                if (type == 3 && connectionType == 0) {
                    receiveHello(parser.getPayload(packet));
                }
                // TODO: callMessageListener()
            }
            System.out.println("Packet processed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends data to a specified destination. This function preserves the original
     * if/else structure but fixes the socket handling to prevent errors.
     * @return 0 on success, -1 on failure.
     */
    public int sendTo(final byte[] data, final InetAddress dest, final int port) {
        // --- This is the Server's logic ---
        if (serverHostname.equals(hostName)) {
            // If destination is another client in this server's cluster
            if (destInCluster(dest, port)) {
                try (Socket socket = new Socket(dest, port)) {
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Server error sending to in-cluster client: " + e.getMessage());
                    e.printStackTrace();
                    return -1;
                }
            } else {
                // If destination is in another cluster (logic to be implemented via Topology)
                // The original logic was commented out, so it is preserved here.
                /*
                try (Socket socket = new Socket(topology.getServer(dest).hostName(), port)) {
                    // TODO: getServer to be implemented by Topology.
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Server error sending to other cluster: " + e.getMessage());
                    e.printStackTrace();
                    return -1;
                }
                */
            }
            // --- This is the Client's logic ---
        } else {
            // If destination is another client in the same cluster
            if (destInCluster(dest, port)) {
                try (Socket socket = new Socket(dest, port)) {
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Client error sending to peer: " + e.getMessage());
                    e.printStackTrace();
                    return -1;
                }
            } else {
                // If destination is outside the cluster, send it to our server to forward it
                try (Socket socket = new Socket(serverHostname, serverPort)) {
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Client error sending to server for forwarding: " + e.getMessage());
                    e.printStackTrace();
                    return -1;
                }
            }
        }
        return 0;
    }

    private long createPacketHeader(int type, int priority, int module, int connectionType, int broadcast, int empty, int ip, int port) {
        long packetHeader = 0;
        packetHeader |= ((long) type << 62);
        packetHeader |= ((long) priority << 59);
        packetHeader |= ((long) module << 55);
        packetHeader |= ((long) connectionType << 52);
        packetHeader |= ((long) broadcast << 51);
        packetHeader |= ((long) empty << 48);
        packetHeader |= ((long) ip << 16);
        packetHeader |= port;
        return packetHeader;
    }

    private void sendHello(InetAddress myIp, int myPort) throws IOException {
        long packetHeader = createPacketHeader(3, 0, 0, 0, 0, 0, 0, 0);
        byte[] addressBytes = myIp.getAddress();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putLong(packetHeader);
        buffer.putInt(addressBytes.length);
        buffer.put(addressBytes);
        buffer.putInt(myPort);

        byte[] packet = new byte[buffer.position()];
        buffer.flip();
        buffer.get(packet);

        this.sendTo(packet, serverHostname, serverPort);
    }

    private void receiveHello(byte[] data) {
        if (serverHostname.equals(hostName)) {
            long packetHeader = createPacketHeader(3, 0, 0, 0, 0, 0, 0, 0);
            ByteBuffer broadcastBuffer = ByteBuffer.allocate(1024);
            broadcastBuffer.putLong(packetHeader);
            broadcastBuffer.put(data);

            byte[] packet = new byte[broadcastBuffer.position()];
            broadcastBuffer.flip();
            broadcastBuffer.get(packet);

            for (ClientNode client : clients) {
                if (!client.hostName.equals(this.hostName)) {
                    this.sendTo(packet, client.hostName, client.port);
                }
            }
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        try {
            int ipSize = buffer.getInt();
            byte[] addressBytes = new byte[ipSize];
            buffer.get(addressBytes);
            InetAddress ip = InetAddress.getByAddress(addressBytes);
            int port = buffer.getInt();

            ClientNode newClient = new ClientNode(ip, port);
            if (!clients.contains(newClient)) {
                clients.add(newClient);
                System.out.println("Added new client to cluster: " + newClient);
            }

        } catch (UnknownHostException e) {
            System.err.println("Error parsing IP address from hello packet.");
            e.printStackTrace();
        } catch (java.nio.BufferUnderflowException e) {
            System.err.println("Error parsing hello packet, buffer underflow. Packet may be malformed.");
            e.printStackTrace();
        }
    }
}