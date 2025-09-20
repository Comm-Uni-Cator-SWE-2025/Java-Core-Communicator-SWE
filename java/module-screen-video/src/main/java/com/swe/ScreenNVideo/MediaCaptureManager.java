package com.swe.ScreenNVideo;


import com.swe.Networking.AbstractNetworking;
import com.swe.Networking.MessageListener;
import com.swe.RPC.AbstractRPC;
import com.swe.RPC.RProcedure;
import com.swe.ScreenNVideo.Capture.ICapture;
import com.swe.ScreenNVideo.Capture.ScreenCapture;
import com.swe.ScreenNVideo.Capture.VideoCapture;
import com.swe.ScreenNVideo.Codec.Codec;
import com.swe.ScreenNVideo.Codec.JpegCodec;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.PatchGenerator.Hasher;
import com.swe.ScreenNVideo.PatchGenerator.IHasher;
import com.swe.ScreenNVideo.PatchGenerator.ImageStitcher;
import com.swe.ScreenNVideo.PatchGenerator.PacketGenerator;
import com.swe.ScreenNVideo.Serializer.NetworkPacketType;
import com.swe.ScreenNVideo.Serializer.NetworkSerializer;
import com.swe.ScreenNVideo.Serializer.Serializer;
import com.swe.ScreenNVideo.Synchronizer.ImageSynchronizer;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Media Manager for Screen N Video.
 * - Manages Screen Capture and Video Capture
 */
public class MediaCaptureManager implements CaptureManager {
    private final int port;

    private boolean isVideoCaptureOn;
    private boolean isScreenCaptureOn;

    private final ICapture videoCapture;
    private final ICapture screenCapture;

    private final Codec videoCodec;
    private final PacketGenerator patchGenerator;
    private final IHasher hasher;
    private ImageStitcher imageStitcher;
    private ImageSynchronizer imageSynchronizer;

    private final AbstractNetworking networking;
    private final AbstractRPC rpc;

    private final ArrayList<String> viewers;

    public MediaCaptureManager(final AbstractNetworking argNetworking, final AbstractRPC argRpc, int port) {
        this.rpc = argRpc;
        this.port = port;
        this.networking = argNetworking;
        videoCapture = new VideoCapture();
        screenCapture = new ScreenCapture();
        videoCodec = new JpegCodec();
        hasher = new Hasher(Utils.HASH_STRIDE);
        patchGenerator = new PacketGenerator(videoCodec, hasher);
        imageSynchronizer = new ImageSynchronizer();
        viewers = new ArrayList<>();
        initializeHandlers();
    }

    private int[][] getFeedMatrix(final BufferedImage videoFeed, final BufferedImage screenFeed) {
        if (videoFeed == null && screenFeed == null) {
            return null;
        }
        // TODO: stitch VideoFeed on ScreenFeed if both are on
        BufferedImage feed = screenFeed;

        if (feed == null) {
            feed = videoFeed;
        }
        final int[][] matrix = new int[feed.getHeight()][feed.getWidth()];
        for (int i = 0; i < feed.getHeight(); i++) {
            for (int j = 0; j < feed.getWidth(); j++) {
                matrix[i][j] = feed.getRGB(j, i);
            }
        }
        return matrix;
    }

    /**
     * Server-side of the ScreenNVideo.
     */
    @Override
    public void startCapture() {
        BufferedImage videoFeed = null;
        BufferedImage screenFeed = null;
        int[][] feed;

        while (true) {
            if (!isScreenCaptureOn && !isVideoCaptureOn) {
                try {
                    Thread.sleep(1000);
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

            feed = getFeedMatrix(videoFeed, screenFeed);
            if (feed == null) {
                continue;
            }

            final List<CompressedPatch> patches = patchGenerator.generatePackets(feed);

            if (patches.isEmpty()) {
                continue;
            }

            // TODO : send packets to the client
            byte[] encodedPatches = null;
            int tries = 3;
            while (tries-- > 0) {
                // maxtries 3 times to convert the patch
                try {
                    encodedPatches = NetworkSerializer.serializeCPackets(patches);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (tries < 0 || encodedPatches == null) {
                System.err.println("Error: Unable to serialize compressed packets");
                continue;
            }



        }
    }

    private void sendImageToViewers(int[][] feed) {
        final byte[] serializedImage = Serializer.serializeImage(feed);
        int[] ports = new int[viewers.size()];
        for (int i = 0; i < viewers.size(); i++) {
            ports[i] = port;
        }
        networking.SendData(serializedImage, viewers.toArray(new String[viewers.size()]), ports);
    }


    private void initializeHandlers() {
        rpc.subscribe(Utils.START_VIDEO_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(final byte[] args) {
                isVideoCaptureOn = true;
                final byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });

        rpc.subscribe(Utils.STOP_VIDEO_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(final byte[] args) {
                isVideoCaptureOn = false;
                final byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });

        rpc.subscribe(Utils.START_SCREEN_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(final byte[] args) {
                isScreenCaptureOn = true;
                final byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });

        rpc.subscribe(Utils.STOP_SCREEN_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(final byte[] args) {
                isScreenCaptureOn = false;
                final byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });
        
        rpc.subscribe(Utils.SUBSCRIBE_AS_VIEWER, new RProcedure() {
            @Override
            public byte[] call(final byte[] args) {
                // Get the destination user IP
                final String destIP = NetworkSerializer.deserializeString(args);
                
                final String selfIP = networking.getSelfIP();
                byte[] subscribeData = NetworkSerializer.serializeString(selfIP);
                networking.SendData(subscribeData, new String[] {destIP}, new int[] {port});

                final byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });

        networking.Subscribe(Utils.MODULE_REMOTE_KEY, new ClientHandler());

    }

    class ClientHandler implements MessageListener {
        private final NetworkPacketType[] enumVals;

        ClientHandler() {
            enumVals = NetworkPacketType.values();
        }

        @Override
        public void ReceiveData(final byte[] data) {
            if (data.length == 0) {
                return;
            }

            final byte packetType = data[0];
            if (packetType > enumVals.length) {
                System.err.println("Error: Invalid packet type: " + packetType);
                System.err.println("Error: Packet data: " + Arrays.toString(data));
                return;
            }
            final NetworkPacketType type = enumVals[packetType];
            switch (type) {
                case NetworkPacketType.LIST_CPACKETS -> {
                    final List<CompressedPatch> patches = NetworkSerializer.deserializeCPackets(data);
                    final int[][] image = imageSynchronizer.synchronize(patches);
                    final byte[] serializedImage = Serializer.serializeImage(image);
                    rpc.Call(Utils.UPDATE_UI, serializedImage);
                }
                case NetworkPacketType.SUBSCRIBE_AS_VIEWER -> {
                    final String viewerIP = NetworkSerializer.deserializeString(data);
                    viewers.add(viewerIP);
                }
                default -> {
                    return;
                }
            }

        }
    }
}
