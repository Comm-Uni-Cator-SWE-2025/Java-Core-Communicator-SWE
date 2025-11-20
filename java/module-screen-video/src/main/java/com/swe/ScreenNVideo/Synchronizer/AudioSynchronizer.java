/**
 * Contributed by @chirag9528
 */

package com.swe.ScreenNVideo.Synchronizer;

import com.swe.ScreenNVideo.Codec.ADPCMDecoder;
import com.swe.ScreenNVideo.Model.APackets;
import com.swe.ScreenNVideo.Model.CPackets;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.Playback.AudioPlayer;
import com.swe.ScreenNVideo.Utils;

import javax.sound.sampled.LineUnavailableException;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Synchronizer to synchronize the image from the patches.
 */
public class AudioSynchronizer {

    /**
     * The next feed number we expect to recieve in correct order.
     * Used to ensure patches are applied sequentially.
     */
    private int expectedFeedNumber;

    public int getExpectedFeedNumber() {
        return expectedFeedNumber;
    }

    /**
     * Sets the expected feedNumber.
     *
     * @param expectedFeedNumberArgs the number to update
     */
    public void setExpectedFeedNumber(final int expectedFeedNumberArgs) {
        this.expectedFeedNumber = expectedFeedNumberArgs;
    }

    /**
     * Min-heap storing out-of-order feed packets.
     */
    private final PriorityQueue<APackets> heap;

    /**
     * @return the min-heap of {@link FeedData} items used for ordering incoming packets.
     */
    public PriorityQueue<APackets> getHeap() {
        return this.heap;
    }

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
        this.expectedFeedNumber = 0;
        this.heap = new PriorityQueue<>((a, b) -> Integer.compare(a.packetNumber(), b.packetNumber()));
        this.decoder = new ADPCMDecoder();
        this.resetHeap = false;
        this.audioPlayer = audioPlayerArg;
    }

    /**
     * .
     *
     */
    public boolean synchronize(final APackets apacket) {
        if(this.resetHeap){
            this.expectedFeedNumber = apacket.packetNumber();
            this.resetHeap = false;
        }

        heap.add(apacket);

        // if heap is growing too large, request a full frame to resync
        if (this.getHeap().size() >= Utils.MAX_HEAP_SIZE) {
            System.out.println("Too Large");
            this.getHeap().clear();
            this.resetHeap = true;
            return false; // ask for data
        }


        // drop all entries older than this full image
        while (!this.getHeap().isEmpty()
                && this.getHeap().peek().packetNumber()
                < this.getExpectedFeedNumber()) {
//                        System.out.println("Removing " + imageSynchronizer.getHeap().peek().getFeedNumber());
            this.getHeap().poll();
        }

        while (true) {

            // If the next expected patch hasn't arrived yet, wait
            final APackets feedData = this.getHeap().peek();
            if (feedData == null || feedData.packetNumber() != this.getExpectedFeedNumber()) {
                break;
            }

            final APackets minFeedCPacket = this.getHeap().poll();

            if (minFeedCPacket == null) {
                break;
            }

            System.out.println("Min Feed Packet " + minFeedCPacket.packetNumber());
            expectedFeedNumber ++;

            // setting the decoder state before decoding
//            int predictedPCM = minFeedCPacket.predictedPCM();
//            int indexPCM = minFeedCPacket.indexPCM();
//            decoder.setState(predictedPCM, indexPCM);

            // playing the decoded audio sample
            this.audioPlayer.play(decoder.decode(minFeedCPacket.data()));
        }


    return true;
    }
}
