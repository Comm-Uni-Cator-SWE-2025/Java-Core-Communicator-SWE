/**
 * Contributed by @Bhupati-Varun
 */

package com.swe.ScreenNVideo.Model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class RImageTest {

    @Test
    @DisplayName("Constructor should store values correctly")
    void testConstructorAndGetters() {
        // Setup
        int[][] dummyImage = new int[10][10];
        String ip = "192.168.1.1";
        long dataRate = 5000L;

        // Action
        RImage rImage = new RImage(dummyImage, ip, dataRate);

        // Verification
        // Verify that the constructor assigned the fields and getters return them correctly
        assertNotNull(rImage);
        assertSame(dummyImage, rImage.getImage(), "Image reference should be preserved");
        assertEquals(ip, rImage.getIp(), "IP address should match");
        assertEquals(dataRate, rImage.getDataRate(), "Data rate should match");
    }

    @Test
    @DisplayName("Serialize() should correctly pack IP, DataRate, Dims, and RGB Data")
    void testSerialize() {
        // 1. Setup Data
        String ip = "127.0.0.1";
        long dataRate = 999999L;

        // Create a 1x2 image (Height 1, Width 2)
        // Pixel 0 (0,0): Pure Red   (0xFF0000) -> R=255, G=0, B=0
        // Pixel 1 (0,1): Pure Blue  (0x0000FF) -> R=0, G=0, B=255
        int[][] image = new int[1][2];
        image[0][0] = 0xFF0000;
        image[0][1] = 0x0000FF;

        RImage rImage = new RImage(image, ip, dataRate);

        // 2. Action
        byte[] result = rImage.serialize();

        // 3. Verification (Manually parse the byte array to ensure format matches Logic)
        ByteBuffer buffer = ByteBuffer.wrap(result);

        // A. Verify IP Address
        int ipLen = buffer.getInt();
        assertEquals(ip.length(), ipLen, "IP length should match");

        byte[] ipBytes = new byte[ipLen];
        buffer.get(ipBytes);
        assertEquals(ip, new String(ipBytes), "IP string should match");

        // B. Verify Data Rate
        long extractedDataRate = buffer.getLong();
        assertEquals(dataRate, extractedDataRate, "DataRate should match");

        // C. Verify Dimensions (Height then Width)
        int height = buffer.getInt();
        int width = buffer.getInt();
        assertEquals(1, height, "Height should be 1");
        assertEquals(2, width, "Width should be 2");

        // D. Verify RGB Pixels
        // The RImage logic shifts >> 16 for Red, >> 8 for Green.

        // Pixel 0: Expecting 255, 0, 0
        assertEquals((byte) 255, buffer.get(), "Pixel 0 Red");
        assertEquals((byte) 0,   buffer.get(), "Pixel 0 Green");
        assertEquals((byte) 0,   buffer.get(), "Pixel 0 Blue");

        // Pixel 1: Expecting 0, 0, 255
        assertEquals((byte) 0,   buffer.get(), "Pixel 1 Red");
        assertEquals((byte) 0,   buffer.get(), "Pixel 1 Green");
        assertEquals((byte) 255, buffer.get(), "Pixel 1 Blue");

        // Ensure no extra bytes are left
        assertFalse(buffer.hasRemaining(), "Buffer should be fully consumed");
    }
}