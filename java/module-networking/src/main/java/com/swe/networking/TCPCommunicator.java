/*
 * -----------------------------------------------------------------------------
 *  File: TCPCommunicator.java
 *  Owner: Loganath
 *  Roll Number : 112201016
 *  Module : Networking
 *
 * -----------------------------------------------------------------------------
 */

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

import com.swe.core.ClientNode;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

//File owned by Loganath
/**
 * Communicator class module for TCP.
 *
 */
public final class TCPCommunicator implements ProtocolBase {

    /**
     * Variable to store the name of the module.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("NETWORKING");

    /**
     * The module name.
     */
    private static final String MODULENAME = "[TCPCOMMUNICATOR]";
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
            LOG.info("TCP communicator initialized...");
            selector = Selector.open();
            deviceServerPort = serverPort;
            setServerPort();
        } catch (IOException ex) {
            LOG.error("Unable to initialize TCP comunicator...");
            LOG.error("Error : " + ex.getMessage());
        }
    }

    /**
     * Function to print all keys in the selector.
     */
    @Override
    public void printKeys() {
        selector.keys().stream().forEach(kay -> LOG.info("Selector channel: " + kay.channel()));
    }

    @Override
    public ReceivePacket receiveData() {
        try {
            final int timeout = 1000;
            selector.select(timeout);
            final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                final SelectionKey key = iter.next();
                iter.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    acceptConnection(key);
                } else if (key.isReadable()) {
                    final byte[] data = readData(key);
                    final String clientIp = ((SocketChannel) key.channel()).getRemoteAddress().toString();
                    final int clientPort = ((InetSocketAddress) ((SocketChannel) key.channel())
                            .getRemoteAddress()).getPort();
                    final ClientNode client = new ClientNode(clientIp, clientPort);
                    return new ReceivePacket(client, data);
                }
            }
            return null;
        } catch (IOException ex) {
            LOG.error("Error while using the selector...");
            LOG.error("Error : " + ex.getMessage());
            return null;
        }
    }

    /**
     * Function to start a TCP socket at the given port.
     */
    private void setServerPort() {
        try {
            receiveSocket = ServerSocketChannel.open();
            receiveSocket.bind(new InetSocketAddress(deviceServerPort));
            receiveSocket.configureBlocking(false);
            receiveSocket.register(selector, SelectionKey.OP_ACCEPT);
            LOG.info("Creating new server port at " + deviceServerPort);
        } catch (IOException ex) {
            LOG.error("Error connecting to port : " + deviceServerPort);
            LOG.error("Error : " + ex.getMessage());
        }
    }

    @Override
    public SocketChannel openSocket() {
        try {
            final SocketChannel socket = SocketChannel.open();
            return socket;
        } catch (IOException ex) {
            LOG.error("Error occurred while opening socket...");
            LOG.error("Error : " + ex.getMessage());
        }
        return null;
    }

    @Override
    public void closeSocket(final ClientNode client) {
        final SocketChannel clientSocket = clientSockets.get(client);
        if (clientSocket != null) {
            try {
                final SelectionKey key = clientSocket.keyFor(selector);
                if (key != null) {
                    key.cancel();
                    clientSocket.close();
                    clientSockets.remove(client);
                    LOG.info("Closing socket for client " + client + " ...");
                }
            } catch (IOException ex) {
                LOG.error("Error occurred while closing socket...");
                LOG.error("Error : " + ex.getMessage());
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
                LOG.info("Connection to " + dest + " exists already...");
                destSocket = clientSockets.get(dest);
            } else {
                destSocket = openSocket();
                destSocket.configureBlocking(true);
                LOG.info("Client : " + dest + " ...");
                destSocket.connect(new InetSocketAddress(destIp, destPort));
                destSocket.configureBlocking(false);
                LOG.info("Opening new socket at port " + destSocket.socket().getLocalPort());
                destSocket.register(selector, SelectionKey.OP_READ);
                LOG.info("New connection created successfully...");
                clientSockets.put(new ClientNode(destIp, destPort), destSocket);
            }
            synchronized (destSocket) {
                final ByteBuffer buffer = ByteBuffer.wrap(data);
                while (buffer.hasRemaining()) {
                    destSocket.write(buffer);
                }
            }
            printIpAddr(destIp, destPort);
        } catch (IOException ex) {
            LOG.error("Error while sending data...");
            LOG.error("Error : " + ex.getMessage());
        }
    }

    /**
     * Function to accept new connection.
     *
     * @param key the selection key for new connections
     */
    public void acceptConnection(final SelectionKey key) {
        try {
            LOG.info("Accepting new connection...");
            final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            final SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            final String ip = clientChannel.getRemoteAddress().toString().split(":")[0].replace("/", "");
            final int port = ((InetSocketAddress) clientChannel.getRemoteAddress()).getPort();
            final ClientNode client = new ClientNode(ip, port);
            clientSockets.put(client, clientChannel);
            LOG.info("New connection esthablished...");
            LOG.info("Client " + client + " ...");
        } catch (IOException ex) {
            LOG.error("Error occured while accepting connection...");
            LOG.error("Error : " + ex.getMessage());
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
            LOG.error("Bytes read : " + bytesRead);
            buffer.flip();
            final byte[] data = new byte[bytesRead];
            buffer.get(data);
            return data;
        } catch (IOException ex) {
            LOG.error("Error occured while reading data...");
            LOG.error("Error : " + ex.getMessage());
            return null;
        }
    }

    /**
     * Function to handle socket on termination.
     */
    @Override
    public void close() {
        try {
            LOG.info("Closing TCP communicator...");
            receiveSocket.close();
            for (SocketChannel socket : clientSockets.values()) {
                LOG.info("Closing socket of " + socket.getRemoteAddress() + "...");
                socket.close();
            }
        } catch (IOException ex) {
            LOG.error("Error occured while closing socket...");
            LOG.error("Error : " + ex.getMessage());
        }
    }

    private void printIpAddr(final String ipAddr, final Integer port) {
        LOG.info("Client: " + ipAddr + ":" + port);
    }
}
