/**
 * Author : Abhirami R Iyer
 */
package data;

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
        // accesses the image and encodes it to string(pngbytes)
        System.out.println("Reading the image, converting it to Base64");
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
