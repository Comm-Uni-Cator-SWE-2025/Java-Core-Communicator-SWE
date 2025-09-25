package com.swe.ScreenNVideo.Capture;

import com.github.sarxos.webcam.Webcam;
import java.util.List;

public class WebcamDiagnostics {
    public static void main(String[] args) {
        System.out.println("Attempting to discover webcams...");
        try {
            List<Webcam> webcams = Webcam.getWebcams();
            if (webcams.isEmpty()) {
                System.out.println("Result: No webcams found.");
                System.out.println("Possible causes: No webcam connected, drivers missing, or OS permissions blocking access.");
            } else {
                System.out.println("Result: Found " + webcams.size() + " webcam(s).");
                for (Webcam cam : webcams) {
                    System.out.println("- " + cam.getName());
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred during webcam discovery:");
            e.printStackTrace();
        }
    }
}