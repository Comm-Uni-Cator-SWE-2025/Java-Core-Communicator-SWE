/**
 * Contributed by @chirag9528
 */

package com.swe.ScreenNVideo.Synchronizer;

import com.swe.ScreenNVideo.Codec.ADPCMDecoder;
import com.swe.ScreenNVideo.Model.APackets;

import com.swe.ScreenNVideo.Playback.AudioPlayer;


/**
 * Synchronizer to synchronize the image from the patches.
 */
public class AudioSynchronizer {

    /**
     * for decoding ADPCM audio sample.
     */
    private final ADPCMDecoder decoder;

    /**
     * for playing audio sample.
     */
    private final AudioPlayer audioPlayer;

    /**
    * for decoding ADPCM audio sample.
    */
    private boolean resetHeap;

    /**
     * Create a new audio synchronizer.
     *
     */
    public AudioSynchronizer(AudioPlayer audioPlayerArg) {
        this.decoder = new ADPCMDecoder();
        this.audioPlayer = audioPlayerArg;
    }

    /**
     * .
     *
     */
    public boolean synchronize(final APackets apacket) {

        // setting the decoder state before decoding
        int predictedPCM = apacket.predictedPCM();
        int indexPCM = apacket.indexPCM();
        decoder.setState(predictedPCM, indexPCM);

        // playing the decoded audio sample
        this.audioPlayer.play(decoder.decode(apacket.data()));

    return true;
    }
}
