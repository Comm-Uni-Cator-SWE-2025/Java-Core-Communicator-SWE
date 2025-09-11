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

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class MediaCaptureManager implements CaptureManager {

    private boolean isVideoCaptureOn;
    private boolean isScreenCaptureOn;

    private ICapture videoCapture;
    private ICapture screenCapture;

    private Codec videoCodec;
    private PacketGenerator patchGenerator;
    private IHasher hasher;
    private ImageStitcher imageStitcher;

    private AbstractNetworking networking;
    private AbstractRPC rpc;

    public MediaCaptureManager(AbstractNetworking _networking, AbstractRPC _rpc) {
        this.rpc = _rpc;
        this.networking = _networking;
        videoCapture = new VideoCapture();
        screenCapture = new ScreenCapture();
        videoCodec = new JpegCodec();
        hasher = new Hasher(ConstantProvider.HASH_STRIDE);
        patchGenerator = new PacketGenerator(videoCodec, hasher);
        initializeHandlers();
    }

    private int[][][] getFeedMatrix(BufferedImage videoFeed, BufferedImage screenFeed) {
        if (videoFeed == null && screenFeed == null) {
            return null;
        }
        // TODO: stitch VideoFeed on ScreenFeed if both are on
        BufferedImage feed = screenFeed;
        if (feed == null) {
            feed = videoFeed;
        }
        int[][][] matrix = new int[feed.getHeight()][feed.getWidth()][3];
        for (int i = 0; i < feed.getHeight(); i++) {
            for (int j = 0; j < feed.getWidth(); j++) {
                int r = feed.getRGB(j, i);
                int g = (r >> 8) & 0xFF;
                int b = (r >> 16) & 0xFF;
                matrix[i][j][0] = r;
                matrix[i][j][1] = g;
                matrix[i][j][2] = b;
            }
        }
        return matrix;
    }

    /**
     * Server-side of the ScreenNVideo
     */
    @Override
    public void startCapture() {
        BufferedImage videoFeed = null;
        BufferedImage screenFeed = null;
        int[][][] feed;

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

            List<CompressedPatch> res = patchGenerator.generatePackets(feed);

            // TODO : send packets to the client

        }
    }

    private void initializeHandlers() {
        rpc.subscribe(ConstantProvider.START_VIDEO_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                isVideoCaptureOn = true;
                byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });

        rpc.subscribe(ConstantProvider.STOP_VIDEO_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                isVideoCaptureOn = false;
                byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });

        rpc.subscribe(ConstantProvider.START_SCREEN_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                isScreenCaptureOn = true;
                byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });

        rpc.subscribe(ConstantProvider.STOP_SCREEN_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                isScreenCaptureOn = false;
                byte[] res = new byte[1];
                res[0] = 1;
                return res;
            }
        });

        networking.Subscribe(ConstantProvider.MODULE_REMOTE_KEY, new ClientHandler());

    }

    class ClientHandler implements MessageListener {
        @Override
        public void ReceiveData(byte[] data) {
            System.out.println("Data recieved: " + Arrays.toString(data));
            // TODO: parse the data

            // TODO: get the Image

            // TODO: send the image to the frontend

        }
    }
}
