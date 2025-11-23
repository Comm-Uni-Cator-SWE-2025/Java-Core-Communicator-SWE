package com.swe.dynamo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Queue;

import com.swe.dynamo.Parsers.Chunk;
import com.swe.dynamo.socket.ISocket;
import com.swe.dynamo.socket.SocketTCP;


public class Link {
    private final ISocket clientChannel;
    private ByteBuffer leftOverBuffer;

    private SocketPacket currentSocketPacket;

    private final Queue<Chunk> chunks;

    ByteBuffer readBuffer;

    public Node getRemoteAddress() {
        try {
            SocketAddress remoteAddress = clientChannel.getSocketChannel().getRemoteAddress();
            if (remoteAddress instanceof InetSocketAddress) {
                InetSocketAddress inetAddress = (InetSocketAddress) remoteAddress;
                return new Node((inetAddress.getHostString()), (short) inetAddress.getPort());
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Blocks to connect to the given port
     *
     * @param _port server port to connect to.
     */
    public Link(int _port) throws IOException {
        clientChannel = new SocketTCP();
        int bufferLength = 1024*10000;
        readBuffer = ByteBuffer.allocate(bufferLength);
        clientChannel.configureBlocking(true); // block till connection is established
        clientChannel.connect(new InetSocketAddress(_port));
        clientChannel.configureBlocking(false);
        chunks = new java.util.concurrent.ConcurrentLinkedQueue<>();
    }

    public Link(ISocket _connectedChannel) throws IOException {
        clientChannel = _connectedChannel;
        int bufferLength = 1024*10000;
        readBuffer = ByteBuffer.allocate(bufferLength);
        clientChannel.configureBlocking(false);
        chunks = new java.util.concurrent.ConcurrentLinkedQueue<>();
    }

    public void configureBlocking(boolean to_block) throws IOException {
        clientChannel.configureBlocking(to_block);
    }

    public void register(Selector selector) throws ClosedChannelException {
        if (clientChannel != null) {
            clientChannel.register(selector, SelectionKey.OP_READ, this);
        }
    }

    public void unregister(Selector selector) {
        if (clientChannel != null) {
            clientChannel.unregister(selector);
        }
    }

    public void close() {
        if (clientChannel != null) {
            try {
                clientChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * reads from the channel into the buffer
     * Puts the leftOverBuffer first then the data is read from the pipe
     * NOTE: Does **not** mark the leftOverBuffer as null. So it is responsibility
     * of callee to mark it null when utilized
     * throws Runtime Exception if not connected
     *
     * @return
     */
    private ByteBuffer readData() throws IOException {
        if (clientChannel == null) {
            // TODO : try to re-connect first
            throw new RuntimeException("Disconnected or never connected : ");
        }

        if (leftOverBuffer != null) {
            readBuffer.put(leftOverBuffer);
            leftOverBuffer = null;
            // This moves the pointer inside the readBuffer
        }
        int dataRead = clientChannel.read(readBuffer);
//        System.out.println("readData : " + dataRead);
        if (dataRead == -1) {
            throw new RuntimeException("Disconnected or never connected : ");
        }
//        System.out.println("readBuffer.position() : " + readBuffer.position());
        readBuffer.flip();
        ByteBuffer b = readBuffer.slice();
        readBuffer.clear();
        return b;
    }

    /**
     * reads and returns chunks received
     * Does not reentrant over the length of the array return
     * It could also be 0
     *
     * @return
     */
    public ArrayList<Chunk> getPackets() {
        try {
            readAndParseAllPackets();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
        ArrayList<Chunk> packetsToReturn = new ArrayList<>(chunks);
        chunks.clear();
        return packetsToReturn;
    }

    /**
     * Reads an int from the given data starting at the given position
     *
     * @param data the data to read from
     * @param pos  the position to start reading from
     * @return the int read
     */
    static int readInt(byte[] data, int pos) {
        if (data.length < pos) {
            throw new IllegalArgumentException("Data length is less than pos");
        }
        return ((data[pos] & 0xFF) << 24) |
            ((data[pos + 1] & 0xFF) << 16) |
            ((data[pos + 2] & 0xFF) << 8) |
            (data[pos + 3] & 0xFF);
    }

    void readAndParseAllPackets() throws IOException {
        boolean initialState = clientChannel.isBlocking();
        int dataRead = readAndParsePackets();
        clientChannel.configureBlocking(false);

        while (dataRead > 0) {
            dataRead = readAndParsePackets();
        }
        clientChannel.configureBlocking(initialState);
    }

    int readAndParsePackets() throws IOException {
        ByteBuffer buffer = readData();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        int currDatapos = 0;
        int len = data.length;
        while (currDatapos < len) {
            if (currentSocketPacket == null) {
                // expecting a `Packet`
                int left = len - currDatapos;
                if (left < 4) {
                    // got a leftover data store it for next parse.
                    leftOverBuffer = ByteBuffer.allocate(left);
                    leftOverBuffer.put(data, currDatapos, left);
                    leftOverBuffer.flip();
                    break;
                }
                int contentLength = readInt(data, currDatapos);
                currentSocketPacket = new SocketPacket(contentLength, new ByteArrayOutputStream());
                currDatapos += 4;
            }
            // check how much data is still to be read
            // and how much can be read
            int to_read = Math.min(currentSocketPacket.bytesLeft, data.length - currDatapos);
            currentSocketPacket.content.write(data, currDatapos, to_read);
            currentSocketPacket.bytesLeft -= to_read;
            currDatapos += to_read;
            if (currentSocketPacket.bytesLeft == 0) {
                chunks.add(Chunk.deserialize(currentSocketPacket.content.toByteArray()));
                currentSocketPacket = null;
            }
        }
        return data.length;
    }

    public Chunk getPacket() {
        try {
            readAndParseAllPackets();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        return chunks.poll();
    }

    public boolean sendPacket(Chunk chunk) {
        byte[] chunkData = chunk.serialize();

        ByteBuffer socketData = ByteBuffer.allocate(chunkData.length + 4);
        socketData.putInt(chunkData.length);
        socketData.put(chunkData);
        socketData.flip();
//        System.out.println("Sending " + chunkData.length + " bytes");
//        System.out.println("clientChannel : " + clientChannel);

        if (clientChannel != null) {
            try {
                boolean initialState = clientChannel.isBlocking();
                clientChannel.configureBlocking(false);
                long curr = System.nanoTime();
                while (socketData.hasRemaining()) {
                    clientChannel.write(socketData);
                }
                clientChannel.configureBlocking(initialState);
                System.out.println("Sending in : " + (System.nanoTime() - curr) / ((double) 1e6));
//                System.out.println("Sent " + chunkData.length + " Kb");
                return true;
            } catch (IOException e) {
                System.out.println("Error while writing" + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}

class SocketPacket {
    int bytesLeft;
    ByteArrayOutputStream content;

    public SocketPacket(int contentLength, ByteArrayOutputStream content) {
        this.bytesLeft = contentLength;
        this.content = content;
    }
}
