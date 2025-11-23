package com.swe.dynamo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;
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

    // Private constructor to prevent instantiation
    private Dynamo() {
        packetQueues = new ConcurrentLinkedQueue[5];
        for (int i = 0; i < 5; i++) {
            packetQueues[i] = new ConcurrentLinkedQueue<>();
        }
        subscriptions = new ConcurrentHashMap<>();
        subscriptions.put(0, (byte[] data) -> {
            if (instantiatedPeerList) {
                System.err.println("Peer list already instantiated, recieved peer list again");
                return null;
            }
            System.out.println("Recieved peer list");

            HashSet<Node> peerList = new HashSet<>();
            Node[] nodes = new Node[data.length / 6];

            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = Node.deserialize(Arrays.copyOfRange(data, i * 6, i * 6 + 6));
                peerList.add(nodes[i]);
            }

            for (Node node : peerList) {
                try {
                    if (node.equals(self) || node.equals(mainServerNode)) {
                        continue;
                    }
                    System.out.println("Connecting to node " + node);
                    coil.connectToNode(node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            instantiatedPeerList = true;
            System.out.println("Peer list instantiated");

            return null;
        });
        incomingMessageMap = new ConcurrentHashMap<>();
        outgoingMessageMap = new ConcurrentHashMap<>();
    }

    // Public accessor for singleton instance
    public static Dynamo getInstance() {
        return INSTANCE;
    }

    private Coil coil;

    private ConcurrentHashMap<Integer, Frame> incomingMessageMap;
    private ConcurrentHashMap<Integer, Frame> outgoingMessageMap;

    private ConcurrentLinkedQueue<PendingPacket>[] packetQueues;

    private volatile boolean instantiatedPeerList = false;

    private ConcurrentHashMap<Integer, Function<byte[], Void>> subscriptions;

    private final int peerCount = 4;

    private Thread coilThread;
    private Thread priorityThread;

    private Node self;
    private Node mainServerNode;

    private Runnable disconnectHandler;

    public void registerDisconnectHandler(Runnable handler) {
        disconnectHandler = handler;
    }

    public void addUser(ClientNode self, ClientNode mainServer) throws Exception {
        // start Server on given port to accept connections
        this.self = new Node(self.hostName(), (short) self.port());
        mainServerNode = new Node(mainServer.hostName(), (short) mainServer.port());
        if (self.equals(mainServer)) {
            instantiatedPeerList = true;
        }
        coil = new Coil(self.equals(mainServer), (node) -> {
            sendNodeList(node);
            return null;
        });
        if (!self.equals(mainServer)) {
            coil.connectToNode(mainServerNode);
        }
        try {
            coil.startServer(self.port());

            coilThread = new Thread(() -> coil.listenLoop((chunk) -> {
                handleChunk(chunk);
                return null;
            }));

            priorityThread = new Thread(this::startPriorityThread);
            priorityThread.setDaemon(true);
            coilThread.setDaemon(true);

            priorityThread.start();
            coilThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPriorityThread() {
        int[] packetCounts = { 1, 2, 2, 3, 3 };
        int failures = 0;
        Node previousFailedReciever = null;
        while (true) {
            // System.out.println("Priority thread running...");
            if (!instantiatedPeerList) {
                continue;
            }
            // System.out.println("Sending data from priority thread...");
            for (int i = 4; i >= 0; i--) {
                ConcurrentLinkedQueue<PendingPacket> queue = packetQueues[i];
                int count = packetCounts[i];
                while (!queue.isEmpty() && count-- > 0) {
                    PendingPacket packet = queue.poll();
                    Chunk chunk = packet.chunk();
                    Frame frame = outgoingMessageMap.get(chunk.getMessageID());
                    if (frame == null) {
                        System.err.println("Frame not found for message ID: " + chunk.getMessageID());
                        continue;
                    }
                    try {
                        System.out.println("Sending data to " + packet.reciever());
                        coil.sendData(packet.reciever(), packet.chunk());

                        if (frame.getLength() / 1024 == chunk.getChunkNumber()) {
                            outgoingMessageMap.remove(chunk.getMessageID());
                        }
                        System.out.println("Sent data to " + packet.reciever());

                        failures = 0;
                        previousFailedReciever = null;
                    } catch (IOException e) {
                        queue.add(packet);
                        // TODO handle retry
                        // get frame of this chunk. send invalid packet
                        if (frame != null) {
                            if (frame.getForwardingLength() > 0) {
                                // send invalid packet to the forwarding nodes
                                // send the whole frame again from the first chunk
                                // TODO: implement this
                                System.err.println("Sending invalid packet to forwarding nodes");
                            }
                        }

                        // TODO decide what to do when a single node fails
                        if (previousFailedReciever != null && previousFailedReciever.equals(packet.reciever())) {
                            continue;
                        }
                        failures++;
                        previousFailedReciever = packet.reciever();
                        System.err.println("Error sending data to node " + packet.reciever() + ": " + e.getMessage());
                        e.printStackTrace();
                        if (failures > 5) {
                            instantiatedPeerList = false;
                            System.err.println("We seem disconnected from the network.");
                            disconnectHandler.run();
                            // try {
                            // coil.sendData(mainServerNode, new Chunk(0, 0, new byte[0]));
                            // } catch (IOException e2) {
                            // e2.printStackTrace();
                            // }
                            break;
                        }
                    }
                }
            }
        }
    }

    public void closeDynamo() {
        // close the dynamo
        coilThread.interrupt();
        priorityThread.interrupt();
    }

    public void consumeRPC(AbstractRPC rpc) {
        // consume the rpc
    }

    private void shuffleNodes(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            int j = (int) (Math.random() * nodes.length);
            Node temp = nodes[i];
            nodes[i] = nodes[j];
            nodes[j] = temp;
        }
    }

    private void sendData(byte[] data, Node[] destNodes, int module, int priority) {

        shuffleNodes(destNodes);

        System.out.println("Sending data to " + destNodes);

        Node[] forwardingNodes = new Node[destNodes.length - 1];
        System.arraycopy(destNodes, 1, forwardingNodes, 0, destNodes.length - 1);
        Frame frame = new Frame(data.length, (byte) module, (byte) priority, (byte) forwardingNodes.length,
                forwardingNodes, data);
        int messageId = UUID.randomUUID().hashCode();

        outgoingMessageMap.put(messageId, frame);

        Chunk[] chunks = new Chunk[frame.getLength() / 1024 + 1];
        byte[] payload = frame.serialize();
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = new Chunk(messageId, i,
                    Arrays.copyOfRange(payload, i * 1024, Math.min((i + 1) * 1024, payload.length)));
        }
        for (Chunk chunk : chunks) {
            packetQueues[priority].add(new PendingPacket(chunk, destNodes[0]));
        }
    }

    public void broadcast(byte[] data, int module, int priority) {
        Node[] nodes = coil.getNodeList();
        ClientNode[] clientNodes = new ClientNode[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            clientNodes[i] = new ClientNode(nodes[i].IPToString(), (short) nodes[i].getPort());
        }
        sendData(data, clientNodes, module, priority);
    }

    public void subscribe(int name, Function<byte[], Void> function) {
        subscriptions.put(name, function);
    }

    public void removeSubscription(int name) {
        subscriptions.remove(name);
    }

    private void handleChunk(Chunk chunk) {
        System.out.println("Recieved chunk: " + chunk.getChunkNumber() + " " + chunk.getMessageID());
        Frame frame = null;
        if (chunk.getChunkNumber() == 0) {
            // first chunk
            frame = Frame.deserialize(chunk.getPayload());
            if (frame.getLength() < 1024) {
                handleFrame(frame);
            } else {
                incomingMessageMap.put(chunk.getMessageID(), frame);
            }
        } else {
            // subsequent chunk
            frame = incomingMessageMap.get(chunk.getMessageID());
            if (frame != null) {
                if (frame.appendPayload(chunk.getPayload())) {
                    incomingMessageMap.remove(chunk.getMessageID());
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

            Node reciever = frame.getForwardingNodes()[Math.min(peerCount - 1, frame.getForwardingLength() - 1)];
            ConcurrentLinkedQueue<PendingPacket> queue = packetQueues[priority];
            queue.add(
                    new PendingPacket(modifyForwarding(chunk, frame.getForwardingLength() > peerCount), reciever));
        }
    }

    private Chunk modifyForwarding(Chunk chunk, boolean isForwarding) {
        if (chunk.getChunkNumber() == 0) {
            // THIS IS THE PACKET WITH THE FRAME

            Frame frame = Frame.deserialize(chunk.getPayload());

            outgoingMessageMap.put(chunk.getMessageID(), frame);

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

    public void sendData(byte[] data, ClientNode[] destIp, int module, int priority) {
        Node[] destNodes = new Node[destIp.length];
        for (int i = 0; i < destIp.length; i++) {
            destNodes[i] = new Node(destIp[i].hostName(), (short) destIp[i].port());
        }
        sendData(data, destNodes, module, priority);
    }

    /**
     * Called only on main server.
     * 
     * @param client
     */
    private void sendNodeList(Node client) {
        System.out.println("Sending node list to " + client);
        Node[] nodeList = coil.getNodeList();
        byte[] nodeListData = new byte[nodeList.length * 6];
        for (int i = 0; i < nodeList.length; i++) {
            System.arraycopy(nodeList[i].serialize(), 0, nodeListData, i * 6, 6);
        }
        sendData(nodeListData, new Node[] { client }, 0, 0);
        System.out.println("Sent node list to " + client);
    }

    private void handleFrame(Frame frame) {
        Function<byte[], Void> subscription = subscriptions.get((int) frame.getType());

        if (subscription != null) {
            subscription.apply(frame.getPayload());
        } else {
            System.err.println("No subscription found for type: " + frame.getType());
        }
    }

}
