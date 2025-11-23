package com.swe.dynamo;


import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Coil {


    final Selector selector;

    public Coil() throws IOException {
        selector = Selector.open();
    }

    final ArrayList<Link> links = new ArrayList<>();

    

    private void registerLink(Link link) throws ClosedChannelException {
        link.register(selector);
        links.add(link);
    }

    public ArrayList<Packet> listen() {
        ArrayList<Packet> packets = new ArrayList<>();
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
        ArrayList<Packet> packetsToReturn = new ArrayList<>();
        // feed each packet received
        for (Packet packet : packets) {
            Packet feededPacket = feedPacket(packet);
            if (feededPacket != null) {
                packetsToReturn.add(feededPacket);
            }
        }

        return packetsToReturn;
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        Link link = new Link(clientChannel);
        registerLink(link);
    }
}
