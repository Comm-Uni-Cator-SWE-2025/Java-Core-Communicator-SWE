/**
 * Contributed by @chirag9528.
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
     * @param audioPlayerArg the audio player used to output decoded PCM audio
     */
    public AudioSynchronizer(final AudioPlayer audioPlayerArg) {
        this.decoder = new ADPCMDecoder();
        this.audioPlayer = audioPlayerArg;
    }

    /**
     * Synchronize APackets.
     * @param apacket the incoming ADPCM packet containing audio data and decoder state
     * @return true if the packet was processed and played successfully
     */
    public boolean synchronize(final APackets apacket) {

        // setting the decoder state before decoding
        final int predictedPCM = apacket.predictedPCM();
        final int indexPCM = apacket.indexPCM();
        decoder.setState(predictedPCM, indexPCM);

        // playing the decoded audio sample
        this.audioPlayer.play(decoder.decode(apacket.data()));

        return true;
    }
}
