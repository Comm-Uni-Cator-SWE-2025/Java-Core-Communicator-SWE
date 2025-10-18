package com.swe.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Communicator class module for TCP.
 *
 */
public final class TCPCommunicator implements ProtocolBase {
    /**
     * The singleton TCP communicator class.
     **/
    private static TCPCommunicator tcpCommunicator = null;
    /**
     * The server socket used to receive client connections.
     **/
    private ServerSocket receiveSocket;
    /**
     * The list of all connected clients and their sockets.
     **/
    private final HashMap<String, Socket> clientSockets;
    // HashMap<String, Long> clientSocketTimeouts;

    /**
     * The port where the server is instantiated.
     **/
    private final Integer deviceServerPort = 8000;
    /**
     * The maximum time the socket must wait to connect to the destination.
     **/
    private final Integer clientConnectTimeout = 5000;
    // Integer clientSendTimeout = 2000;

    // maintain list of clients and add timeouts
    private TCPCommunicator() {
        System.out.println("TCP communicator initialized...");
        clientSockets = new HashMap<>();
        // clientSocketTimeouts = new HashMap<>();
        setServerPort();
    }

    /**
     * The static class to get the TCP communicator object.
     *
     * @return the static object instantiated
     */
    public static ProtocolBase getTCPCommunicator() {
        if (tcpCommunicator == null) {
            System.out.println("Creating new TCP communicator object...");
            tcpCommunicator = new TCPCommunicator();
        }
        System.out.println("Already instantiated TCP communicator object...");
        return tcpCommunicator;
    }

    private void setServerPort() {
        try {
            receiveSocket = new ServerSocket(deviceServerPort);
            receiveSocket.setSoTimeout(0);
            System.out.println(
                    "Creating new server port at " + deviceServerPort + " ...");
        } catch (IOException ex) {
            System.out.println(
                    "An error occured while setting server port to "
                            + deviceServerPort + " ...");
        }
    }

    @Override
    public Socket openSocket() {
        System.out.println("Opening new socket...");
        return new Socket();
    }

    @Override
    public void closeSocket(final String client) {
        try {
            final Socket clientSocket = clientSockets.get(client);
            clientSockets.remove(client);
            clientSocket.close();
            System.out.println("Closing socket for client " + client + " ...");
        } catch (IOException ex) {
            System.out.println("Error occured while closing Socket...");
        }
    }

    @Override
    public void sendData(final byte[] data, final ClientNode dest) {
        final String destIp = dest.hostName();
        final Integer destPort = dest.port();
        try {
            if (clientSockets.containsKey(destIp)) {
                System.out.println("Connection exists already...");
                final Socket destSocket = clientSockets.get(destIp);
                final OutputStream output = destSocket.getOutputStream();
                final DataOutputStream dataOut = new DataOutputStream(output);
                dataOut.write(data);
                return;
            }
            final Socket destSocket = openSocket();
            System.out.println("Waiting for "
                    + clientConnectTimeout + " s to connect to the client...");
            destSocket.connect(new InetSocketAddress(destIp, destPort),
                    clientConnectTimeout);
            System.out.println("New connection created successfully...");
            final OutputStream output = destSocket.getOutputStream();
            final DataOutputStream dataOut = new DataOutputStream(output);
            dataOut.write(data);
            printIpAddr(destIp, destPort);
            clientSockets.put(destIp, destSocket);
        } catch (IOException ex) {
            System.out.println("Error occured while sending data.... ");
            printIpAddr(destIp, destPort);
        }
    }

    @Override
    public void receiveData() {
        try {
            // TODO Run the receiveData function in an infinite loop
            final Socket socket = receiveSocket.accept();
            final InputStream input = socket.getInputStream();
            final DataInputStream dataIn = new DataInputStream(input);
            final byte[] packet = dataIn.readAllBytes();
            System.out.println("Received data from "
                    + socket.getInetAddress().getHostAddress() + " ...");
            System.out.println("Data: " + new String(packet) + " ...");
            // TODO call a method in chunk manager
            // to parse the data or retrun the bytes
        } catch (IOException ex) {
        }
    }

    private void printIpAddr(final String ipAddr, final Integer port) {
        System.out.println("Client: " + ipAddr + ":" + port);
    }
}
