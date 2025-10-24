package com.swe.ScreenNVideo;


import com.swe.RPC.AbstractRPC;
import com.swe.ScreenNVideo.Capture.ICapture;
import com.swe.ScreenNVideo.Capture.ScreenCapture;
import com.swe.ScreenNVideo.Capture.VideoCapture;
import com.swe.ScreenNVideo.Codec.BilinearScaler;
import com.swe.ScreenNVideo.Codec.Codec;
import com.swe.ScreenNVideo.Codec.ImageScaler;
import com.swe.ScreenNVideo.Codec.JpegCodec;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.PatchGenerator.Hasher;
import com.swe.ScreenNVideo.PatchGenerator.IHasher;
import com.swe.ScreenNVideo.PatchGenerator.ImageStitcher;
import com.swe.ScreenNVideo.PatchGenerator.PacketGenerator;
import com.swe.ScreenNVideo.PatchGenerator.Patch;
import com.swe.ScreenNVideo.Serializer.CPackets;
import com.swe.ScreenNVideo.Serializer.NetworkPacketType;
import com.swe.ScreenNVideo.Serializer.NetworkSerializer;
import com.swe.ScreenNVideo.Serializer.RImage;
import com.swe.ScreenNVideo.Synchronizer.ImageSynchronizer;
import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;
import com.swe.networking.SimpleNetworking.AbstractNetworking;
import com.swe.networking.SimpleNetworking.MessageListener;
import com.swe.networking.SimpleNetworking.PacketParser;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
     * Flag for video capture.
     */
    private boolean isVideoCaptureOn;
    /**
     * Flag for screen capture.
     */
    private boolean isScreenCaptureOn;

    /**
     * Video capture object.
     */
    private final ICapture videoCapture;
    /**
     * Screen capture object.
     */
    private final ICapture screenCapture;

    /**
     * Patch generator object.
     */
    private final PacketGenerator patchGenerator;
    /**
     * Image stitcher object.
     */
    private final ImageStitcher imageStitcher;
    /**
     * Image synchronizer object.
     */
    private final HashMap<String, ImageSynchronizer> imageSynchronizers;
    /**
     * Image scaler object.
     */
    private final ImageScaler scalar;
    /**
     * Video codec object.
     */
    private final Codec videoCodec;

    /**
     * Networking object.
     */
    private final AbstractNetworking networking;
    /**
     * RPC object.
     */
    private final AbstractRPC rpc;

    /**
     * List of viewers to send the video Feed.
     */
    private final ArrayList<ClientNode> viewers;

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
        isScreenCaptureOn = false;
        isVideoCaptureOn = false;
        this.networking = argNetworking;
        videoCapture = new VideoCapture();
        screenCapture = new ScreenCapture();
        videoCodec = new JpegCodec();
        final IHasher hasher = new Hasher(Utils.HASH_STRIDE);
        patchGenerator = new PacketGenerator(videoCodec, hasher);
        imageStitcher = new ImageStitcher();
        imageSynchronizers = new HashMap<>();
        viewers = new ArrayList<>();
        scalar = new BilinearScaler();

        System.out.println(getSelfIP());
        addParticipant(getSelfIP());
        addParticipant("10.32.11.242");
        initializeHandlers();
    }

    private void addParticipant(final String ip) {
        final ClientNode node = new ClientNode(ip, port);
        viewers.add(node);
        imageSynchronizers.put(ip, new ImageSynchronizer(videoCodec));
    }

    private int[][] getFeedMatrix(final BufferedImage videoFeed, final BufferedImage screenFeed) {
        int[][] feed = null;
        if (screenFeed != null) {
            feed = Utils.convertToRGBMatrix(screenFeed);
        }

        if (videoFeed != null) {
            final int[][] videoMatrix = Utils.convertToRGBMatrix(videoFeed);
            if (feed == null) {
                feed = videoMatrix;
            } else {
                final int height = feed.length;
                final int width = feed[0].length;
                final int targetHeight = height / Utils.SCALE_Y;
                final int targetWidth = width / Utils.SCALE_X;
                final int[][] scaledDownedFeed = scalar.scale(videoMatrix, targetHeight, targetWidth);
                final int videoPosY = height - Utils.VIDEO_PADDING_Y - targetHeight;
                final int videoPosX = width - Utils.VIDEO_PADDING_X - targetWidth;
                final Patch videoPatch = new Patch(scaledDownedFeed, videoPosX, videoPosY);
                imageStitcher.setCanvas(feed);
                imageStitcher.stitch(videoPatch);
                feed = imageStitcher.getCanvas();
            }
        }
        feed = scalar.scale(feed, Utils.SERVER_HEIGHT, Utils.SERVER_WIDTH);

        return feed;
    }

    private static String getSelfIP() {
        // Get IP address as string
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Server-side of the ScreenNVideo.
     */
    @Override
    public void startCapture() throws ExecutionException, InterruptedException {

        System.out.println("Starting capture");
        BufferedImage videoFeed = null;
        BufferedImage screenFeed = null;
        int[][] feed;

        final int fps = 30;
        final double timeDelay = (1.0 / fps) * 1_000_000_000;
        long start = 0;
        while (true) {
            final long currTime = System.nanoTime();
            final long diff = currTime - start;
            if (diff < timeDelay) {
                continue;
            }

            System.out.println("Server FPS : "
                + (int) ((double) (Utils.SEC_IN_MS) / ((currTime - start)
                / ((double) (Utils.MSEC_IN_NS)))));
            start = System.nanoTime();
            screenFeed = null;
            videoFeed = null;

            if (!isScreenCaptureOn && !isVideoCaptureOn) {
                try {
                    Thread.sleep(Utils.SEC_IN_MS);
                } catch (InterruptedException e) {
                    System.out.println("Error : " + e.getMessage());
                }
                continue;
            }

            if (isVideoCaptureOn) {
                try {
                    videoFeed = videoCapture.capture();
                } catch (AWTException e) {
                    videoFeed = null;
                }
            }
            if (isScreenCaptureOn) {
                try {
                    screenFeed = screenCapture.capture();
                } catch (AWTException e) {
                    screenFeed = null;
                }
            }

            // get the feed to send
            feed = getFeedMatrix(videoFeed, screenFeed);
            if (feed == null) {
                continue;
            }

            videoCodec.setScreenshot(feed);

            final List<CompressedPatch> patches = patchGenerator.generatePackets(feed);

            if (patches.isEmpty()) {
                continue;
            }

            final CPackets networkPackets = new CPackets(getSelfIP(), patches);
            System.out.println("Sending to " + networkPackets.getIp());
            byte[] encodedPatches = null;
            int tries = Utils.MAX_TRIES_TO_SERIALIZE;
            while (tries-- > 0) {
                // max tries 3 times to convert the patch
                try {
                    encodedPatches = networkPackets.serializeCPackets();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (tries < 0 || encodedPatches == null) {
                System.err.println("Error: Unable to serialize compressed packets");
                continue;
            }
//             send to others who have subscribed
            sendImageToViewers(encodedPatches);
            // TODO: send to UI of current machine to display.
            //  Remove selfIP from viewers list and directly send the data from here to UI
        }
    }

    private void sendImageToViewers(final byte[] feed) {
        viewers.forEach(System.out::println);
        networking.sendData(feed, viewers.toArray(new ClientNode[0]), ModuleType.CHAT, 2);
    }


    private void initializeHandlers() {
        rpc.subscribe(Utils.START_VIDEO_CAPTURE, (final byte[] args) -> {
            isVideoCaptureOn = true;
            final byte[] res = new byte[1];
            res[0] = 1;
            return res;
        });

        rpc.subscribe(Utils.STOP_VIDEO_CAPTURE, (final byte[] args) -> {
            isVideoCaptureOn = false;
            final byte[] res = new byte[1];
            res[0] = 1;
            return res;
        });

        rpc.subscribe(Utils.START_SCREEN_CAPTURE, (final byte[] args) -> {
            isScreenCaptureOn = true;
            final byte[] res = new byte[1];
            res[0] = 1;
            return res;
        });

        rpc.subscribe(Utils.STOP_SCREEN_CAPTURE, (final byte[] args) -> {
            isScreenCaptureOn = false;
            final byte[] res = new byte[1];
            res[0] = 1;
            return res;
        });

        rpc.subscribe(Utils.SUBSCRIBE_AS_VIEWER, (final byte[] args) -> {
            // Get the destination user IP
            final String destIP = NetworkSerializer.deserializeIP(args);

            final ClientNode destNode = new ClientNode(destIP, port);

            // Get IP address as string
            String selfIP = getSelfIP();
            final byte[] subscribeData = NetworkSerializer.serializeIP(selfIP);
            networking.sendData(subscribeData, new ClientNode[] {destNode}, ModuleType.SCREENSHARING, 2);

            final byte[] res = new byte[1];
            res[0] = 1;
            return res;
        });

        networking.subscribe(ModuleType.CHAT, new ClientHandler());

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
        public void receiveData(byte[] data) {

            System.out.println("Recieved");
            data = PacketParser.getPacketParser().getPayload(data);
            if (data.length == 0) {
                return;
            }

            final byte packetType = data[0];
            if (packetType > enumVals.length) {
                System.err.println("Error: Invalid packet type: " + packetType + "  " + data.length);
                System.err.println("Error: Packet data: " + (Arrays.toString(Arrays.copyOf(data, 20))));
                return;
            }
            final NetworkPacketType type = enumVals[packetType];
            switch (type) {
                case NetworkPacketType.LIST_CPACKETS -> {
                    final CPackets networkPackets = CPackets.deserialize(data);
                    final List<CompressedPatch> patches = networkPackets.getPackets();
                    final ImageSynchronizer imageSynchronizer = imageSynchronizers.get(networkPackets.getIp());
                    if (imageSynchronizer == null) {
                        return;
                    }
                    final int[][] image = imageSynchronizer.synchronize(patches);
                    final RImage rImage = new RImage(image, networkPackets.getIp());
                    final byte[] serializedImage = rImage.serialize();
                    // Do not wait for result
                    try {
                        rpc.call(Utils.UPDATE_UI, serializedImage).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
                case NetworkPacketType.SUBSCRIBE_AS_VIEWER -> {
                    final String viewerIP = NetworkSerializer.deserializeIP(data);
                    addParticipant(viewerIP);
                }
                default -> {
                }
            }

        }
    }
}
