package com.swe.dynamo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import com.swe.dynamo.Parsers.Chunk;
import com.swe.dynamo.socket.ISocket;
import com.swe.dynamo.socket.SocketTCP;

public class Coil {
    final boolean isMainServer;
    final Selector selector;
    final Function<Node, Void> sendNodeList;

    public Coil(boolean isMainServer, Function<Node, Void> sendNodeList) throws IOException {
        this.isMainServer = isMainServer;
        selector = Selector.open();
        this.sendNodeList = sendNodeList;
    }

    private HashMap<Node, Link> nodeLinks = new HashMap<>();

    public Node[] getNodeList() {

        // get all ip addresses of the nodes
        HashSet<String> ipAddresses = new HashSet<>();
        for (Node node : nodeLinks.keySet()) {
            ipAddresses.add(node.IPToString());
        }

        // get all nodes from the ip addresses
        Node[] nodes = new Node[ipAddresses.size()];
        int i = 0;
        for (String ipAddress : ipAddresses) {
            nodes[i++] = new Node(ipAddress, (short) 1212);
        }

        return nodes;

    }

    public void connectToNode(Node node) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        int port = node.getPortInt();

        // Start the connection (returns immediately in non-blocking mode)
        socketChannel.connect(new InetSocketAddress(node.IPToString(), port));

        // Create a temporary selector for connection timeout
        Selector connectSelector = Selector.open();
        socketChannel.register(connectSelector, SelectionKey.OP_CONNECT);

        // Wait for connection with 1 second timeout
        int ready = connectSelector.select(1000); // 1000 ms = 1 second

        if (ready == 0) {
            // Timeout occurred
            socketChannel.close();
            connectSelector.close();
            throw new IOException("Connection timeout after 1 second to " + node.IPToString() + ":" + node.getPort());
        }

        System.out.println("Connection established to " + node.IPToString() + ":" + node.getPort() + " but not finished" + socketChannel.isConnected() + " " + socketChannel.finishConnect());
        // Complete the connection
        if (socketChannel.finishConnect()) {
            connectSelector.close();
            ISocket socket = new SocketTCP(socketChannel);
            System.out.println("Connection established to " + node.IPToString() + ":" + node.getPort());
            registerLink(new Link(socket));
        } else {
            socketChannel.close();
            connectSelector.close();
            throw new IOException("Failed to complete connection to " + node.IPToString() + ":" + node.getPort());
        }

        // nodeLinks.put(node, new Link(new SocketTCP(socketChannel)));
    }

    /** 
     * send data to the given node
     * @param node the node to send the data to
     * @param chunk the chunk to send
     * @throws IOException if the data cannot be sent
     */
    public void sendData(Node node, Chunk chunk) throws IOException {
        Link link = nodeLinks.get(node);
        if (link == null) {
            try {
                connectToNode(node);
            } catch (IOException e) {
                // close and remove the link
                throw e;
            }
            link = nodeLinks.get(node);
            System.out.println("Link found for node " + node + " " + link);
            if (link == null) {
                throw new IOException("Failed to establish link to node " + node + " after connection");
            }
        }
        if (!link.sendPacket(chunk)) {
            unregisterLink(link);
            throw new IOException("Failed to send data to node " + node);
        }
    }

    /**
     * Starts a server on the given port
     * 
     * @param port the port to start the server on
     * @throws IOException if the server cannot be started
     */
    public void startServer(int port) throws IOException {
        // Create and configure the server socket channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));

        // Register the server socket with the selector for ACCEPT operations
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port " + port);
    }

    private void registerLink(Link link) throws ClosedChannelException {
        link.register(selector);
        Node remoteAddress = link.getRemoteAddress();
        System.out.println("Registering link to " + remoteAddress);
        nodeLinks.put(remoteAddress, link);
        System.out.println("Node links: " + nodeLinks.keySet() + " " + nodeLinks.values());
    }

    /**
     * Unregisters the link and closes the socket
     * @param link the link to unregister
     */
    public void unregisterLink(Link link) {
        link.close();
        link.unregister(selector);
        Node remoteAddress = link.getRemoteAddress();
        nodeLinks.remove(remoteAddress);
    }

    public void listenLoop(Function<Chunk, Void> handleChunk) {
        while (true) {
            ArrayList<Chunk> packets = listen();
            packets.forEach(packet -> {
                handleChunk.apply(packet);
            });
        }
    }

    private ArrayList<Chunk> listen() {
        ArrayList<Chunk> packets = new ArrayList<>();
        try {
            // Block until at least one channel is ready with some data to read
            int readyChannels = selector.select(1); // 100 milli-second timeout

            if (readyChannels == 0) {
                return new ArrayList<>();
            }

            // System.out.println("readyChannels : " + readyChannels);

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove(); // drain the selected-key set
                if (!key.isValid()) {
                    continue;
                }

                if (key.isReadable()) {
                    Link link = (Link) key.attachment();
                    packets.addAll(link.getPackets());
                }

                if (key.isAcceptable()) {
                    acceptConnection(key);
                }
                // System.out.println("packets : " + packets);
            }

        } catch (IOException e) {
            System.err.println("Error in selector loop: " + e.getMessage());
            e.printStackTrace();
        }
        // System.out.println("packets : " + packets);

        return packets;
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        System.out.println("Accepting connection");
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        SocketTCP socket = new SocketTCP(clientChannel);
        Link link = new Link(socket);
        registerLink(link);
        if (isMainServer) {
            sendNodeList.apply(link.getRemoteAddress());
        }
    }
}
