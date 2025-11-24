/**
 * Contributed by @chirag9528
 */
package com.swe.ScreenNVideo.Synchronizer;

import com.swe.ScreenNVideo.Model.CPackets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * Comprehensive test suite for FeedData class.
 * Tests data encapsulation for synchronization packets.
 */
public class FeedDataTest {
    /**
     * Tests that the constructor correctly initializes fields and getters return correct values.
     */
    @Test
    public void testConstructorAndGetters() {
        final int feedNumber = 100;
        final CPackets mockPackets = mock(CPackets.class);

        final FeedData feedData = new FeedData(feedNumber, mockPackets);

        assertEquals(feedNumber, feedData.getFeedNumber(), "Feed number should match constructor argument");
        assertSame(mockPackets, feedData.getFeedPackets(), "Packets object should be the same instance");
    }

    /**
     * Tests with different values to ensure no static/shared state issues.
     */
    @Test
    public void testWithDifferentValues() {
        final int feedNumber = 42;
        final CPackets mockPackets = mock(CPackets.class);

        final FeedData feedData = new FeedData(feedNumber, mockPackets);

        assertEquals(feedNumber, feedData.getFeedNumber());
        assertSame(mockPackets, feedData.getFeedPackets());
    }

    /**
     * Tests behavior with null packets.
     */
    @Test
    public void testConstructorWithNullPackets() {
        final int feedNumber = 1;
        final FeedData feedData = new FeedData(feedNumber, null);

        assertEquals(feedNumber, feedData.getFeedNumber());
        assertSame(null, feedData.getFeedPackets());
    }
}
