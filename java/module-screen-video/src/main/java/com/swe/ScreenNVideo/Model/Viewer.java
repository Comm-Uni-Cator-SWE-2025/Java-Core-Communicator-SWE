/**
 * Contributed by @chirag9528.
 */

package com.swe.ScreenNVideo.Model;


import com.swe.core.ClientNode;

/**
 * Represents a Viewer for Screen Share module.
 */
public class Viewer {
    /**
     * Client node for this viewer.
     */
    private final ClientNode node;
    /**
     * This viewer requires compressed feed or not.
     */
    private boolean requireCompressed;

    /**
     * Get the client node for this viewer.
     * @return the client node
     */
    public ClientNode getNode() {
        return node;
    }

    /**
     * Check if this viewer requires compressed feed.
     * @return true if requires compressed feed, false otherwise
     */
    public boolean isRequireCompressed() {
        return requireCompressed;
    }

    /**
     * Constructor for the Viewer class.
     * @param nodeArgs The client node.
     * @param requireCompressedArgs The request compression.
     */
    public Viewer(final ClientNode nodeArgs, final boolean requireCompressedArgs) {
        node = nodeArgs;
        requireCompressed = requireCompressedArgs;
    }

    /**
     * Set the request compression.
     * @param val The request compression.
     */
    public void setRequireCompressed(final boolean val) {
        requireCompressed = val;
    }
}
