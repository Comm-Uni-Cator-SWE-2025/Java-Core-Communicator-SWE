package com.swe.dynamo;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.dynamo.Parsers.Chunk;
import com.swe.dynamo.Parsers.Frame;

record PendingPacket(Chunk chunk, Node reciever) {

}

public class Dynamo {
    // Singleton instance
    private static final Dynamo INSTANCE = new Dynamo();

    private Coil socketry;

    // Private constructor to prevent instantiation
    private Dynamo() {
        try {
            socketry = new Coil();
            packetQueues = new ConcurrentLinkedQueue[5];
            for (int i = 0; i < 5; i++) {
                packetQueues[i] = new ConcurrentLinkedQueue<>();
            }
            subscriptions = new ConcurrentHashMap<>();
            messageMap = new ConcurrentHashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Public accessor for singleton instance
    public static Dynamo getInstance() {
        return INSTANCE;
    }

    private ConcurrentHashMap<Integer, Frame> messageMap;

    private ConcurrentLinkedQueue<PendingPacket>[] packetQueues;

    private ConcurrentHashMap<Integer, Function<byte[], Void>> subscriptions;

    private final int peerCount = 4;

    public void addUser(ClientNode self, ClientNode mainServer) {
        if (self.equals(mainServer)) {
            // self is the main server
            // add self to the main server
            // add self to the dynamo
        } else {
            // self is a client
            // add self to the client
            // add self to the dynamo
        }
        // start Server on given port to accept connections
        try {
            socketry.startServer(self.port());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeDynamo() {
        // close the dynamo
    }

    public void consumeRPC(AbstractRPC rpc) {
        // consume the rpc
    }

    public void sendData(byte[] data, ClientNode[] destIp, int module, int priority) {
        // send the data to the destination
    }

    public void broadcast(byte[] data, int module, int priority) {
        // broadcast the data to all clients
    }

    public void subscribe(int name, Function<byte[], Void> function) {
        // subscribe to the name
    }

    public void removeSubscription(int name) {
        // remove the subscription
    }

    private void handleChunk(Chunk chunk) {
        Frame frame = null;
        if (chunk.getChunkNumber() == 0) {
            // first chunk
            frame = Frame.deserialize(chunk.getPayload());
            messageMap.put(chunk.getMessageID(), frame);
        } else {
            // subsequent chunk
            frame = messageMap.get(chunk.getMessageID());
            if (frame != null) {
                if (frame.appendPayload(chunk.getPayload())) {
                    messageMap.remove(chunk.getMessageID());
                    handleFrame(frame);
                } else {
                    // continue
                }
            } else {
                System.err.println("Empty payload for message ID: " + chunk.getMessageID());
            }
        }

        if (frame.getForwardingLength() > 0) {
            byte priority = (byte) frame.getPriority();
            for (int i = 0; i < Math.min(frame.getForwardingLength(), peerCount - 1); i++) {
                Node reciever = frame.getForwardingNodes()[i];
                ConcurrentLinkedQueue<PendingPacket> queue = packetQueues[priority];
                queue.add(new PendingPacket(modifyForwarding(chunk, false), reciever));
            }

            Node reciever = frame.getForwardingNodes()[peerCount - 1];
            ConcurrentLinkedQueue<PendingPacket> queue = packetQueues[priority];
            queue.add(
                    new PendingPacket(modifyForwarding(chunk, frame.getForwardingLength() > peerCount), reciever));
        }
    }

    private Chunk modifyForwarding(Chunk chunk, boolean isForwarding) {
        if (chunk.getChunkNumber() == 0) {
            // THIS IS THE PACKET WITH THE FRAME

            Frame frame = Frame.deserialize(chunk.getPayload());

            if (frame.getForwardingLength() > 0) {
                // THIS IS A PACKET WITH FORWARDING NODES
                if (frame.getForwardingLength() < peerCount) {
                    // THIS IS A PACKET WITH FORWARDING NODES BUT LESS THAN THE PEER COUNT

                    Frame newFrame = new Frame(frame.getLength(), frame.getType(), frame.getPriority(), (byte) 0,
                            new Node[0], frame.getPayload());
                    return new Chunk(chunk.getMessageID(), chunk.getChunkNumber(), newFrame.serialize());

                } else if (isForwarding) {
                    // THIS IS A PACKET WITH FORWARDING NODES BUT GREATER THAN THE PEER COUNT

                    Node[] newForwardingNodes = new Node[frame.getForwardingLength() - peerCount];
                    System.arraycopy(frame.getForwardingNodes(), peerCount, newForwardingNodes, 0,
                            frame.getForwardingLength() - peerCount);

                    Frame newFrame = new Frame(frame.getLength(), frame.getType(), frame.getPriority(),
                            (byte) (frame.getForwardingLength() - peerCount), newForwardingNodes, frame.getPayload());

                    return new Chunk(chunk.getMessageID(), chunk.getChunkNumber(), newFrame.serialize());
                } else {
                    // THIS IS A PACKET WITH FORWARDING NODES BUT GREATER THAN THE PEER COUNT BUT
                    // SENT TO NON FORWARDING NODE
                    Frame newFrame = new Frame(frame.getLength(), frame.getType(), frame.getPriority(), (byte) 0,
                            frame.getForwardingNodes(), frame.getPayload());
                    return new Chunk(chunk.getMessageID(), chunk.getChunkNumber(), newFrame.serialize());
                }
            } else {
                return chunk;
            }
        } else {
            return chunk;
        }
    }

    private void handleFrame(Frame frame) {
        Function<byte[], Void> subscription = subscriptions.get((int) frame.getType());

        if (subscription != null) {
            subscription.apply(frame.getPayload());
        } else {
            System.err.println("No subscription found for type: " + frame.getType());
        }
    }

    private void startListening() throws IOException {
        while (true) {
            // since each are configured in non-blocking mode
            // they just returns back almost instantly
            ArrayList<Chunk> unhandledPackets = socketry.listen();
            unhandledPackets.forEach(packet -> {
                handleChunk(packet);
            });
        }
    }

}
