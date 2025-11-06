package com.swe.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Communicator class module for TCP.
 *
 */
public final class TCPCommunicator implements ProtocolBase {

    /**
     * The server socket used to receive client connections.
     *
     */
    private ServerSocketChannel receiveSocket;

    /**
     * The selector for the sockets.
     */
    private Selector selector;
    /**
     * The list of all connected clients and their sockets.
     *
     */
    private final HashMap<ClientNode, SocketChannel> clientSockets = new HashMap<>();

    /**
     * The port where the server is instantiated.
     *
     */
    private Integer deviceServerPort = 0;
    /**
     * The maximum time the socket must wait to connect to the destination.
     *
     */
    private final Integer clientConnectTimeout = 5000;
    // Integer clientSendTimeout = 2000;

    /**
     * The size of the buffer to read data. Let it be the size of 15KB.
     */
    private final Integer byteBufferSize = 15 * 1024;

    // maintain list of clients and add timeouts
    /**
     * Constructor function for TCP Communicator class.
     *
     * @param serverPort which port to start the TCP.
     */
    public TCPCommunicator(final int serverPort) {
        try {
            System.out.println("TCP communicator initialized...");
            selector = Selector.open();
            deviceServerPort = serverPort;
            setServerPort();
        } catch (IOException ex) {
            System.out.println("Not able to initialize TCP communicator...");
        }
    }

    @Override
    public byte[] receiveData() {
        try {
            selector.select();
            final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                final SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    acceptConnection(key);
                } else if (key.isReadable()) {
                    final byte[] data = readData(key);
                    return data;
                }
            }
            return null;
        } catch (IOException ex) {
            // System.out.println("Error while using selector...");
            return null;
        }
    }

    private void setServerPort() {
        try {
            receiveSocket = ServerSocketChannel.open();
            receiveSocket.bind(new InetSocketAddress(deviceServerPort));
            receiveSocket.configureBlocking(false);
            receiveSocket.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Creating new server port at " + deviceServerPort + " ...");
        } catch (IOException ex) {
            System.out.println("Error while opening a socket at this port");
        }
    }

    @Override
    public SocketChannel openSocket() {
        System.out.println("Opening new socket...");
        try {
            final SocketChannel socket = SocketChannel.open();
            return socket;
        } catch (IOException ex) {
            System.out.println("Error occurred while opening socket...");
        }
        return null;
    }

    @Override
    public void closeSocket(final ClientNode client) {
        final SocketChannel clientSocket = clientSockets.get(client);
        if (clientSocket != null) {
            try {
                clientSocket.close();
                clientSockets.remove(client);
                System.out.println("Closed socket for " + client.hostName() + ":" + client.port());
            } catch (IOException ex) {
                System.out.println("Error occurred while closing socket...");
            }
        }
    }

    @Override
    public void sendData(final byte[] data, final ClientNode dest) {
        final String destIp = dest.hostName();
        final Integer destPort = dest.port();
        try {
            final SocketChannel destSocket;
            if (clientSockets.containsKey(dest)) {
                System.out.println("Connection exists already...");
                destSocket = clientSockets.get(dest);
            } else {
                destSocket = openSocket();
                destSocket.configureBlocking(true);
                System.out.println("Waiting for " + clientConnectTimeout + " s to connect to the client...");
                System.out.println("Client: " + dest + " ...");
                destSocket.socket().connect(new InetSocketAddress(destIp, destPort), clientConnectTimeout);
                destSocket.configureBlocking(false);
                destSocket.register(selector, SelectionKey.OP_READ);
                System.out.println("New connection created successfully...");
                clientSockets.put(new ClientNode(destIp, destPort), destSocket);
            }
            final ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.hasRemaining()) {
                destSocket.write(buffer);
            }
            printIpAddr(destIp, destPort);
        } catch (Exception ex) {
            System.out.println("Error occured while sending data.... " + ex.getMessage());
        }
    }

    /**
     * Function to accept new connection.
     *
     * @param key the selection key for new connections
     */
    public void acceptConnection(final SelectionKey key) {
        try {
            final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            final SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            final String ip = clientChannel.getRemoteAddress().toString().split(":")[0].replace("/", "");
            final int port = ((InetSocketAddress) clientChannel.getRemoteAddress()).getPort();
            clientSockets.put(new ClientNode(ip, port), clientChannel);
            System.out.println("Accepted new connection from " + ip + ":" + port);
        } catch (IOException ex) {
            System.out.println("Error occured while accepting connection...");
        }
    }

    /**
     * Function to read data from available socket.
     *
     * @param key the key for the socket
     * @return the data ead
     */
    public byte[] readData(final SelectionKey key) {
        try {
            final ByteBuffer buffer = ByteBuffer.allocate(byteBufferSize);
            final SocketChannel clientChannel = (SocketChannel) key.channel();
            final int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                return null;
            }
            System.out.println("bytes read..." + bytesRead);
            buffer.flip();
            final byte[] data = new byte[bytesRead];
            buffer.get(data);
            return data;
        } catch (IOException ex) {
            // System.out.println("Error while reading data from client Channel...");
            return null;
        }
    }

    /**
     * Function to handle socket on termination.
     */
    @Override
    public void close() {
        try {
            System.out.println("Closing TCP communicator");
            receiveSocket.close();
            for (SocketChannel socket : clientSockets.values()) {
                socket.close();
            }
        } catch (IOException ex) {
        }
    }

    private void printIpAddr(final String ipAddr, final Integer port) {
        System.out.println("Client: " + ipAddr + ":" + port);
    }
}
