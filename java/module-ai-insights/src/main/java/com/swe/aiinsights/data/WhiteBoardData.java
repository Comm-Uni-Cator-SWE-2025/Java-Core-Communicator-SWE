/*
 * -----------------------------------------------------------------------------
 *  File: WhiteboardData.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.data
 * -----------------------------------------------------------------------------
 */

/**
 * To store the snapshot of whiteboard sent by the Canvas team.
 *
 * @author Abhirami R Iyer
 */

package com.swe.aiinsights.data;

import com.swe.aiinsights.logging.CommonLogger;
import org.slf4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Represents an image file to be sent to the AI.
 * Reads the image from disk and converts it to Base64.
 */
public class WhiteBoardData {
    /**
     * Get the log file path.
     */
    private static final Logger LOG =
            CommonLogger.getLogger(WhiteBoardData.class);
    /**
     * Stores the content of image file.
     */
    private String content;

    /**
     * Image file path.
     */
    private Path imgFile;

    /**
     * Constructs a WhiteBoardData object by reading the image file.
     *
     * @param img Path to the image file.
     * @throws IOException if reading the file fails.
     */
    public WhiteBoardData(final String img) throws IOException {
        // accesses the image and encodes it to string(png bytes)
        LOG.info("Reading the image, converting it to Base64");
        this.imgFile = Paths.get(img);
        final byte[] pngBytes = Files.readAllBytes(imgFile);
        this.content = Base64.getEncoder().encodeToString(pngBytes);
    }

    /**
     * Returns the Base64 encoded content of the image.
     *
     * @return Base64 encoded image content.
     */
    public String getContent() throws IOException {
        // to get the string content
        return this.content;
    }

    /**
     * Sets the Base64 content of the image(if not read from the path).
     *
     * @param contentToSet Base64 string of image content.
     */
    public void setContent(final String contentToSet) {
        // to set the content
        this.content = contentToSet;
    }
}
