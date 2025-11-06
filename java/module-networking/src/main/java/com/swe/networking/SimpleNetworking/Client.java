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
import com.swe.networking.PacketInfo;
import com.swe.networking.PacketParser;

/**
 * The main module of the client device.
 */
public class Client implements IUser {

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
     * The variable to store chunk Manager.
     */
    private SimpleChunkManager chunkManager;

    /**
     * The variable to store chunk Manager payload size.
     */
    private final int payloadSize = 15 * 1024;

    /**
     * The constructor function for the client class.
     *
     * @param deviceAddr the device IP address details
     */
    public Client(final ClientNode deviceAddr) {
        deviceIp = deviceAddr.hostName();
        devicePort = deviceAddr.port();
        parser = PacketParser.getPacketParser();
        simpleNetworking = SimpleNetworking.getSimpleNetwork();
        chunkManager = SimpleChunkManager.getChunkManager(payloadSize);
        try {
            receiveSocket = new ServerSocket(devicePort);
            receiveSocket.setSoTimeout(0);
        } catch (IOException e) {
            System.err.println("Client1 Error: " + e.getMessage());
        }
    }

    /**
     * Function to send the data to a list of destination.
     *
     * @param data the data to be sent
     * @param destIp the list fo destination to send the data
     * @param serverIp the Ip address of the main server
     * @param module the module to send th data to
     */
    @Override
    public void send(final byte[] data, final ClientNode[] destIp,
            final ClientNode serverIp, final ModuleType module) {
        for (ClientNode client : destIp) {
            final String ip = client.hostName();
            final int port = client.port();
            try {
                sendSocket = new Socket();
                sendSocket.connect(new InetSocketAddress(serverIp.hostName(),
                        serverIp.port()), connectionTimeout);
                final OutputStream output = sendSocket.getOutputStream();
                final DataOutputStream dataOut = new DataOutputStream(output);
                final InetAddress addr = InetAddress.getByName(ip);
                dataOut.write(data);
                System.out.println("Sent data succesfully...");
                sendSocket.close();
            } catch (IOException e) {
                System.err.println("Client2 Error: " + e.getMessage());
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
            parsePacket(packet);
        } catch (SocketTimeoutException e) {
            System.err.println("Client3 Error: " + e.getMessage());
        }
    }

    /**
     * Function to parse the received packet and perform required response.
     *
     * @param packet the packet to parse
     */
    public void parsePacket(final byte[] packet) {
        try {
            final PacketInfo pktInfo = parser.parsePacket(packet);
            final int module = pktInfo.getModule();
            System.out.println("Module : " + module);
            final ModuleType type = moduleType.getType(module);
            System.out.println("Module : " + type);
            final String data = new String(pktInfo.getPayload(),
                    StandardCharsets.UTF_8);
            System.out.println("Client Data received : " + data);
            byte[] message = chunkManager.addChunk(packet);
            System.out.println("Client Data length received : " + data.length());
            System.out.println("Client Module received : " + type);
            if (message != null) {
                final PacketInfo newpktInfo = parser.parsePacket(message);
                message = newpktInfo.getPayload();
                simpleNetworking.callSubscriber(message, type);
            }
        } catch (UnknownHostException ex) {
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
