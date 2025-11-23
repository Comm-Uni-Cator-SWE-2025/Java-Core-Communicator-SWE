package com.swe.dynamo;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.socketry.socket.SocketTCP;
import com.swe.dynamo.Parsers.Chunk;

public class Coil {


    final Selector selector;

    public Coil() throws IOException {
        selector = Selector.open();
    }

    final ArrayList<Link> links = new ArrayList<>();

    /**
     * Starts a server on the given port
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
        links.add(link);
    }

    public ArrayList<Chunk> listen() {
        ArrayList<Chunk> packets = new ArrayList<>();
        try {
            // Block until at least one channel is ready with some data to read
            int readyChannels = selector.select(1); // 100 milli-second timeout

            if (readyChannels == 0) {
                return new ArrayList<>();
            }

//            System.out.println("readyChannels : " + readyChannels);

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
//        System.out.println("packets : " + packets);

        return packets;
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        SocketTCP socket = new SocketTCP(clientChannel);
        Link link = new Link(socket);
        registerLink(link);
    }
}
