package com.swe.ScreenNVideo.Capture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * A simple test class to verify the functionality of VideoCapture.
 */
public class test_video {

    public static void main(String[] args) {
        System.out.println("Attempting to initialize webcam...");
        VideoCapture videoCapture = null;

        try {
            // 1. Create an object of VideoCapture
            videoCapture = new VideoCapture();

            // Give the webcam a second to initialize and adjust lighting
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 2. Capture a single frame
            System.out.println("Capturing frame...");
            BufferedImage frame = videoCapture.capture();

            // 3. Check if the frame was captured successfully
            if (frame != null) {
                File outputFile = new File("webcam_capture.jpg");
                // 4. Save the captured frame as a JPG file
                ImageIO.write(frame, "JPG", outputFile);
                System.out.println("✅ Success! Image saved to: " + outputFile.getAbsolutePath());
            } else {
                System.err.println("❌ Failed to capture frame. Is a webcam connected and not in use?");
            }

        } catch (IOException e) {
            System.err.println("❌ Error saving the image file.");
            e.printStackTrace();
        } finally {
            // 5. IMPORTANT: Always close the webcam to release the resource
            if (videoCapture != null) {
                videoCapture.close();
            }
        }
    }
}