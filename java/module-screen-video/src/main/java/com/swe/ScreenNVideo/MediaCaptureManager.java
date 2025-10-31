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
import java.util.concurrent.ArrayBlockingQueue;

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
     * CaptureComponent object.
     */
    private final CaptureComponents captureComponents;
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
        this.networking = argNetworking;
        captureComponents = new CaptureComponents(networking);
        videoComponent = new VideoComponents(Utils.FPS);
        imageSynchronizers = new HashMap<>();
        viewers = new ArrayList<>();

        // Cache local IP once to avoid repeated socket operations during capture
        this.localIp = getSelfIP();
        System.out.println(this.localIp);

//        addParticipant(getSelfIP());
        addParticipant("10.32.11.242");
//        addParticipant("10.32.9.37");
    }

    private void addParticipant(final String ip) {
        final ClientNode node = new ClientNode(ip, port);
        viewers.add(node);
        imageSynchronizers.put(ip, new ImageSynchronizer(videoComponent.videoCodec));
    }

    private static String getSelfIP() {
        // Get IP address as string
        try (DatagramSocket socket = new DatagramSocket()) {
            final int pingPort = 10002;
            socket.connect(InetAddress.getByName("8.8.8.8"), pingPort);
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
        //noinspection InfiniteLoopStatement
        while (true) {
            final byte[] encodedPatches = videoComponent.captureScreenNVideo();
            if (encodedPatches == null) {
                continue;
            }
            sendImageToViewers(encodedPatches);
        }
    }

    private void sendImageToViewers(final byte[] feed) {
        System.out.println("Size : " + feed.length / Utils.KB + " KB");
        networking.sendData(feed, viewers.toArray(new ClientNode[0]), ModuleType.CHAT, 2);
    }


    class VideoComponents {
        /**
         * Video codec object.
         */
        private final Codec videoCodec;

        /**
         * Patch generator object.
         */
        private final PacketGenerator patchGenerator;

        /**
         * Keeps track of previous click.
         */
        private long start = 0L;

//        /**
//         * Limit the FPS at server.
//         */
//        private final double timeDelay;

        VideoComponents(final int fps) {
            final IHasher hasher = new Hasher(Utils.HASH_STRIDE);
            videoCodec = new JpegCodec();
            patchGenerator = new PacketGenerator(videoCodec, hasher);
            // initialize bounded queue and start worker thread that reads from the queue and updates the UI
            this.uiQueue = new ArrayBlockingQueue<>(UI_QUEUE_CAPACITY);
            final Thread uiWorkerThread = new Thread(this::uiWorkLoop, "MediaCaptureManager-UI-Worker");
            uiWorkerThread.setDaemon(true);
            uiWorkerThread.start();
//            timeDelay = (1.0 / fps) * Utils.SEC_IN_NS;
        }

        private void uiWorkLoop() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final int[][] frame = uiQueue.take(); // blocks until a frame is available
                    try {
                        final RImage rImage = new RImage(frame, localIp);
                        final byte[] serializedImage = rImage.serialize();
                        // Fire-and-forget; do not block capture thread — worker thread can block on network
                        try {
                            rpc.call(Utils.UPDATE_UI, serializedImage);
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                } catch (final InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        /**
         * Previous time stamp.
         */
        private long prev = 0;

        /**
         * limit the queue length.
         */
        private static final int UI_QUEUE_CAPACITY = 2;
        /**
         * Bounded queue for UI frames.
         */
        private final ArrayBlockingQueue<int[][]> uiQueue;

        // Submit an RImage serialization + RPC task to the background worker queue. If the queue is full,
        // this method will drop the frame (non-blocking) to keep capture smooth.
        private void submitUIUpdate(final int[][] frame) {
            // only add to the queue; worker thread handles serialization and sending
            if (frame == null) {
                return;
            }
            final boolean offered = uiQueue.offer(frame);
            if (!offered) {
                // drop the frame
                System.err.println("UI queue full — dropping frame");
            }
        }

        /**
         * Captures the Video using CaptureComponents, handles overlay ,diffing and converting it to bytes  .
         *
         * @return encoded Patches to be sent through the network
         */
        protected byte[] captureScreenNVideo() {
            final long currTime = System.nanoTime();
            final long diff = currTime - start;
//            if (diff < timeDelay) {
//                return null;
//            }
            System.out.println("Time Delay " + ((currTime - prev)
                / ((double) Utils.MSEC_IN_NS)) + " " + (diff / ((double) Utils.MSEC_IN_NS)));
            System.out.println("\nServer FPS : "
                + (int) ((double) (Utils.SEC_IN_MS) / (diff / ((double) (Utils.MSEC_IN_NS)))));
            start = System.nanoTime();

            final int[][] feed = captureComponents.getFeed();
            if (feed == null) {
                return null;
            }

            System.out.println("Time taken : Video | PacketGen");
            final long curr1 = System.nanoTime();
            System.out.print((curr1 - start) / (double) (Utils.MSEC_IN_NS) + " | ");

            videoCodec.setScreenshot(feed);

            final List<CompressedPatch> patches = patchGenerator.generatePackets(feed);

            if (patches.isEmpty()) {
                prev = System.nanoTime();
                return null;
            }

            final CPackets networkPackets = new CPackets(localIp, patches);
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
                prev = System.nanoTime();
                return null;
            }

            // Asynchronously send a serialized RImage to the UI so we don't block capture
            // (frame is deep-copied inside submitUIUpdate)
            submitUIUpdate(feed);

            prev = System.nanoTime();
            System.out.print((prev - curr1) / (double) (Utils.MSEC_IN_NS));
            System.out.println("\nSending to " + networkPackets.getIp());
            return encodedPatches;
        }
    }

    class CaptureComponents {
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
         * Image scaler object.
         */
        private final ImageScaler scalar;


        /**
         * Image stitcher object.
         */
        private final ImageStitcher imageStitcher;

        /**
         * Networking object.
         */
        private final AbstractNetworking networking;

        CaptureComponents(final AbstractNetworking argNetworking) {
            isScreenCaptureOn = false;
            isVideoCaptureOn = false;
            this.networking = argNetworking;
            videoCapture = new VideoCapture();
            screenCapture = new ScreenCapture();
            scalar = new BilinearScaler();
            imageStitcher = new ImageStitcher();
            initializeHandlers();
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
            if (feed != null) {
                feed = scalar.scale(feed, Utils.SERVER_HEIGHT, Utils.SERVER_WIDTH);
            }
            return feed;
        }

        public int[][] getFeed() {
            BufferedImage videoFeed = null;
            BufferedImage screenFeed = null;
            final int[][] feed;

            if (!isScreenCaptureOn && !isVideoCaptureOn) {
                try {
                    Thread.sleep(Utils.SEC_IN_MS);
                } catch (InterruptedException e) {
                    System.out.println("Error : " + e.getMessage());
                }
            } else if (isVideoCaptureOn) {
                try {
                    videoFeed = videoCapture.capture();
                } catch (AWTException _) {
                }
            } else if (isScreenCaptureOn) {
                try {
                    screenFeed = screenCapture.capture();
                } catch (AWTException _) {
                }
            }

            // get the feed to send
            feed = getFeedMatrix(videoFeed, screenFeed);
            return feed;
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
                final byte[] subscribeData = NetworkSerializer.serializeIP(localIp);
                networking.sendData(subscribeData, new ClientNode[] {destNode}, ModuleType.SCREENSHARING, 2);

                final byte[] res = new byte[1];
                res[0] = 1;
                return res;
            });

            networking.subscribe(ModuleType.CHAT, new ClientHandler());

        }
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
        public void receiveData(final byte[] dataArgs) {

//            System.out.println("Recieved");
            final byte[] data = PacketParser.getPacketParser().getPayload(dataArgs);
            if (data.length == 0) {
                return;
            }

            final byte packetType = data[0];
            if (packetType > enumVals.length) {
                final int printLen = 20;
                System.err.println("Error: Invalid packet type: " + packetType + "  " + data.length);
                System.err.println("Error: Packet data: " + (Arrays.toString(Arrays.copyOf(data, printLen))));
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
