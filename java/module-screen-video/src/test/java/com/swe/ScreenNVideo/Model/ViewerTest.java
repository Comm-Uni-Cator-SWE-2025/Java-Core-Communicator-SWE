/**
 * Contributed by @chirag9528
 */
package com.swe.ScreenNVideo.Model;

import com.swe.core.ClientNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Comprehensive test suite for Viewer class.
 * Tests encapsulation of ClientNode and compression preferences.
 */
public class ViewerTest {
    /**
     * Tests that the constructor correctly initializes fields.
     */
    @Test
    public void testConstructorAndGetters() {
        final ClientNode mockNode = mock(ClientNode.class);
        final boolean requireCompressed = true;

        final Viewer viewer = new Viewer(mockNode, requireCompressed);

        assertSame(mockNode, viewer.getNode(), "ClientNode should match constructor argument");
        assertTrue(viewer.isRequireCompressed(), "Compression flag should match constructor argument");
    }

    /**
     * Tests that the setter updates the compression flag correctly.
     */
    @Test
    public void testSetRequireCompressed() {
        final ClientNode mockNode = mock(ClientNode.class);
        final Viewer viewer = new Viewer(mockNode, false);

        // Initial state
        assertFalse(viewer.isRequireCompressed());

        // Update state
        viewer.setRequireCompressed(true);
        assertTrue(viewer.isRequireCompressed(), "Flag should be updated to true");
    }

    /**
     * Tests behavior with null ClientNode.
     */
    @Test
    public void testConstructorWithNullNode() {
        final Viewer viewer = new Viewer(null, true);

        assertNull(viewer.getNode(), "Node should be null");
        assertTrue(viewer.isRequireCompressed());
    }
}
