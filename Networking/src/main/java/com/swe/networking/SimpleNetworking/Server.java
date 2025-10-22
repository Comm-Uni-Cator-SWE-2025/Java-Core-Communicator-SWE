package com.swe.networking.SimpleNetworking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;

/**
 * The main class for the server device.
 */
public class Server implements IUser {

    /**
     * The variable to store the device IP address.
     */
    private String deviceIp;
    /**
     * The variable to store the device port number.
     */
    private int devicePort;
    /**
     * The variable used by the server to connect to other devices.
     */
    private Socket sendSocket = new Socket();
    /**
     * The variable used by server to accept connections from clients.
     */
    private ServerSocket receiveSocket;
    /**
     * The singleton class object for packet parser.
     */
    private PacketParser parser;
    /**
     * The singleton class object for simplenetworking.
     */
    private SimpleNetworking simpleNetworking;
    /**
     * The variable to store the module type.
     */
    private ModuleType moduleType = ModuleType.NETWORKING;
    /**
     * The variable isused to store connection timeout.
     */
    private final int connectionTimeout = 5000;

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
        try {
            receiveSocket = new ServerSocket(devicePort);
            receiveSocket.setSoTimeout(0);
        } catch (IOException e) {
            System.err.println("Server1 Error: " + e.getMessage());
        }
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
                dataOut.write(parser.createPkt(0, 0,
                        module.ordinal(), 0, 0, addr, port, data));
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
        try {
            final Socket socket = receiveSocket.accept();
            final InputStream input = socket.getInputStream();
            final DataInputStream dataIn = new DataInputStream(input);
            final byte[] packet = dataIn.readAllBytes();
            System.out.println("Message received from " + socket.toString() + " ...");
            parsePacket(packet);
        } catch (SocketTimeoutException e) {
            System.err.println("Server3 Error: " + e.getMessage());
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
        final int module = parser.getModule(packet);
        final ModuleType type = moduleType.getType(module);
        final InetAddress address = parser.getIpAddress(packet);
        final int port = parser.getPortNum(packet);
        final String addr = address.getHostAddress();
        if (addr.equals(deviceIp) && port == devicePort) {
            final String data = new String(parser.getPayload(packet),
                    StandardCharsets.UTF_8);
            System.out.println("Server Data received : " + data);
            simpleNetworking.callSubscriber(packet, type);
        } else {
            final ClientNode dest = new ClientNode(address.getHostAddress(),
                    port);
            System.out.println("Redirecting data : " + dest.toString());
            final ClientNode[] dests = {dest };
            sendPkt(packet, dests, dest);
        }
    }

    /**
     * Function to be called on closing.
     */
    @Override
    public void closeUser() {
        try {
            receiveSocket.close();
        } catch (IOException e) {
        }
    }
}
