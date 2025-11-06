package com.swe.networking.SimpleNetworking;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.PacketInfo;
import com.swe.networking.PacketParser;
import com.swe.networking.ProtocolBase;
import com.swe.networking.TCPCommunicator;

/**
 * The main class for the server device.
 */
public class Server implements IUser {

    /**
     * The variable to store the device IP address.
     */
    private final String deviceIp;
    /**
     * The variable to store the device port number.
     */
    private final int devicePort;
    /**
     * The variable used by the server to connect to other devices.
     */
    private Socket sendSocket = new Socket();
    /**
     * The variable used by server to accept connections from clients.
     */
    private final ProtocolBase receiveSocket;
    /**
     * The singleton class object for packet parser.
     */
    private final PacketParser parser;
    /**
     * The singleton class object for simplenetworking.
     */
    private final SimpleNetworking simpleNetworking;
    /**
     * The variable to store the module type.
     */
    private final ModuleType moduleType = ModuleType.NETWORKING;
    /**
     * The variable isused to store connection timeout.
     */
    private final int connectionTimeout = 5000;

    /** The variable to store chunk Manager. */
    private SimpleChunkManager chunkManager;

    /** The variable to store chunk Manager payload size. */
    private final int payloadSize = 15 * 1024;

    /**
     * The constructor function for the server class.
     *
     * @param deviceAddr the device IP address details
     */
    public Server(final ClientNode deviceAddr) {
        deviceIp = deviceAddr.hostName();
        devicePort = deviceAddr.port();
        parser = PacketParser.getPacketParser();
        simpleNetworking = SimpleNetworking.getSimpleNetwork();
        receiveSocket = new TCPCommunicator(devicePort);
        chunkManager = SimpleChunkManager.getChunkManager(payloadSize);
    }

    /**
     * Function to send the data to a list of destination.
     *
     * @param data     the data to be sent
     * @param destIp   the list fo destination to send the data
     * @param serverIp the Ip address of the main server
     * @param module   the module to send th data to
     */
    @Override
    public void send(final byte[] data, final ClientNode[] destIp,
            final ClientNode serverIp, final ModuleType module) {
        for (ClientNode client : destIp) {
            final String ip = client.hostName();
            final int port = client.port();
            try {
                sendSocket = new Socket();
                sendSocket.connect(new InetSocketAddress(ip, port),
                        connectionTimeout);
                final OutputStream output = sendSocket.getOutputStream();
                final DataOutputStream dataOut = new DataOutputStream(output);
                final InetAddress addr = InetAddress.getByName(ip);
                dataOut.write(data);
                System.out.println("Sent data succesfully...");
                sendSocket.close();
            } catch (IOException e) {
                System.err.println("Server2 Error: " + e.getMessage());
            }
        }
    }

    /**
     * Function to receive data from the given socket.
     */
    @Override
    public void receive() throws IOException {
        while (true) {
            final byte[] packet = receiveSocket.receiveData();
            if (packet != null) {
                parsePacket(packet);
            }
        }
    }

    /**
     * Function to send a packet directly instead of creating packet.
     * Used in case of redirecting packets.
     *
     * @param packet   the packet to send
     * @param destIp   the list of destination to send the packet
     * @param serverIp the main server IP address details
     */
    public void sendPkt(final byte[] packet, final ClientNode[] destIp,
            final ClientNode serverIp) {
        for (ClientNode client : destIp) {
            final String ip = client.hostName();
            final int port = client.port();
            try {
                sendSocket = new Socket();
                sendSocket.connect(new InetSocketAddress(ip, port),
                        connectionTimeout);
                final OutputStream output = sendSocket.getOutputStream();
                final DataOutputStream dataOut = new DataOutputStream(output);
                dataOut.write(packet);
                System.out.println("Sent data succesfully...");
                sendSocket.close();
            } catch (IOException e) {
                System.err.println("Server2 Error: " + e.getMessage());
            }
        }
    }

    /**
     * Function to parse the received packet and perform required response.
     *
     * @param packet the packet to parse
     * @throws UnknownHostException throws when sending data to unknown host
     */
    public void parsePacket(final byte[] packet) throws UnknownHostException {
        final PacketInfo pktInfo = parser.parsePacket(packet);
        final int module = pktInfo.getModule();
        final ModuleType type = moduleType.getType(module);
        final InetAddress address = pktInfo.getIpAddress();
        final int port = pktInfo.getPortNum();
        final String addr = address.getHostAddress();
        if (addr.equals(deviceIp) && port == devicePort) {
            final String data = new String(pktInfo.getPayload(),
                    StandardCharsets.UTF_8);
//            System.out.println("Server Data received : " + data);
            final byte[] message = chunkManager.addChunk(packet);
            System.out.println("Server Data length received : " + data.length());
            System.out.println("Server Module received : " + type);
            if (message != null) {
                simpleNetworking.callSubscriber(message, type);
            }
        } else {
            final ClientNode dest = new ClientNode(address.getHostAddress(),
                    port);
            System.out.println("Redirecting data : " + dest.toString());
            final ClientNode[] dests = { dest };
            sendPkt(packet, dests, dest);
        }
    }

    /**
     * Function to be called on closing.
     */
    @Override
    public void closeUser() {
        receiveSocket.close();
    }
}
