package com.Comm_Uni_Cator.ScreenNVideo;


import com.Comm_Uni_Cator.Networking.AbstractNetworking;
import com.Comm_Uni_Cator.Networking.MessageListener;
import com.Comm_Uni_Cator.RPC.AbstractRPC;
import com.Comm_Uni_Cator.RPC.RProcedure;
import com.Comm_Uni_Cator.ScreenNVideo.Capture.ICapture;
import com.Comm_Uni_Cator.ScreenNVideo.Capture.ScreenCapture;
import com.Comm_Uni_Cator.ScreenNVideo.Capture.VideoCapture;
import com.Comm_Uni_Cator.ScreenNVideo.Codec.Codec;
import com.Comm_Uni_Cator.ScreenNVideo.Codec.JpegCodec;
import com.Comm_Uni_Cator.ScreenNVideo.PatchGenerator.Hasher;
import com.Comm_Uni_Cator.ScreenNVideo.PatchGenerator.IHasher;
import com.Comm_Uni_Cator.ScreenNVideo.PatchGenerator.PacketGenerator;

import java.util.Arrays;

public class MediaCaptureManager implements CaptureManager {

    private ICapture videoCapture;
    private ICapture screenCapture;

    private Codec videoCodec;
    private PacketGenerator patchGenerator;
    private IHasher hasher;

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

    @Override
    public void startCapture() {

    }

    private void initializeHandlers() {
        rpc.subscribe(ConstantProvider.START_VIDEO_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                if (videoCapture == null || !videoCapture.startCapture()) {
                    return new byte[1];
                }
                return new byte[0];
            }
        });

        rpc.subscribe(ConstantProvider.STOP_VIDEO_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                videoCapture.stopCapture();
                return new byte[0];
            }
        });

        rpc.subscribe(ConstantProvider.START_SCREEN_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                if (screenCapture == null || !screenCapture.startCapture()) {
                    return new byte[1];
                }
                return new byte[0];
            }
        });

        rpc.subscribe(ConstantProvider.STOP_SCREEN_CAPTURE, new RProcedure() {
            @Override
            public byte[] call(byte[] args) {
                screenCapture.stopCapture();
                return new byte[0];
            }
        });

        networking.Subscribe(ConstantProvider.MODULE_REMOTE_KEY, new ClientHandler());

    }

    class ClientHandler implements MessageListener {
        @Override
        public void ReceiveData(byte[] data) {
            System.out.println("Data recieved: " + Arrays.toString(data));

        }
    }
}
