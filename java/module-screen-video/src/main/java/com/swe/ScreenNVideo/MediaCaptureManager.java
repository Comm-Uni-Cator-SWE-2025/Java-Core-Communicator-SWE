package com.swe.ScreenNVideo;


import com.swe.RPC.AbstractRPC;
import com.swe.ScreenNVideo.Capture.BackgroundCaptureManager;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.Serializer.CPackets;
import com.swe.ScreenNVideo.Serializer.NetworkPacketType;
import com.swe.ScreenNVideo.Serializer.NetworkSerializer;
import com.swe.ScreenNVideo.Serializer.RImage;
import com.swe.ScreenNVideo.Synchronizer.FeedData;
import com.swe.ScreenNVideo.Synchronizer.ImageSynchronizer;
import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.AbstractNetworking;
import com.swe.networking.SimpleNetworking.MessageListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Media Manager for Screen N Video.
 * - Manages Screen Capture and Video Capture
 */
public class MediaCaptureManager implements CaptureManager {
    /**
     * Port for the server.
     */
    private final int port;

    /**
     * VideoComponent object : manages the video overlay and diffing.
     */
    private final VideoComponents videoComponent;
    /**
     * Image synchronizer object.
     */
    private final HashMap<String, ImageSynchronizer> imageSynchronizers;

    /**
     * Networking object.
     */
    private final AbstractNetworking networking;
    /**
     * RPC object.
     */
    private final AbstractRPC rpc;

    /**
     * Cached IP for this machine to avoid repeated socket calls.
     */
    private final String localIp;

    /**
     * List of viewers to send the video Feed.
     */
    private final HashSet<ClientNode> viewers;

    /**
     * Client handler for incoming messages.
     */
    private final ClientHandler clientHandler;

    /**
     * Constructor for the MediaCaptureManager.
     *
     * @param argNetworking Networking object
     * @param argRpc        RPC object
     * @param portArgs      Port for the server
     */
    public MediaCaptureManager(final AbstractNetworking argNetworking, final AbstractRPC argRpc, final int portArgs) {
        this.rpc = argRpc;
        this.port = portArgs;
        this.networking = argNetworking;
        final CaptureComponents captureComponents = new CaptureComponents(networking, rpc, port);
        videoComponent = new VideoComponents(Utils.FPS, rpc, captureComponents);
        final BackgroundCaptureManager backgroundCaptureManager = new BackgroundCaptureManager(captureComponents);
        backgroundCaptureManager.start();

        imageSynchronizers = new HashMap<>();
        viewers = new HashSet<>();

        // Cache local IP once to avoid repeated socket operations during capture
        this.localIp = Utils.getSelfIP();
        System.out.println(this.localIp);

        addParticipant(localIp);

        clientHandler = new MediaCaptureManager.ClientHandler();

        networking.subscribe(ModuleType.SCREENSHARING, clientHandler);
    }

    /**
     * Broadcast join meeting to available IPs.
     * Only till broadcast is supported, multicast not supported yet.
     *
     * @param availableIPs list of available IPs
     */
    public void broadcastJoinMeeting(final List<String> availableIPs) {
        final ClientNode[] clientNodes = availableIPs.stream().map(ip -> new ClientNode(ip, port)).toArray(ClientNode[]::new);

        System.out.println("Broadcasting join meeting to : " + Arrays.toString(clientNodes));
        final byte[] subscribeData = NetworkSerializer.serializeIP(NetworkPacketType.SUBSCRIBE_AS_VIEWER, localIp);
        networking.sendData(subscribeData, clientNodes, ModuleType.SCREENSHARING, 2);
    }

    @Override
    public void newParticipantJoined(final String ip) {
        clientHandler.addUserNFullImageRequest(ip);
    }

    private void addParticipant(final String ip) {
        if (ip == null) {
            return;
        }
//        if (localIp != null && ip == localIp) {
//            return;
//        }
        final ClientNode node = new ClientNode(ip, port);
        viewers.add(node);
        imageSynchronizers.put(ip, new ImageSynchronizer(videoComponent.getVideoCodec()));
        rpc.call(Utils.SUBSCRIBE_AS_VIEWER, ip.getBytes());
    }

    /**
     * Server-side of the ScreenNVideo.
     */
    @Override
    public void startCapture() throws ExecutionException, InterruptedException {

        System.out.println("Starting capture");
        int[][] feed = null;
        while (true) {
            final byte[] encodedPatches = videoComponent.captureScreenNVideo();
            final int[][] newFeed = videoComponent.getFeed();
            if (encodedPatches == null) {
                if (feed != null && newFeed == null) {
                    final byte[] subscribeData = NetworkSerializer.serializeIP(NetworkPacketType.STOP_SHARE, localIp);
                    sendDataToViewers(subscribeData);
                    feed = newFeed;
                }
                continue;
            }
            feed = newFeed;
            sendDataToViewers(encodedPatches);
        }
    }

    private void sendDataToViewers(final byte[] feed) {

//        System.out.println("Size : " + feed.length / Utils.KB + " KB");
        CompletableFuture.runAsync(() -> {
            networking.sendData(feed, viewers.toArray(new ClientNode[0]), ModuleType.SCREENSHARING, 2);
    //        SimpleNetworking.getSimpleNetwork().closeNetworking();
//            System.out.println("Sent to viewers " + viewers.size());
//            viewers.forEach(v -> System.out.println("Viewer IP : " + v.hostName()));
    //        try {
    //            Thread.sleep(30000);
    //        } catch (InterruptedException e) {
    //            throw new RuntimeException(e);
    //        }
        });
    }


