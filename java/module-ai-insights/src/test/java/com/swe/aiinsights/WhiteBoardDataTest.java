/*
 * -----------------------------------------------------------------------------
 *  File: WhiteBoardDataTest.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201003
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.swe.aiinsights.data.WhiteBoardData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Test class for WhiteBoardData with 100% code coverage.
 */
class WhiteBoardDataTest {
    /**
     * path to create a temporary img file.
     */
    @TempDir
    private Path tempDir;


    @Test
    void testConstructorValidImageFile() throws IOException {
        final Path imageFile = tempDir.resolve("test-image.png");
        final byte[] imageData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        Files.write(imageFile, imageData);

        final WhiteBoardData wbData = new WhiteBoardData(imageFile.toString());

        assertNotNull(wbData);
        assertNotNull(wbData.getContent());
    }

    @Test
    void testConstructorNonExistentFile() {
        final String nonExistentPath = "/nonexistent/path/image.png";

        assertThrows(IOException.class, () -> {
            new WhiteBoardData(nonExistentPath);
        });
    }


    @Test
    void testSetContent() throws IOException {
        final Path imageFile = tempDir.resolve("override-test.png");
        final byte[] originalData = new byte[]{0x01, 0x02};
        Files.write(imageFile, originalData);
        final WhiteBoardData wbData = new WhiteBoardData(imageFile.toString());

        final String newContent = "NewBase64EncodedContent";

        wbData.setContent(newContent);

        assertEquals(newContent, wbData.getContent());
    }



}