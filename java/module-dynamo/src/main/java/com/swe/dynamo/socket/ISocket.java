package com.swe.dynamo.socket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public interface ISocket {

    int read(ByteBuffer dst) throws IOException;

    int write(ByteBuffer src) throws IOException;

    SelectableChannel configureBlocking(boolean block)
        throws IOException;

    boolean connect(SocketAddress remote) throws IOException;

    void unregister(Selector sel);

    void close() throws IOException;

    SocketChannel getSocketChannel();

    boolean isConnected();

    boolean isBlocking();

    SelectionKey register(Selector sel, int ops, Object att)
        throws ClosedChannelException;

    default SelectionKey register(Selector sel, int ops)
        throws ClosedChannelException {
        return register(sel, ops, null);
    }

}
