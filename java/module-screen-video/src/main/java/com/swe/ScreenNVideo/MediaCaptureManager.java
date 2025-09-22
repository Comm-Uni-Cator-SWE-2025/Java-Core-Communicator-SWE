package com.swe.ScreenNVideo;


import com.swe.Networking.AbstractNetworking;
import com.swe.Networking.MessageListener;
import com.swe.RPC.AbstractRPC;
import com.swe.RPC.RProcedure;
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
import java.util.concurrent.ExecutionException;

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

    private final PacketGenerator patchGenerator;
    private final ImageStitcher imageStitcher;
    private final ImageSynchronizer imageSynchronizer;
    private final ImageScaler scalar;
    final Codec videoCodec;

    private final AbstractNetworking networking;
    private final AbstractRPC rpc;

    private final ArrayList<String> viewers;

    public MediaCaptureManager(final AbstractNetworking argNetworking, final AbstractRPC argRpc, final int portArgs) {
        this.rpc = argRpc;
        this.port = portArgs;
        isScreenCaptureOn = true;
        isVideoCaptureOn = false;
        this.networking = argNetworking;
        videoCapture = new VideoCapture();
        screenCapture = new ScreenCapture();
        videoCodec = new JpegCodec();
        final IHasher hasher = new Hasher(Utils.HASH_STRIDE);
        patchGenerator = new PacketGenerator(videoCodec, hasher);
        imageStitcher = new ImageStitcher();
        imageSynchronizer = new ImageSynchronizer(videoCodec, hasher, imageStitcher);
        viewers = new ArrayList<>();
        scalar = new BilinearScaler();
        viewers.add(networking.getSelfIP());
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
//                System.out.println("Here");
                feed = videoMatrix;
            } else {
                final int height = feed.length;
                final int width = feed[0].length;
                final int targetHeight =  height / Utils.SCALE_Y;
                final int targetWidth = width / Utils.SCALE_X;
                final int[][] scaledDownedFeed = scalar.Scale(videoMatrix, targetHeight, targetWidth);
                final int videoPosY = height - Utils.VIDEO_PADDING_Y - targetHeight;
                final int videoPosX = width - Utils.VIDEO_PADDING_X - targetWidth;
                final Patch videoPatch = new Patch(scaledDownedFeed, videoPosX, videoPosY);
//                System.out.println(videoPosX + " " + videoPosY + " " + targetWidth + " " + targetHeight);
                imageStitcher.setCanvas(feed);
                imageStitcher.stitch(videoPatch);
                feed = imageStitcher.getCanvas();
            }
        }

        return feed;
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
            long currTime = System.nanoTime();
            long diff = currTime - start;
            if (diff < timeDelay) {
                continue;
            }

            System.out.println("Server FPS : "  + (int)(1000.0 / ((currTime - start) / 1_000_000.0)) );
            start = System.nanoTime();
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

            byte[] encodedPatches = null;
            int tries = 3;
            while (tries-- > 0) {
                // max tries 3 times to convert the patch
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

//             send to others who have subscribed
            sendImageToViewers(encodedPatches);
            // TODO: send to UI of current machine to display. Remove selfIP from viewers list and directly send the data from here to UI
        }
    }

    private void sendImageToViewers(final byte[] feed) {
        final int[] ports = new int[viewers.size()];
        for (int i = 0; i < viewers.size(); i++) {
            ports[i] = port;
        }
        networking.SendData(feed, viewers.toArray(new String[0]), ports);
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
                final byte[] subscribeData = NetworkSerializer.serializeString(selfIP);
                networking.SendData(subscribeData, new String[] {destIP}, new int[] {port});

                final byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });

        networking.Subscribe(Utils.MODULE_REMOTE_KEY, new ClientHandler());

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
                    // Do not wait for result
                    try {
                        rpc.Call(Utils.UPDATE_UI, serializedImage).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
                case NetworkPacketType.SUBSCRIBE_AS_VIEWER -> {
                    final String viewerIP = NetworkSerializer.deserializeString(data);
                    viewers.add(viewerIP);
                }
                default -> {
                }
            }

        }
    }
}