    class ClientHandler implements MessageListener {
        /**
         * Cache for NetworkType enum.
         */
        private final NetworkPacketType[] enumVals;

        ClientHandler() {
            enumVals = NetworkPacketType.values();
        }

        @Override
        public void receiveData(final byte[] data) {

//            System.out.println("Recieved");
            if (data.length == 0) {
                return;
            }

            final byte packetType = data[0];
            if (packetType > enumVals.length) {
                final int printLen = 34;
                System.err.println("Error: Invalid packet type: " + packetType + "  " + data.length);
                System.err.println("Error: Packet data: " + (Arrays.toString(Arrays.copyOf(data, printLen))));
                return;
            }
            final NetworkPacketType type = enumVals[packetType];
            switch (type) {
                case NetworkPacketType.LIST_CPACKETS -> {
//                    System.out.println(Arrays.toString(Arrays.copyOf(data, 10)));
                    final CPackets networkPackets = CPackets.deserialize(data);
                    System.err.println("Received CPackets : " + data.length / Utils.KB + " KB " + networkPackets.packetNumber());
//                    System.out.println("Height: " + networkPackets.height() + " Width: " + networkPackets.width());

                    ImageSynchronizer imageSynchronizer = imageSynchronizers.get(networkPackets.ip());
                    if (imageSynchronizer == null) {
                        // add new participant if not already present
                        addParticipant(networkPackets.ip());
                        imageSynchronizer = imageSynchronizers.get(networkPackets.ip());
                    }

                    final List<CompressedPatch> patches;
                    final int newHeight;
                    final int newWidth;

                    if (networkPackets.isFullImage()) {
                        // reset expected feed number
                        imageSynchronizer.setExpectedFeedNumber(networkPackets.packetNumber());

                        // drop all entries older than this full image
                        while (!imageSynchronizer.getHeap().isEmpty() && imageSynchronizer.getHeap().peek().getFeedNumber() < imageSynchronizer.getExpectedFeedNumber()) {
                            imageSynchronizer.getHeap().poll();
                        }

                        patches = networkPackets.packets();
                        newHeight = networkPackets.height();
                        newWidth = networkPackets.width();
                    } else {

                        // if heap is growing too large, request a full frame to resync
                        if (imageSynchronizer.getHeap().size() >= Utils.MAX_HEAP_SIZE) {
                            System.err.println("Asking for data...");
                            final byte[] subscribeData = NetworkSerializer.serializeIP(NetworkPacketType.SUBSCRIBE_AS_VIEWER, localIp);
                            final ClientNode destNode = new ClientNode(networkPackets.ip(), port);
                            networking.sendData(subscribeData, new ClientNode[]{destNode}, ModuleType.SCREENSHARING, 2);

                            imageSynchronizer.getHeap().clear();
                            return;
                        }

                        imageSynchronizer.getHeap().add(new FeedData(networkPackets.packetNumber(), networkPackets));

                        // If the next expected patch hasn't arrived yet, wait
                        final FeedData feedData = imageSynchronizer.getHeap().peek();
                        if (feedData == null || !(feedData.getFeedNumber() == imageSynchronizer.getExpectedFeedNumber())) {
                            return;
                        }

                        final FeedData minFeedNumPacket = imageSynchronizer.getHeap().poll();

                        if (minFeedNumPacket == null) {
                            return;
                        }

                        final CPackets minFeedCPacket = minFeedNumPacket.getFeedPackets();
                        patches = minFeedCPacket.packets();
                        newHeight = minFeedCPacket.height();
                        newWidth = minFeedCPacket.width();
                    }

                    imageSynchronizer.setExpectedFeedNumber(imageSynchronizer.getExpectedFeedNumber() + 1);

                    final int[][] image = imageSynchronizer.synchronize(newHeight, newWidth, patches,
                        networkPackets.compress());
                    final RImage rImage = new RImage(image, networkPackets.ip());
                    final byte[] serializedImage = rImage.serialize();
                    // Do not wait for result
                    try {
                        final byte[] res = rpc.call(Utils.UPDATE_UI, serializedImage).get();
                        final boolean success = res[0] == 1;
                        if (!success) {
                            addParticipant(networkPackets.ip());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                }
                case NetworkPacketType.SUBSCRIBE_AS_VIEWER -> {
                    final String viewerIP = NetworkSerializer.deserializeIP(data);
                    System.out.println("Viewer joined" + viewerIP);
                    addUserNFullImageRequest(viewerIP);
                    System.out.println("Handled packet type: " + type);
                }
                case STOP_SHARE -> {
                    final String viewerIP = NetworkSerializer.deserializeIP(data);
                    rpc.call(Utils.STOP_SHARE, viewerIP.getBytes());
                }
                default -> {
                }
            }
        }

        public void addUserNFullImageRequest(final String ip) {
            addParticipant(ip);
            final byte[] fullImageEncoded = videoComponent.captureFullImage();
            if (fullImageEncoded == null) {
                return;
            }
            networking.sendData(fullImageEncoded, new ClientNode[]{new ClientNode(ip, port)}, ModuleType.SCREENSHARING, 2);
        }
    }
}
