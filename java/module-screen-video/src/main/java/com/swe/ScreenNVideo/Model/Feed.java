/**
 * Contributed by @alonot.
 */

package com.swe.ScreenNVideo.Model;

/**
 * Feed model for the ScreenNVideo module.
 * @param compressedFeed The compressed feed.
 * @param unCompressedFeed The uncompressed feed.
 */
public record Feed(byte[] compressedFeed, byte[] unCompressedFeed) {
}
